package com.quanli.quanligara.dao;

import com.quanli.quanligara.model.Invoice;
import com.quanli.quanligara.model.InvoiceLine;
import com.quanli.quanligara.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class InvoiceLineDAO {

    public void save(InvoiceLine invoiceLine) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(invoiceLine);
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

    public List<InvoiceLine> findByInvoice(Invoice invoice) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<InvoiceLine> query = em.createQuery(
                    "SELECT il FROM InvoiceLine il WHERE il.invoice = :invoice ORDER BY il.id", InvoiceLine.class);
            query.setParameter("invoice", invoice);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}