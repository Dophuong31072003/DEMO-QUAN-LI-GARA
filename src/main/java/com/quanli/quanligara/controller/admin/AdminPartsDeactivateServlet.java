package com.quanli.quanligara.controller.admin;

import com.quanli.quanligara.service.CatalogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "adminPartsDeactivateServlet", value = "/admin/parts/deactivate")
public class AdminPartsDeactivateServlet extends HttpServlet {

    private CatalogService catalogService;

    @Override
    public void init() {
        catalogService = new CatalogService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Long id = Long.parseLong(request.getParameter("id"));
            catalogService.deleteSparePart(id);
            request.getSession().setAttribute("flashMessage", "Part deleted.");
        } catch (Exception e) {
            request.getSession().setAttribute("flashError",
                    "Cannot delete part (it may be referenced by a work order or invoice). " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/admin/parts");
    }
}
