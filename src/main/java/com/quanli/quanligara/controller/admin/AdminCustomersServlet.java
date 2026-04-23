package com.quanli.quanligara.controller.admin;

import com.quanli.quanligara.dao.UserDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "adminCustomersServlet", value = "/admin/customers")
public class AdminCustomersServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() {
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String q = request.getParameter("q");
        request.setAttribute("customers", userDAO.findActiveCustomersByNameLike(q == null ? "" : q));
        request.setAttribute("q", q == null ? "" : q);
        request.getRequestDispatcher("/WEB-INF/views/admin/customers.jsp").forward(request, response);
    }
}
