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

@WebServlet(name = "adminPartsServlet", value = "/admin/parts")
public class AdminPartsServlet extends HttpServlet {

    private CatalogService catalogService;

    @Override
    public void init() {
        catalogService = new CatalogService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        copyFlash(request, "flashMessage", "message");
        copyFlash(request, "flashError", "error");
        request.setAttribute("parts", catalogService.listAllSpareParts());
        request.getRequestDispatcher("/WEB-INF/views/admin/parts.jsp").forward(request, response);
    }

    private static void copyFlash(HttpServletRequest request, String sessionKey, String requestKey) {
        Object v = request.getSession().getAttribute(sessionKey);
        if (v != null) {
            request.setAttribute(requestKey, v);
            request.getSession().removeAttribute(sessionKey);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            SparePart p = new SparePart();
            p.setCode(trimToEmpty(request.getParameter("code")));
            p.setName(trimToEmpty(request.getParameter("name")));
            p.setDescription(request.getParameter("description") == null ? "" : request.getParameter("description").trim());
            p.setUnitName(trimToEmpty(request.getParameter("unitName")));
            p.setUnitPrice(new BigDecimal(trimToEmpty(request.getParameter("unitPrice"))));
            p.setStockQuantity(Integer.parseInt(trimToEmpty(request.getParameter("stockQuantity"))));
            catalogService.createSparePart(p);
            request.setAttribute("message", "Part created.");
        } catch (Exception e) {
            request.setAttribute("error", e.getMessage());
        }
        doGet(request, response);
    }

    private static String trimToEmpty(String s) {
        return s == null ? "" : s.trim();
    }
}
