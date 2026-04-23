package com.quanli.quanligara.service;

import com.quanli.quanligara.dao.InvoiceDAO;
import com.quanli.quanligara.dao.InvoiceLineDAO;
import com.quanli.quanligara.dao.WorkOrderDAO;
import com.quanli.quanligara.model.*;
import com.quanli.quanligara.model.enums.InvoiceItemType;
import com.quanli.quanligara.model.enums.InvoiceStatus;
import com.quanli.quanligara.model.enums.WorkOrderStatus;
import com.quanli.quanligara.util.JpaUtil;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class InvoiceService {

    private final InvoiceDAO invoiceDAO;
    private final InvoiceLineDAO invoiceLineDAO;
    private final WorkOrderDAO workOrderDAO;

    public InvoiceService() {
        this.invoiceDAO = new InvoiceDAO();
        this.invoiceLineDAO = new InvoiceLineDAO();
        this.workOrderDAO = new WorkOrderDAO();
    }

    public Invoice issueInvoice(Long workOrderId, User adminUser) {
        if (workOrderId == null) {
            throw new IllegalArgumentException("Work order ID is required");
        }
        if (adminUser == null || !adminUser.isAdmin()) {
            throw new IllegalArgumentException("Admin user is required to issue invoice");
        }

        return JpaUtil.inTransactionWithResult(em -> {
            WorkOrder workOrder = em.find(WorkOrder.class, workOrderId);
            if (workOrder == null) {
                throw new IllegalArgumentException("Work order not found: " + workOrderId);
            }
            em.createQuery(
                    "SELECT w FROM WorkOrder w LEFT JOIN FETCH w.partLines WHERE w.id = :id",
                    WorkOrder.class)
                    .setParameter("id", workOrderId)
                    .getSingleResult();
            em.createQuery(
                    "SELECT w FROM WorkOrder w LEFT JOIN FETCH w.serviceLines WHERE w.id = :id",
                    WorkOrder.class)
                    .setParameter("id", workOrderId)
                    .getSingleResult();
            workOrder = em.find(WorkOrder.class, workOrderId);
            if (workOrder == null) {
                throw new IllegalArgumentException("Work order not found: " + workOrderId);
            }
            Long existingInvoices = em.createQuery(
                            "SELECT COUNT(i) FROM Invoice i WHERE i.workOrder.id = :wid", Long.class)
                    .setParameter("wid", workOrderId)
                    .getSingleResult();
            if (existingInvoices != null && existingInvoices > 0) {
                throw new IllegalStateException("Invoice already exists for this work order");
            }
            if (workOrder.getStatus() == WorkOrderStatus.INVOICED) {
                throw new IllegalStateException("Work order is already invoiced");
            }
            if (workOrder.getPartLines().isEmpty() && workOrder.getServiceLines().isEmpty()) {
                throw new IllegalStateException("Cannot issue invoice for empty work order");
            }

            // Re-validate stock for each part line
            for (WorkOrderPartLine partLine : workOrder.getPartLines()) {
                SparePart freshPart = em.find(SparePart.class, partLine.getSparePart().getId());
                if (freshPart == null) {
                    throw new IllegalStateException("Spare part no longer exists: " + partLine.getSparePart().getCode());
                }
                if (freshPart.getStockQuantity() < partLine.getQuantity()) {
                    throw new IllegalStateException(
                        String.format("Insufficient stock for part %s. Available: %d, Requested: %d",
                            freshPart.getCode(), freshPart.getStockQuantity(), partLine.getQuantity())
                    );
                }
                // Decrement stock
                freshPart.setStockQuantity(freshPart.getStockQuantity() - partLine.getQuantity());
                em.merge(freshPart);
            }

            // Create invoice header
            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber(generateInvoiceNumber(em));
            invoice.setWorkOrder(workOrder);
            invoice.setUser(workOrder.getUser());
            invoice.setStatus(InvoiceStatus.ISSUED);
            invoice.setIssuedAt(LocalDateTime.now());

            // Create snapshot lines
            BigDecimal totalAmount = BigDecimal.ZERO;

            // Part lines — snapshot prices from current catalog rows in this persistence context
            for (WorkOrderPartLine partLine : workOrder.getPartLines()) {
                SparePart part = em.find(SparePart.class, partLine.getSparePart().getId());
                InvoiceLine line = new InvoiceLine();
                line.setInvoice(invoice);
                line.setItemType(InvoiceItemType.PART);
                line.setItemCode(part.getCode());
                line.setItemName(part.getName());
                line.setUnitPrice(part.getUnitPrice());
                line.setQuantity(partLine.getQuantity());
                line.setLineTotal(part.getUnitPrice().multiply(BigDecimal.valueOf(partLine.getQuantity())));
                invoice.getLines().add(line);
                totalAmount = totalAmount.add(line.getLineTotal());
            }

            // Service lines
            for (WorkOrderServiceLine serviceLine : workOrder.getServiceLines()) {
                ServiceOffering service = em.find(ServiceOffering.class, serviceLine.getServiceOffering().getId());
                InvoiceLine line = new InvoiceLine();
                line.setInvoice(invoice);
                line.setItemType(InvoiceItemType.SERVICE);
                line.setItemCode(service.getCode());
                line.setItemName(service.getName());
                line.setUnitPrice(service.getUnitPrice());
                line.setQuantity(serviceLine.getQuantity());
                line.setLineTotal(service.getUnitPrice().multiply(BigDecimal.valueOf(serviceLine.getQuantity())));
                invoice.getLines().add(line);
                totalAmount = totalAmount.add(line.getLineTotal());
            }

            invoice.setTotalAmount(totalAmount);

            // Update work order
            workOrder.setStatus(WorkOrderStatus.INVOICED);
            workOrder.setInvoicedAt(LocalDateTime.now());
            em.merge(workOrder);

            // Persist invoice (cascade will save lines)
            // Retry once or twice if a concurrent issuance uses the same number.
            int attempts = 0;
            while (true) {
                try {
                    if (attempts > 0) {
                        invoice.setInvoiceNumber(generateInvoiceNumber(em));
                    }
                    em.persist(invoice);
                    break;
                } catch (jakarta.persistence.PersistenceException pe) {
                    attempts++;
                    if (attempts >= 3) {
                        throw pe;
                    }
                }
            }

            return invoice;
        });
    }

    public Optional<Invoice> findById(Long id) {
        return invoiceDAO.findById(id);
    }

    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        return invoiceDAO.findByInvoiceNumber(invoiceNumber);
    }

    public Optional<Invoice> findByWorkOrder(WorkOrder workOrder) {
        return invoiceDAO.findByWorkOrder(workOrder);
    }

    public List<Invoice> findByUser(User user) {
        return invoiceDAO.findByUser(user);
    }

    public List<Invoice> findAll() {
        return invoiceDAO.findAll();
    }

    /** Payments screen: filter issued invoices by customer name / username. */
    public List<Invoice> searchInvoicesByCustomerName(String keyword) {
        return invoiceDAO.findByCustomerNameLike(keyword);
    }

    public boolean isWorkOrderInvoiced(Long workOrderId) {
        Optional<WorkOrder> wo = workOrderDAO.findById(workOrderId);
        return wo.map(w -> w.getStatus() == WorkOrderStatus.INVOICED).orElse(false);
    }

    public boolean canIssueInvoice(Long workOrderId) {
        Optional<WorkOrder> wo = workOrderDAO.findWithLines(workOrderId);
        if (wo.isEmpty()) {
            return false;
        }
        WorkOrder workOrder = wo.get();
        if (workOrder.getStatus() == WorkOrderStatus.INVOICED) {
            return false;
        }
        return !workOrder.getPartLines().isEmpty() || !workOrder.getServiceLines().isEmpty();
    }

    public List<InvoiceLine> getInvoiceLines(Long invoiceId) {
        Optional<Invoice> invoice = invoiceDAO.findById(invoiceId);
        return invoice.map(i -> invoiceLineDAO.findByInvoice(i)).orElse(List.of());
    }

    public Optional<Invoice> loadInvoiceForDisplay(Long invoiceId) {
        Invoice inv = JpaUtil.inTransactionWithResult(em -> {
            Invoice i = em.find(Invoice.class, invoiceId);
            if (i == null) {
                return null;
            }
            Hibernate.initialize(i.getUser());
            Hibernate.initialize(i.getWorkOrder());
            Hibernate.initialize(i.getLines());
            return i;
        });
        return Optional.ofNullable(inv);
    }

    public void confirmPayment(Long invoiceId) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("Invoice ID is required");
        }
        JpaUtil.inTransaction(em -> {
            Invoice inv = em.find(Invoice.class, invoiceId);
            if (inv == null) {
                throw new IllegalArgumentException("Invoice not found");
            }
            if (inv.getPaidAt() == null) {
                inv.setPaidAt(LocalDateTime.now());
                em.merge(inv);
            }
        });
    }

    public boolean userOwnsInvoice(Long invoiceId, Long userId) {
        if (invoiceId == null || userId == null) {
            return false;
        }
        Long count = JpaUtil.inTransactionWithResult(em -> em.createQuery(
                        "SELECT COUNT(i) FROM Invoice i WHERE i.id = :id AND i.user.id = :uid", Long.class)
                .setParameter("id", invoiceId)
                .setParameter("uid", userId)
                .getSingleResult());
        return count != null && count > 0;
    }

    private String generateInvoiceNumber(EntityManager em) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "INV-" + datePart + "-";

        // Because the suffix is zero-padded to 6 digits, DESC string sort matches numeric sort.
        List<String> last = em.createQuery(
                        "SELECT i.invoiceNumber FROM Invoice i " +
                                "WHERE i.invoiceNumber LIKE :p " +
                                "ORDER BY i.invoiceNumber DESC",
                        String.class)
                .setParameter("p", prefix + "%")
                .setMaxResults(1)
                .getResultList();

        long nextSeq = 1;
        if (!last.isEmpty() && last.get(0) != null && last.get(0).startsWith(prefix)) {
            String suffix = last.get(0).substring(prefix.length());
            try {
                nextSeq = Long.parseLong(suffix) + 1;
            } catch (NumberFormatException ignored) {
                nextSeq = 1;
            }
        }
        return prefix + String.format("%06d", nextSeq);
    }
}
