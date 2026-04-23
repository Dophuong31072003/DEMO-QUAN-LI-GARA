package com.quanli.quanligara.dao;

import com.quanli.quanligara.model.ServiceOffering;
import com.quanli.quanligara.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class ServiceOfferingDAO {

    public Optional<ServiceOffering> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            ServiceOffering offering = em.find(ServiceOffering.class, id);
            return Optional.ofNullable(offering);
        } finally {
            em.close();
        }
    }

    public Optional<ServiceOffering> findByCode(String code) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ServiceOffering> query = em.createQuery(
                    "SELECT so FROM ServiceOffering so WHERE so.code = :code", ServiceOffering.class);
            query.setParameter("code", code);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public List<ServiceOffering> findActiveByKeyword(String q) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ServiceOffering> query = em.createQuery(
                    "SELECT so FROM ServiceOffering so WHERE so.isActive = true "
                            + "AND (UPPER(so.code) LIKE UPPER(:keyword) "
                            + "OR UPPER(so.name) LIKE UPPER(:keyword))", ServiceOffering.class);
            query.setParameter("keyword", "%" + q + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<ServiceOffering> findAllForAdmin() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<ServiceOffering> query = em.createQuery(
                    "SELECT so FROM ServiceOffering so", ServiceOffering.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void save(ServiceOffering serviceOffering) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(serviceOffering);
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

    public void update(ServiceOffering serviceOffering) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(serviceOffering);
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

    public void deactivate(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            ServiceOffering offering = em.find(ServiceOffering.class, id);
            if (offering != null) {
                offering.setActive(false);
                em.merge(offering);
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

    public void delete(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            ServiceOffering offering = em.find(ServiceOffering.class, id);
            if (offering != null) {
                em.remove(offering);
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

    public boolean existsByCode(String code) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(so) FROM ServiceOffering so WHERE so.code = :code", Long.class)
                    .setParameter("code", code)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public boolean existsByCodeExcludingId(String code, Long excludeId) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(so) FROM ServiceOffering so WHERE so.code = :code AND so.id <> :excludeId",
                            Long.class)
                    .setParameter("code", code)
                    .setParameter("excludeId", excludeId)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
}