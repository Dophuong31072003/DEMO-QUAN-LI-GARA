package com.quanli.quanligara.controller.home;

import com.quanli.quanligara.model.User;
import com.quanli.quanligara.service.InvoiceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "homeInvoicesServlet", value = "/home/invoices")
public class HomeInvoicesServlet extends HttpServlet {

    private InvoiceService invoiceService;

    @Override
    public void init() {
        invoiceService = new InvoiceService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.setAttribute("invoices", invoiceService.findByUser(user));
        request.getRequestDispatcher("/WEB-INF/views/user/invoices.jsp").forward(request, response);
    }
}
