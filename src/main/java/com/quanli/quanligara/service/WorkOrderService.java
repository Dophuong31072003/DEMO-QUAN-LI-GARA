package com.quanli.quanligara.service;

import com.quanli.quanligara.dao.UserDAO;
import com.quanli.quanligara.dao.WorkOrderDAO;
import com.quanli.quanligara.model.*;
import com.quanli.quanligara.model.enums.WorkOrderStatus;
import com.quanli.quanligara.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class WorkOrderService {

    private final WorkOrderDAO workOrderDAO;

    public WorkOrderService() {
        this.workOrderDAO = new WorkOrderDAO();
    }

    public List<WorkOrder> listOpenWorkOrdersForAdmin() {
        return workOrderDAO.findAllOpenWithUser();
    }

    public int countPartLines(Long workOrderId) {
        return workOrderDAO.countPartLines(workOrderId);
    }

    public int countServiceLines(Long workOrderId) {
        return workOrderDAO.countServiceLines(workOrderId);
    }

    /**
     * Loads work order with user, part lines, service lines, and catalog rows initialized for JSP rendering.
     */
    public Optional<WorkOrder> loadWorkOrderForDisplay(Long workOrderId) {
        WorkOrder w = JpaUtil.inTransactionWithResult(em -> {
            WorkOrder wo = em.find(WorkOrder.class, workOrderId);
            if (wo == null) {
                return null;
            }
            Hibernate.initialize(wo.getUser());
            Hibernate.initialize(wo.getPartLines());
            for (WorkOrderPartLine pl : wo.getPartLines()) {
                Hibernate.initialize(pl.getSparePart());
            }
            Hibernate.initialize(wo.getServiceLines());
            for (WorkOrderServiceLine sl : wo.getServiceLines()) {
                Hibernate.initialize(sl.getServiceOffering());
            }
            return wo;
        });
        return Optional.ofNullable(w);
    }

    public Optional<WorkOrder> getOpenWorkOrder(User sessionUser) {
        return workOrderDAO.findOpenByUser(sessionUser);
    }

    /**
     * Staff: open or create DRAFT/SUBMITTED work order (quotation) for a customer user id.
     */
    public WorkOrder getOrCreateOpenQuotationForCustomer(Long customerUserId) {
        if (customerUserId == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        UserDAO userDAO = new UserDAO();
        User u = userDAO.findById(customerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        if (u.isAdmin()) {
            throw new IllegalArgumentException("User is not a customer account");
        }
        if (!u.isActive()) {
            throw new IllegalArgumentException("Customer account is inactive");
        }
        return getOrCreateOpenWorkOrder(u);
    }

    public WorkOrder getOrCreateOpenWorkOrder(User sessionUser) {
        if (sessionUser == null || sessionUser.getId() == null) {
            throw new IllegalArgumentException("User is required");
        }
        return JpaUtil.inTransactionWithResult(em -> {
            User managedUser = em.find(User.class, sessionUser.getId());
            if (managedUser == null) {
                throw new IllegalArgumentException("User not found");
            }
            TypedQuery<WorkOrder> q = em.createQuery(
                    "SELECT w FROM WorkOrder w WHERE w.user.id = :uid AND w.status IN :st",
                    WorkOrder.class);
            q.setParameter("uid", managedUser.getId());
            q.setParameter("st", List.of(WorkOrderStatus.DRAFT, WorkOrderStatus.SUBMITTED));
            List<WorkOrder> found = q.getResultList();
            if (!found.isEmpty()) {
                return found.get(0);
            }
            WorkOrder w = new WorkOrder();
            w.setUser(managedUser);
            w.setStatus(WorkOrderStatus.DRAFT);
            em.persist(w);
            return w;
        });
    }

    public void addPartLine(User sessionUser, Long sparePartId, int quantity) {
        requirePositiveQty(quantity);
        JpaUtil.inTransaction(em -> {
            WorkOrder w = loadOpenWorkOrderManaged(em, sessionUser.getId());
            assertEditable(w);
            SparePart part = em.find(SparePart.class, sparePartId);
            if (part == null || !part.isActive()) {
                throw new IllegalArgumentException("Spare part not found or inactive");
            }
            for (WorkOrderPartLine line : w.getPartLines()) {
                if (line.getSparePart().getId().equals(sparePartId)) {
                    line.setQuantity(line.getQuantity() + quantity);
                    return;
                }
            }
            WorkOrderPartLine line = new WorkOrderPartLine();
            line.setWorkOrder(w);
            line.setSparePart(part);
            line.setQuantity(quantity);
            w.getPartLines().add(line);
            em.persist(line);
        });
    }

    public void updatePartLineQuantity(User sessionUser, Long partLineId, int quantity) {
        requirePositiveQty(quantity);
        JpaUtil.inTransaction(em -> {
            WorkOrderPartLine line = em.find(WorkOrderPartLine.class, partLineId);
            if (line == null) {
                throw new IllegalArgumentException("Line not found");
            }
            WorkOrder w = line.getWorkOrder();
            assertOwnedOpen(w, sessionUser.getId());
            assertEditable(w);
            line.setQuantity(quantity);
        });
    }

    public void removePartLine(User sessionUser, Long partLineId) {
        JpaUtil.inTransaction(em -> {
            WorkOrderPartLine line = em.find(WorkOrderPartLine.class, partLineId);
            if (line == null) {
                return;
            }
            WorkOrder w = line.getWorkOrder();
            assertOwnedOpen(w, sessionUser.getId());
            assertEditable(w);
            w.getPartLines().remove(line);
            em.remove(line);
        });
    }

    public void addServiceLine(User sessionUser, Long serviceOfferingId, int quantity) {
        requirePositiveQty(quantity);
        JpaUtil.inTransaction(em -> {
            WorkOrder w = loadOpenWorkOrderManaged(em, sessionUser.getId());
            assertEditable(w);
            ServiceOffering svc = em.find(ServiceOffering.class, serviceOfferingId);
            if (svc == null || !svc.isActive()) {
                throw new IllegalArgumentException("Service not found or inactive");
            }
            for (WorkOrderServiceLine line : w.getServiceLines()) {
                if (line.getServiceOffering().getId().equals(serviceOfferingId)) {
                    line.setQuantity(line.getQuantity() + quantity);
                    return;
                }
            }
            WorkOrderServiceLine line = new WorkOrderServiceLine();
            line.setWorkOrder(w);
            line.setServiceOffering(svc);
            line.setQuantity(quantity);
            w.getServiceLines().add(line);
            em.persist(line);
        });
    }

    public void updateServiceLineQuantity(User sessionUser, Long serviceLineId, int quantity) {
        requirePositiveQty(quantity);
        JpaUtil.inTransaction(em -> {
            WorkOrderServiceLine line = em.find(WorkOrderServiceLine.class, serviceLineId);
            if (line == null) {
                throw new IllegalArgumentException("Line not found");
            }
            WorkOrder w = line.getWorkOrder();
            assertOwnedOpen(w, sessionUser.getId());
            assertEditable(w);
            line.setQuantity(quantity);
        });
    }

    public void removeServiceLine(User sessionUser, Long serviceLineId) {
        JpaUtil.inTransaction(em -> {
            WorkOrderServiceLine line = em.find(WorkOrderServiceLine.class, serviceLineId);
            if (line == null) {
                return;
            }
            WorkOrder w = line.getWorkOrder();
            assertOwnedOpen(w, sessionUser.getId());
            assertEditable(w);
            w.getServiceLines().remove(line);
            em.remove(line);
        });
    }

    // -- Staff: edit quotation by work order id (no end-user session) --

    public void addPartLineToWorkOrder(Long workOrderId, Long sparePartId, int quantity) {
        requirePositiveQty(quantity);
        JpaUtil.inTransaction(em -> {
            WorkOrder w = em.find(WorkOrder.class, workOrderId);
            if (w == null) {
                throw new IllegalArgumentException("Work order not found");
            }
            assertEditable(w);
            Hibernate.initialize(w.getPartLines());
            SparePart part = em.find(SparePart.class, sparePartId);
            if (part == null || !part.isActive()) {
                throw new IllegalArgumentException("Spare part not found or inactive");
            }
            for (WorkOrderPartLine line : w.getPartLines()) {
                if (line.getSparePart().getId().equals(sparePartId)) {
                    line.setQuantity(line.getQuantity() + quantity);
                    return;
                }
            }
            WorkOrderPartLine line = new WorkOrderPartLine();
            line.setWorkOrder(w);
            line.setSparePart(part);
            line.setQuantity(quantity);
            w.getPartLines().add(line);
            em.persist(line);
        });
    }

    public void updatePartLineQuantityOnWorkOrder(Long workOrderId, Long partLineId, int quantity) {
        requirePositiveQty(quantity);
        JpaUtil.inTransaction(em -> {
            WorkOrderPartLine line = em.find(WorkOrderPartLine.class, partLineId);
            if (line == null) {
                throw new IllegalArgumentException("Line not found");
            }
            WorkOrder w = line.getWorkOrder();
            if (w == null || !w.getId().equals(workOrderId)) {
                throw new IllegalArgumentException("Line does not belong to this work order");
            }
            assertEditable(w);
            line.setQuantity(quantity);
        });
    }

    public void removePartLineFromWorkOrder(Long workOrderId, Long partLineId) {
        JpaUtil.inTransaction(em -> {
            WorkOrderPartLine line = em.find(WorkOrderPartLine.class, partLineId);
            if (line == null) {
                return;
            }
            WorkOrder w = line.getWorkOrder();
            if (w == null || !w.getId().equals(workOrderId)) {
                throw new IllegalArgumentException("Line does not belong to this work order");
            }
            assertEditable(w);
            Hibernate.initialize(w.getPartLines());
            w.getPartLines().remove(line);
            em.remove(line);
        });
    }

    public void addServiceLineToWorkOrder(Long workOrderId, Long serviceOfferingId, int quantity) {
        requirePositiveQty(quantity);
        JpaUtil.inTransaction(em -> {
            WorkOrder w = em.find(WorkOrder.class, workOrderId);
            if (w == null) {
                throw new IllegalArgumentException("Work order not found");
            }
            assertEditable(w);
            Hibernate.initialize(w.getServiceLines());
            ServiceOffering svc = em.find(ServiceOffering.class, serviceOfferingId);
            if (svc == null || !svc.isActive()) {
                throw new IllegalArgumentException("Service not found or inactive");
            }
            for (WorkOrderServiceLine line : w.getServiceLines()) {
                if (line.getServiceOffering().getId().equals(serviceOfferingId)) {
                    line.setQuantity(line.getQuantity() + quantity);
                    return;
                }
            }
            WorkOrderServiceLine line = new WorkOrderServiceLine();
            line.setWorkOrder(w);
            line.setServiceOffering(svc);
            line.setQuantity(quantity);
            w.getServiceLines().add(line);
            em.persist(line);
        });
    }

    public void updateServiceLineQuantityOnWorkOrder(Long workOrderId, Long serviceLineId, int quantity) {
        requirePositiveQty(quantity);
        JpaUtil.inTransaction(em -> {
            WorkOrderServiceLine line = em.find(WorkOrderServiceLine.class, serviceLineId);
            if (line == null) {
                throw new IllegalArgumentException("Line not found");
            }
            WorkOrder w = line.getWorkOrder();
            if (w == null || !w.getId().equals(workOrderId)) {
                throw new IllegalArgumentException("Line does not belong to this work order");
            }
            assertEditable(w);
            line.setQuantity(quantity);
        });
    }

    public void removeServiceLineFromWorkOrder(Long workOrderId, Long serviceLineId) {
        JpaUtil.inTransaction(em -> {
            WorkOrderServiceLine line = em.find(WorkOrderServiceLine.class, serviceLineId);
            if (line == null) {
                return;
            }
            WorkOrder w = line.getWorkOrder();
            if (w == null || !w.getId().equals(workOrderId)) {
                throw new IllegalArgumentException("Line does not belong to this work order");
            }
            assertEditable(w);
            Hibernate.initialize(w.getServiceLines());
            w.getServiceLines().remove(line);
            em.remove(line);
        });
    }

    public void submit(User sessionUser) {
        JpaUtil.inTransaction(em -> {
            WorkOrder w = loadOpenWorkOrderManaged(em, sessionUser.getId());
            if (w.getStatus() == WorkOrderStatus.INVOICED) {
                throw new IllegalStateException("Cannot submit an invoiced work order");
            }
            if (w.getStatus() == WorkOrderStatus.DRAFT) {
                w.setStatus(WorkOrderStatus.SUBMITTED);
                w.setSubmittedAt(LocalDateTime.now());
            }
        });
    }

    public boolean userOwnsWorkOrder(Long workOrderId, Long userId) {
        if (workOrderId == null || userId == null) {
            return false;
        }
        Long count = JpaUtil.inTransactionWithResult(em -> em.createQuery(
                        "SELECT COUNT(w) FROM WorkOrder w WHERE w.id = :id AND w.user.id = :uid", Long.class)
                .setParameter("id", workOrderId)
                .setParameter("uid", userId)
                .getSingleResult());
        return count != null && count > 0;
    }

    private static void requirePositiveQty(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    private static void assertEditable(WorkOrder w) {
        if (w.getStatus() == WorkOrderStatus.INVOICED) {
            throw new IllegalStateException("Work order is invoiced and cannot be edited");
        }
    }

    private static void assertOwnedOpen(WorkOrder w, Long userId) {
        if (!w.getUser().getId().equals(userId)) {
            throw new SecurityException("Work order does not belong to current user");
        }
        if (w.getStatus() != WorkOrderStatus.DRAFT && w.getStatus() != WorkOrderStatus.SUBMITTED) {
            throw new IllegalStateException("Work order is not open for editing");
        }
    }

    private static WorkOrder loadOpenWorkOrderManaged(EntityManager em, Long userId) {
        TypedQuery<WorkOrder> q = em.createQuery(
                "SELECT w FROM WorkOrder w WHERE w.user.id = :uid AND w.status IN :st",
                WorkOrder.class);
        q.setParameter("uid", userId);
        q.setParameter("st", List.of(WorkOrderStatus.DRAFT, WorkOrderStatus.SUBMITTED));
        List<WorkOrder> found = q.getResultList();
        if (found.isEmpty()) {
            throw new IllegalStateException("No open work order for user");
        }
        return found.get(0);
    }
}
