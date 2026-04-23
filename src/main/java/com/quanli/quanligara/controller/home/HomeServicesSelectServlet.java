package com.quanli.quanligara.controller.home;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Legacy endpoint — customer quotations are managed by staff under /admin.
 */
@WebServlet(name = "homeServicesSelectServlet", value = "/home/services/select")
public class HomeServicesSelectServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not available for customers.");
    }
}
