package com.quanli.quanligara.controller.home;

import com.quanli.quanligara.service.CatalogService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "homePartsServlet", value = "/home/parts")
public class HomePartsServlet extends HttpServlet {

    private CatalogService catalogService;

    @Override
    public void init() {
        catalogService = new CatalogService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String q = request.getParameter("q");
        request.setAttribute("parts", catalogService.searchActiveParts(q == null ? "" : q));
        request.setAttribute("q", q == null ? "" : q);
        request.getRequestDispatcher("/WEB-INF/views/user/parts.jsp").forward(request, response);
    }
}
