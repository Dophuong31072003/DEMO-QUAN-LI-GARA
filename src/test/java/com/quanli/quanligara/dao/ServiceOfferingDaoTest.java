package com.quanli.quanligara.dao;

import com.quanli.quanligara.model.ServiceOffering;
import com.quanli.quanligara.util.JpaUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ServiceOfferingDaoTest {

    private static ServiceOfferingDAO dao;

    @BeforeAll
    static void setUp() {
        System.setProperty("pu.name", "test-unit");
        dao = new ServiceOfferingDAO();
    }

    @BeforeEach
    void cleanData() {
        JpaUtil.inTransaction(em -> em.createQuery("DELETE FROM ServiceOffering").executeUpdate());
    }

    private ServiceOffering createOffering(String code, String name, BigDecimal price) {
        ServiceOffering offering = new ServiceOffering();
        offering.setCode(code);
        offering.setName(name);
        offering.setUnitPrice(price);
        offering.setActive(true);
        return offering;
    }

    @Test
    void save_andFindById() {
        ServiceOffering offering = createOffering("OIL-CHG", "Oil Change", new BigDecimal("350000"));
        dao.save(offering);

        Optional<ServiceOffering> found = dao.findById(offering.getId());
        assertTrue(found.isPresent());
        assertEquals("OIL-CHG", found.get().getCode());
        assertEquals("Oil Change", found.get().getName());
    }

    @Test
    void findByCode_returnsOffering() {
        ServiceOffering offering = createOffering("BRK-REP", "Brake Repair", new BigDecimal("800000"));
        dao.save(offering);

        Optional<ServiceOffering> found = dao.findByCode("BRK-REP");
        assertTrue(found.isPresent());
        assertEquals("Brake Repair", found.get().getName());
    }

    @Test
    void findByCode_notFound_returnsEmpty() {
        Optional<ServiceOffering> found = dao.findByCode("NONEXISTENT");
        assertFalse(found.isPresent());
    }

    @Test
    void findActiveByKeyword_matchesCode() {
        dao.save(createOffering("OIL-CHG", "Oil Change", new BigDecimal("350000")));
        dao.save(createOffering("BRK-REP", "Brake Repair", new BigDecimal("800000")));

        List<ServiceOffering> results = dao.findActiveByKeyword("OIL");
        assertEquals(1, results.size());
        assertEquals("OIL-CHG", results.get(0).getCode());
    }

    @Test
    void findActiveByKeyword_matchesName() {
        dao.save(createOffering("OIL-CHG", "Oil Change", new BigDecimal("350000")));
        dao.save(createOffering("BRK-REP", "Brake Repair", new BigDecimal("800000")));

        List<ServiceOffering> results = dao.findActiveByKeyword("brake");
        assertEquals(1, results.size());
        assertEquals("BRK-REP", results.get(0).getCode());
    }

    @Test
    void findActiveByKeyword_isCaseInsensitive() {
        dao.save(createOffering("OIL-CHG", "Oil Change", new BigDecimal("350000")));

        List<ServiceOffering> results = dao.findActiveByKeyword("oil");
        assertEquals(1, results.size());

        List<ServiceOffering> results2 = dao.findActiveByKeyword("OIL");
        assertEquals(1, results2.size());
    }

    @Test
    void findActiveByKeyword_excludesInactive() {
        ServiceOffering active = createOffering("ACT-01", "Active Service", new BigDecimal("100000"));
        dao.save(active);

        ServiceOffering inactive = createOffering("INA-01", "Inactive Service", new BigDecimal("200000"));
        dao.save(inactive);
        dao.deactivate(inactive.getId());

        List<ServiceOffering> results = dao.findActiveByKeyword("Service");
        assertEquals(1, results.size());
        assertEquals("ACT-01", results.get(0).getCode());
    }

    @Test
    void findAllForAdmin_returnsAll() {
        ServiceOffering active = createOffering("ACT-01", "Active Service", new BigDecimal("100000"));
        dao.save(active);

        ServiceOffering inactive = createOffering("INA-01", "Inactive Service", new BigDecimal("200000"));
        dao.save(inactive);
        dao.deactivate(inactive.getId());

        List<ServiceOffering> all = dao.findAllForAdmin();
        assertEquals(2, all.size());
    }

    @Test
    void update_changesData() {
        ServiceOffering offering = createOffering("OIL-CHG", "Oil Change", new BigDecimal("350000"));
        dao.save(offering);

        offering.setName("Full Oil Change");
        offering.setUnitPrice(new BigDecimal("400000"));
        dao.update(offering);

        Optional<ServiceOffering> found = dao.findById(offering.getId());
        assertTrue(found.isPresent());
        assertEquals("Full Oil Change", found.get().getName());
        assertTrue(found.get().getUnitPrice().compareTo(new BigDecimal("400000")) == 0,
                "Expected 400000 but got " + found.get().getUnitPrice());
    }

    @Test
    void deactivate_setsInactive() {
        ServiceOffering offering = createOffering("OIL-CHG", "Oil Change", new BigDecimal("350000"));
        dao.save(offering);

        dao.deactivate(offering.getId());

        Optional<ServiceOffering> found = dao.findById(offering.getId());
        assertTrue(found.isPresent());
        assertFalse(found.get().isActive());
    }

    @Test
    void deactivate_nonExistent_doesNotThrow() {
        assertDoesNotThrow(() -> dao.deactivate(99999L));
    }

    @Test
    void existsByCode_returnsTrueWhenExists() {
        dao.save(createOffering("OIL-CHG", "Oil Change", new BigDecimal("350000")));

        assertTrue(dao.existsByCode("OIL-CHG"));
    }

    @Test
    void existsByCode_returnsFalseWhenNotExists() {
        assertFalse(dao.existsByCode("NONEXISTENT"));
    }
}