package com.quanli.quanligara;

import com.quanli.quanligara.model.User;
import com.quanli.quanligara.util.JpaUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bootstrap test that verifies the H2-based test persistence unit
 * and JpaUtil can persist and retrieve entities.
 */
class JpaModuleBootstrapTest {

    @BeforeAll
    static void setUp() {
        System.setProperty("pu.name", "test-unit");
    }

    @Test
    void persistAndFindUser() {
        User user = new User("testuser", "secret", "Test User",
                "test@example.com", "0900000000", false);

        JpaUtil.inTransaction(em -> em.persist(user));

        JpaUtil.inTransaction(em -> {
            User found = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", "testuser")
                    .getSingleResult();

            assertNotNull(found.getId(), "Persisted user must have an ID");
            assertEquals("testuser", found.getUsername());
            assertEquals("Test User", found.getFullName());
        });
    }

    @Test
    void transactionRollbackOnError() {
        assertThrows(RuntimeException.class, () ->
                JpaUtil.inTransaction(em -> {
                    User user = new User("rollbackuser", "pass", "R",
                            "r@x.com", "000", false);
                    em.persist(user);
                    throw new RuntimeException("forced failure");
                })
        );

        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            long count = em.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                    .setParameter("username", "rollbackuser")
                    .getSingleResult();
            assertEquals(0, count, "Rolled-back entity must not be persisted");
        } finally {
            em.close();
        }
    }
}