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

@WebServlet(name = "adminServicesServlet", value = "/admin/services")
public class AdminServicesServlet extends HttpServlet {

    private CatalogService catalogService;

    @Override
    public void init() {
        catalogService = new CatalogService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Object fm = request.getSession().getAttribute("flashMessage");
        if (fm != null) {
            request.setAttribute("message", fm);
            request.getSession().removeAttribute("flashMessage");
        }
        Object fe = request.getSession().getAttribute("flashError");
        if (fe != null) {
            request.setAttribute("error", fe);
            request.getSession().removeAttribute("flashError");
        }
        request.setAttribute("services", catalogService.listAllServiceOfferings());
        request.getRequestDispatcher("/WEB-INF/views/admin/services.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            ServiceOffering s = new ServiceOffering();
            s.setCode(request.getParameter("code") == null ? "" : request.getParameter("code").trim());
            s.setName(request.getParameter("name") == null ? "" : request.getParameter("name").trim());
            s.setDescription(request.getParameter("description") == null ? "" : request.getParameter("description").trim());
            s.setUnitPrice(new BigDecimal(request.getParameter("unitPrice").trim()));
            catalogService.createServiceOffering(s);
            request.setAttribute("message", "Service created.");
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
        }
        doGet(request, response);
    }
}
