package com.quanli.quanligara.dao;

import com.quanli.quanligara.model.SparePart;
import com.quanli.quanligara.util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class SparePartDAO {

    public Optional<SparePart> findById(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            SparePart part = em.find(SparePart.class, id);
            return Optional.ofNullable(part);
        } finally {
            em.close();
        }
    }

    public Optional<SparePart> findByCode(String code) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<SparePart> query = em.createQuery(
                    "SELECT sp FROM SparePart sp WHERE sp.code = :code", SparePart.class);
            query.setParameter("code", code);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public Optional<SparePart> findByIdForUpdate(Long id) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<SparePart> query = em.createQuery(
                    "SELECT sp FROM SparePart sp WHERE sp.id = :id", SparePart.class);
            query.setParameter("id", id);
            query.setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
            SparePart part = query.getResultList().stream().findFirst().orElse(null);
            em.getTransaction().commit();
            return Optional.ofNullable(part);
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<SparePart> findActiveByKeyword(String q) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<SparePart> query = em.createQuery(
                    "SELECT sp FROM SparePart sp WHERE sp.isActive = true "
                            + "AND (UPPER(sp.code) LIKE UPPER(:keyword) "
                            + "OR UPPER(sp.name) LIKE UPPER(:keyword))", SparePart.class);
            query.setParameter("keyword", "%" + q + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<SparePart> findAllForAdmin() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<SparePart> query = em.createQuery(
                    "SELECT sp FROM SparePart sp", SparePart.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void save(SparePart sparePart) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(sparePart);
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

    public void update(SparePart sparePart) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(sparePart);
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
            SparePart part = em.find(SparePart.class, id);
            if (part != null) {
                part.setActive(false);
                em.merge(part);
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
            SparePart part = em.find(SparePart.class, id);
            if (part != null) {
                em.remove(part);
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
                            "SELECT COUNT(sp) FROM SparePart sp WHERE sp.code = :code", Long.class)
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
                            "SELECT COUNT(sp) FROM SparePart sp WHERE sp.code = :code AND sp.id <> :excludeId",
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