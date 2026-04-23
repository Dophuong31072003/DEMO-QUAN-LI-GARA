package com.quanli.quanligara.dao;

import com.quanli.quanligara.model.SparePart;
import com.quanli.quanligara.util.JpaUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SparePartDaoTest {

    private static SparePartDAO dao;

    @BeforeAll
    static void setUp() {
        System.setProperty("pu.name", "test-unit");
        dao = new SparePartDAO();
    }

    @BeforeEach
    void cleanData() {
        JpaUtil.inTransaction(em -> em.createQuery("DELETE FROM SparePart").executeUpdate());
    }

    private SparePart createPart(String code, String name, BigDecimal price, int qty) {
        SparePart part = new SparePart();
        part.setCode(code);
        part.setName(name);
        part.setUnitPrice(price);
        part.setStockQuantity(qty);
        part.setActive(true);
        return part;
    }

    @Test
    void save_andFindById() {
        SparePart part = createPart("OIL-01", "Engine Oil 5W-30", new BigDecimal("250000"), 100);
        dao.save(part);

        Optional<SparePart> found = dao.findById(part.getId());
        assertTrue(found.isPresent());
        assertEquals("OIL-01", found.get().getCode());
        assertEquals("Engine Oil 5W-30", found.get().getName());
    }

    @Test
    void findByCode_returnsPart() {
        SparePart part = createPart("BRK-01", "Brake Pad Front", new BigDecimal("450000"), 50);
        dao.save(part);

        Optional<SparePart> found = dao.findByCode("BRK-01");
        assertTrue(found.isPresent());
        assertEquals("Brake Pad Front", found.get().getName());
    }

    @Test
    void findByCode_notFound_returnsEmpty() {
        Optional<SparePart> found = dao.findByCode("NONEXISTENT");
        assertFalse(found.isPresent());
    }

    @Test
    void findByIdForUpdate_returnsPart() {
        SparePart part = createPart("FLT-01", "Oil Filter", new BigDecimal("85000"), 200);
        dao.save(part);

        Optional<SparePart> found = dao.findByIdForUpdate(part.getId());
        assertTrue(found.isPresent());
        assertEquals("FLT-01", found.get().getCode());
    }

    @Test
    void findActiveByKeyword_matchesCode() {
        dao.save(createPart("OIL-01", "Engine Oil", new BigDecimal("250000"), 100));
        dao.save(createPart("BRK-01", "Brake Pad", new BigDecimal("450000"), 50));

        List<SparePart> results = dao.findActiveByKeyword("OIL");
        assertEquals(1, results.size());
        assertEquals("OIL-01", results.get(0).getCode());
    }

    @Test
    void findActiveByKeyword_matchesName() {
        dao.save(createPart("OIL-01", "Engine Oil", new BigDecimal("250000"), 100));
        dao.save(createPart("BRK-01", "Brake Pad", new BigDecimal("450000"), 50));

        List<SparePart> results = dao.findActiveByKeyword("brake");
        assertEquals(1, results.size());
        assertEquals("BRK-01", results.get(0).getCode());
    }

    @Test
    void findActiveByKeyword_isCaseInsensitive() {
        dao.save(createPart("OIL-01", "Engine Oil", new BigDecimal("250000"), 100));

        List<SparePart> results = dao.findActiveByKeyword("oil");
        assertEquals(1, results.size());

        List<SparePart> results2 = dao.findActiveByKeyword("OIL");
        assertEquals(1, results2.size());
    }

    @Test
    void findActiveByKeyword_excludesInactive() {
        SparePart active = createPart("ACT-01", "Active Part", new BigDecimal("100000"), 10);
        dao.save(active);

        SparePart inactive = createPart("INA-01", "Inactive Part", new BigDecimal("200000"), 5);
        dao.save(inactive);
        dao.deactivate(inactive.getId());

        List<SparePart> results = dao.findActiveByKeyword("Part");
        assertEquals(1, results.size());
        assertEquals("ACT-01", results.get(0).getCode());
    }

    @Test
    void findAllForAdmin_returnsAll() {
        SparePart active = createPart("ACT-01", "Active Part", new BigDecimal("100000"), 10);
        dao.save(active);

        SparePart inactive = createPart("INA-01", "Inactive Part", new BigDecimal("200000"), 5);
        dao.save(inactive);
        dao.deactivate(inactive.getId());

        List<SparePart> all = dao.findAllForAdmin();
        assertEquals(2, all.size());
    }

    @Test
    void update_changesData() {
        SparePart part = createPart("OIL-01", "Engine Oil", new BigDecimal("250000"), 100);
        dao.save(part);

        part.setName("Engine Oil 5W-30");
        part.setUnitPrice(new BigDecimal("260000"));
        dao.update(part);

        Optional<SparePart> found = dao.findById(part.getId());
        assertTrue(found.isPresent());
        assertEquals("Engine Oil 5W-30", found.get().getName());
        assertTrue(found.get().getUnitPrice().compareTo(new BigDecimal("260000")) == 0,
                "Expected 260000 but got " + found.get().getUnitPrice());
    }

    @Test
    void deactivate_setsInactive() {
        SparePart part = createPart("OIL-01", "Engine Oil", new BigDecimal("250000"), 100);
        dao.save(part);

        dao.deactivate(part.getId());

        Optional<SparePart> found = dao.findById(part.getId());
        assertTrue(found.isPresent());
        assertFalse(found.get().isActive());
    }

    @Test
    void deactivate_nonExistent_doesNotThrow() {
        assertDoesNotThrow(() -> dao.deactivate(99999L));
    }

    @Test
    void existsByCode_returnsTrueWhenExists() {
        dao.save(createPart("OIL-01", "Engine Oil", new BigDecimal("250000"), 100));

        assertTrue(dao.existsByCode("OIL-01"));
    }

    @Test
    void existsByCode_returnsFalseWhenNotExists() {
        assertFalse(dao.existsByCode("NONEXISTENT"));
    }
}