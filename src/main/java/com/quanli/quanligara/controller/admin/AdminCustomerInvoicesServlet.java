package com.quanli.quanligara.controller.admin;

import com.quanli.quanligara.dao.UserDAO;
import com.quanli.quanligara.model.User;
import com.quanli.quanligara.service.InvoiceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "adminCustomerInvoicesServlet", value = "/admin/customers/invoices")
public class AdminCustomerInvoicesServlet extends HttpServlet {

    private UserDAO userDAO;
    private InvoiceService invoiceService;

    @Override
    public void init() {
        userDAO = new UserDAO();
        invoiceService = new InvoiceService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userIdParam = request.getParameter("userId");
        if (userIdParam == null || userIdParam.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing userId");
            return;
        }
        Long userId;
        try {
            userId = Long.parseLong(userIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid userId");
            return;
        }

        User customer = userDAO.findById(userId).orElse(null);
        if (customer == null || customer.isAdmin()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        request.setAttribute("customer", customer);
        request.setAttribute("invoices", invoiceService.findByUser(customer));
        request.getRequestDispatcher("/WEB-INF/views/admin/customer-invoices.jsp").forward(request, response);
    }
}

