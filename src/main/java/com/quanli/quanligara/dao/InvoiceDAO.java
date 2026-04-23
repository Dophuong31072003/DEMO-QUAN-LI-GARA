package com.quanli.quanligara.dao;

import com.quanli.quanligara.model.Invoice;
import com.quanli.quanligara.model.User;
import com.quanli.quanligara.model.WorkOrder;
import com.quanli.quanligara.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class InvoiceDAO {

    public Optional<Invoice> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            Invoice invoice = em.find(Invoice.class, id);
            return Optional.ofNullable(invoice);
        } finally {
            em.close();
        }
    }

    public Optional<Invoice> findByInvoiceNumber(String invoiceNumber) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Invoice> query = em.createQuery(
                    "SELECT i FROM Invoice i WHERE i.invoiceNumber = :invoiceNumber", Invoice.class);
            query.setParameter("invoiceNumber", invoiceNumber);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public Optional<Invoice> findByWorkOrder(WorkOrder workOrder) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Invoice> query = em.createQuery(
                    "SELECT i FROM Invoice i WHERE i.workOrder = :workOrder", Invoice.class);
            query.setParameter("workOrder", workOrder);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public List<Invoice> findByUser(User user) {
        if (user == null || user.getId() == null) {
            return List.of();
        }
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Invoice> query = em.createQuery(
                    "SELECT i FROM Invoice i WHERE i.user.id = :uid ORDER BY i.issuedAt DESC", Invoice.class);
            query.setParameter("uid", user.getId());
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Invoice> findAll() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Invoice> query = em.createQuery(
                    "SELECT DISTINCT i FROM Invoice i LEFT JOIN FETCH i.user ORDER BY i.issuedAt DESC",
                    Invoice.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Invoices whose customer (invoice user) matches name or username.
     */
    public List<Invoice> findByCustomerNameLike(String rawKeyword) {
        String q = rawKeyword == null ? "" : rawKeyword.trim();
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            String pattern = "%" + q.toUpperCase() + "%";
            TypedQuery<Invoice> query = em.createQuery(
                    "SELECT DISTINCT i FROM Invoice i JOIN FETCH i.user u "
                            + "WHERE UPPER(COALESCE(u.fullName, '')) LIKE :p "
                            + "OR UPPER(u.username) LIKE :p "
                            + "ORDER BY i.issuedAt DESC",
                    Invoice.class);
            query.setParameter("p", pattern);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void save(Invoice invoice) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(invoice);
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

    public boolean existsByInvoiceNumber(String invoiceNumber) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(i) FROM Invoice i WHERE i.invoiceNumber = :invoiceNumber", Long.class)
                    .setParameter("invoiceNumber", invoiceNumber)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
}