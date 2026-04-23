package com.quanli.quanligara.controller.admin;

import com.quanli.quanligara.model.ServiceOffering;
import com.quanli.quanligara.service.CatalogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet(name = "adminServicesUpdateServlet", value = "/admin/services/update")
public class AdminServicesUpdateServlet extends HttpServlet {

    private CatalogService catalogService;

    @Override
    public void init() {
        catalogService = new CatalogService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            ServiceOffering s = catalogService.loadServiceOfferingForEdit(id)
                    .orElseThrow(() -> new IllegalArgumentException("Service not found"));
            if (request.getParameter("code") != null) {
                s.setCode(request.getParameter("code").trim());
            }
            if (request.getParameter("name") != null) {
                s.setName(request.getParameter("name").trim());
            }
            s.setDescription(request.getParameter("description") == null ? "" : request.getParameter("description").trim());
            if (request.getParameter("unitPrice") != null && !request.getParameter("unitPrice").isBlank()) {
                s.setUnitPrice(new BigDecimal(request.getParameter("unitPrice").trim()));
            }
            catalogService.updateServiceOffering(s);
            request.getSession().setAttribute("flashMessage", "Service updated.");
        } catch (Exception e) {
            request.getSession().setAttribute("flashError", e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/admin/services");
    }
}
