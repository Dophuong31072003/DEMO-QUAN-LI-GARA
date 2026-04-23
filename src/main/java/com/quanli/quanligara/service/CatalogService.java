package com.quanli.quanligara.service;

import com.quanli.quanligara.dao.SparePartDAO;
import com.quanli.quanligara.dao.ServiceOfferingDAO;
import com.quanli.quanligara.model.SparePart;
import com.quanli.quanligara.model.ServiceOffering;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CatalogService {

    private final SparePartDAO sparePartDAO;
    private final ServiceOfferingDAO serviceOfferingDAO;

    public CatalogService() {
        this.sparePartDAO = new SparePartDAO();
        this.serviceOfferingDAO = new ServiceOfferingDAO();
    }

    // -- Admin: Spare Parts --

    public void createSparePart(SparePart part) {
        validateSparePart(part, true);
        sparePartDAO.save(part);
    }

    public void updateSparePart(SparePart part) {
        if (part.getId() == null) {
            throw new IllegalArgumentException("Part ID is required for update");
        }
        validateSparePart(part, false);
        sparePartDAO.update(part);
    }

    public void deactivateSparePart(Long id) {
        sparePartDAO.deactivate(id);
    }

    public List<SparePart> listAllSpareParts() {
        return sparePartDAO.findAllForAdmin();
    }

    public Optional<SparePart> loadSparePartForEdit(Long id) {
        return sparePartDAO.findById(id);
    }

    public Optional<SparePart> findSparePartById(Long id) {
        return sparePartDAO.findById(id);
    }

    // -- Admin: Service Offerings --

    public void createServiceOffering(ServiceOffering offering) {
        validateServiceOffering(offering, true);
        serviceOfferingDAO.save(offering);
    }

    public void updateServiceOffering(ServiceOffering offering) {
        if (offering.getId() == null) {
            throw new IllegalArgumentException("Service ID is required for update");
        }
        validateServiceOffering(offering, false);
        serviceOfferingDAO.update(offering);
    }

    public void deactivateServiceOffering(Long id) {
        serviceOfferingDAO.deactivate(id);
    }

    public List<ServiceOffering> listAllServiceOfferings() {
        return serviceOfferingDAO.findAllForAdmin();
    }

    public Optional<ServiceOffering> loadServiceOfferingForEdit(Long id) {
        return serviceOfferingDAO.findById(id);
    }

    public Optional<ServiceOffering> findServiceOfferingById(Long id) {
        return serviceOfferingDAO.findById(id);
    }

    // -- User: Search --

    public List<SparePart> searchActiveParts(String keyword) {
        String q = (keyword == null || keyword.trim().isEmpty()) ? "" : keyword.trim();
        return sparePartDAO.findActiveByKeyword(q);
    }

    public List<ServiceOffering> searchActiveServices(String keyword) {
        String q = (keyword == null || keyword.trim().isEmpty()) ? "" : keyword.trim();
        return serviceOfferingDAO.findActiveByKeyword(q);
    }

    // -- Validation --

    private void validateSparePart(SparePart part, boolean checkDuplicateCode) {
        if (part == null) {
            throw new IllegalArgumentException("Spare part must not be null");
        }
        if (part.getCode() == null || part.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Part code is required");
        }
        part.setCode(part.getCode().trim());
        if (part.getName() == null || part.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Part name is required");
        }
        part.setName(part.getName().trim());
        if (part.getUnitPrice() == null || part.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Part price must be positive");
        }
        if (part.getStockQuantity() == null || part.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Part stock must be non-negative");
        }
        if (part.getDescription() == null) {
            part.setDescription("");
        }
        if (checkDuplicateCode) {
            if (sparePartDAO.existsByCode(part.getCode())) {
                throw new IllegalArgumentException("Part code already exists: " + part.getCode());
            }
        } else if (part.getId() != null && sparePartDAO.existsByCodeExcludingId(part.getCode(), part.getId())) {
            throw new IllegalArgumentException("Part code already exists: " + part.getCode());
        }
    }

    private void validateServiceOffering(ServiceOffering offering, boolean checkDuplicateCode) {
        if (offering == null) {
            throw new IllegalArgumentException("Service offering must not be null");
        }
        if (offering.getCode() == null || offering.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Service code is required");
        }
        offering.setCode(offering.getCode().trim());
        if (offering.getName() == null || offering.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Service name is required");
        }
        offering.setName(offering.getName().trim());
        if (offering.getUnitPrice() == null || offering.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Service price must be positive");
        }
        if (offering.getDescription() == null) {
            offering.setDescription("");
        }
        if (checkDuplicateCode) {
            if (serviceOfferingDAO.existsByCode(offering.getCode())) {
                throw new IllegalArgumentException("Service code already exists: " + offering.getCode());
            }
        } else if (offering.getId() != null
                && serviceOfferingDAO.existsByCodeExcludingId(offering.getCode(), offering.getId())) {
            throw new IllegalArgumentException("Service code already exists: " + offering.getCode());
        }
    }
}
