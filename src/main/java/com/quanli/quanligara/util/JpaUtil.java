package com.quanli.quanligara.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Shared JPA utility providing a singleton EntityManagerFactory
 * and a transactional execution helper.
 *
 * <p>For tests, set the system property {@code pu.name} to the
 * test persistence-unit name (e.g. {@code "test-unit"}).
 * Defaults to {@code "default"} for runtime.</p>
 */
public final class JpaUtil {

    private static final String PU_NAME = System.getProperty("pu.name", "default");

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory(PU_NAME);

    private JpaUtil() {
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return EMF;
    }

    /**
     * Executes a callback inside a single transaction.
     * Commits on success; rolls back on any exception, then rethrows.
     *
     * @param work the callback receiving an active EntityManager with a live transaction
     */
    public static void inTransaction(Consumer<EntityManager> work) {
        EntityManager em = EMF.createEntityManager();
        try {
            em.getTransaction().begin();
            work.accept(em);
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

    /**
     * Executes a callback inside a single transaction and returns a result.
     * Commits on success; rolls back on any exception, then rethrows.
     *
     * @param work the callback receiving an active EntityManager with a live transaction
     * @param <T> the return type
     * @return the result from the callback
     */
    public static <T> T inTransactionWithResult(Function<EntityManager, T> work) {
        EntityManager em = EMF.createEntityManager();
        try {
            em.getTransaction().begin();
            T result = work.apply(em);
            em.getTransaction().commit();
            return result;
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