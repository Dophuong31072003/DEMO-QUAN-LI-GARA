package com.quanli.quanligara.controller.admin;

import com.quanli.quanligara.model.SparePart;
import com.quanli.quanligara.service.CatalogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet(name = "adminPartsUpdateServlet", value = "/admin/parts/update")
public class AdminPartsUpdateServlet extends HttpServlet {

    private CatalogService catalogService;

    @Override
    public void init() {
        catalogService = new CatalogService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            SparePart p = catalogService.loadSparePartForEdit(id)
                    .orElseThrow(() -> new IllegalArgumentException("Part not found"));
            p.setCode(request.getParameter("code") == null ? p.getCode() : request.getParameter("code").trim());
            p.setName(request.getParameter("name") == null ? p.getName() : request.getParameter("name").trim());
            p.setDescription(request.getParameter("description") == null ? "" : request.getParameter("description").trim());
            p.setUnitName(request.getParameter("unitName") == null ? p.getUnitName() : request.getParameter("unitName").trim());
            if (request.getParameter("unitPrice") != null && !request.getParameter("unitPrice").isBlank()) {
                p.setUnitPrice(new BigDecimal(request.getParameter("unitPrice").trim()));
            }
            if (request.getParameter("stockQuantity") != null && !request.getParameter("stockQuantity").isBlank()) {
                p.setStockQuantity(Integer.parseInt(request.getParameter("stockQuantity").trim()));
            }
            catalogService.updateSparePart(p);
            request.getSession().setAttribute("flashMessage", "Part updated.");
        } catch (Exception e) {
            request.getSession().setAttribute("flashError", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/admin/parts");
    }
}
