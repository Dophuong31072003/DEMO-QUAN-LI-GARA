package com.quanli.quanligara.dao;

import com.quanli.quanligara.model.User;
import com.quanli.quanligara.model.WorkOrder;
import com.quanli.quanligara.model.WorkOrderPartLine;
import com.quanli.quanligara.model.WorkOrderServiceLine;
import com.quanli.quanligara.model.enums.WorkOrderStatus;
import com.quanli.quanligara.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class WorkOrderDAO {

    private static final EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

    public Optional<WorkOrder> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            WorkOrder wo = em.find(WorkOrder.class, id);
            return Optional.ofNullable(wo);
        } finally {
            em.close();
        }
    }

    /**
     * Returns the single DRAFT or SUBMITTED work order for a user,
     * or Optional.empty() if none exists.
     */
    public Optional<WorkOrder> findOpenByUser(User user) {
        if (user == null || user.getId() == null) {
            return Optional.empty();
        }
        return findOpenByUserId(user.getId());
    }

    public Optional<WorkOrder> findOpenByUserId(Long userId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<WorkOrder> query = em.createQuery(
                    "SELECT w FROM WorkOrder w WHERE w.user.id = :uid AND w.status IN :statuses ORDER BY w.id ASC",
                    WorkOrder.class);
            query.setParameter("uid", userId);
            query.setParameter("statuses", List.of(WorkOrderStatus.DRAFT, WorkOrderStatus.SUBMITTED));
            List<WorkOrder> list = query.getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } finally {
            em.close();
        }
    }

    public List<WorkOrder> findAllOpen() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<WorkOrder> query = em.createQuery(
                    "SELECT w FROM WorkOrder w WHERE w.status IN :statuses ORDER BY w.id ASC",
                    WorkOrder.class);
            query.setParameter("statuses", List.of(WorkOrderStatus.DRAFT, WorkOrderStatus.SUBMITTED));
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Open work orders with {@link User} initialized for list screens (avoids lazy load after EM closes).
     */
    public List<WorkOrder> findAllOpenWithUser() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<WorkOrder> query = em.createQuery(
                    "SELECT w FROM WorkOrder w JOIN FETCH w.user WHERE w.status IN :statuses ORDER BY w.id ASC",
                    WorkOrder.class);
            query.setParameter("statuses", List.of(WorkOrderStatus.DRAFT, WorkOrderStatus.SUBMITTED));
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Eager-fetches a work order together with its part lines and service lines.
     * Uses two separate queries since JPA does not allow multiple JOIN FETCH
     * on bags (Lists) in a single query.
     */
    public Optional<WorkOrder> findWithLines(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            WorkOrder wo = em.find(WorkOrder.class, id);
            if (wo == null) {
                return Optional.empty();
            }
            // Force initialization of both collections inside the EM session
            em.createQuery(
                    "SELECT w FROM WorkOrder w " +
                            "LEFT JOIN FETCH w.partLines pl " +
                            "LEFT JOIN FETCH pl.sparePart " +
                            "WHERE w.id = :id",
                    WorkOrder.class)
                    .setParameter("id", id)
                    .getSingleResult();
            em.createQuery(
                    "SELECT w FROM WorkOrder w " +
                            "LEFT JOIN FETCH w.serviceLines sl " +
                            "LEFT JOIN FETCH sl.serviceOffering " +
                            "WHERE w.id = :id",
                    WorkOrder.class)
                    .setParameter("id", id)
                    .getSingleResult();
            return Optional.of(wo);
        } finally {
            em.close();
        }
    }

    public List<WorkOrder> findAllByUser(User user) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<WorkOrder> query = em.createQuery(
                    "SELECT w FROM WorkOrder w WHERE w.user = :user ORDER BY w.id DESC",
                    WorkOrder.class);
            query.setParameter("user", user);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void save(WorkOrder workOrder) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(workOrder);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void update(WorkOrder workOrder) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(workOrder);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void addPartLine(WorkOrderPartLine partLine) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(partLine);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void updatePartLineQuantity(Long partLineId, int quantity) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            WorkOrderPartLine line = em.find(WorkOrderPartLine.class, partLineId);
            if (line != null) {
                line.setQuantity(quantity);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void removePartLine(Long partLineId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            WorkOrderPartLine line = em.find(WorkOrderPartLine.class, partLineId);
            if (line != null) {
                em.remove(line);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void addServiceLine(WorkOrderServiceLine serviceLine) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(serviceLine);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void updateServiceLineQuantity(Long serviceLineId, int quantity) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            WorkOrderServiceLine line = em.find(WorkOrderServiceLine.class, serviceLineId);
            if (line != null) {
                line.setQuantity(quantity);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void removeServiceLine(Long serviceLineId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            WorkOrderServiceLine line = em.find(WorkOrderServiceLine.class, serviceLineId);
            if (line != null) {
                em.remove(line);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void markSubmitted(Long id, LocalDateTime submittedAt) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            WorkOrder wo = em.find(WorkOrder.class, id);
            if (wo != null) {
                wo.setStatus(WorkOrderStatus.SUBMITTED);
                wo.setSubmittedAt(submittedAt);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public int countPartLines(Long workOrderId) {
        EntityManager em = emf.createEntityManager();
        try {
            Long c = em.createQuery(
                            "SELECT COUNT(pl) FROM WorkOrderPartLine pl WHERE pl.workOrder.id = :id", Long.class)
                    .setParameter("id", workOrderId)
                    .getSingleResult();
            return c != null ? c.intValue() : 0;
        } finally {
            em.close();
        }
    }

    public int countServiceLines(Long workOrderId) {
        EntityManager em = emf.createEntityManager();
        try {
            Long c = em.createQuery(
                            "SELECT COUNT(sl) FROM WorkOrderServiceLine sl WHERE sl.workOrder.id = :id", Long.class)
                    .setParameter("id", workOrderId)
                    .getSingleResult();
            return c != null ? c.intValue() : 0;
        } finally {
            em.close();
        }
    }

    public void markInvoiced(Long id, LocalDateTime invoicedAt) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            WorkOrder wo = em.find(WorkOrder.class, id);
            if (wo != null) {
                wo.setStatus(WorkOrderStatus.INVOICED);
                wo.setInvoicedAt(invoicedAt);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}