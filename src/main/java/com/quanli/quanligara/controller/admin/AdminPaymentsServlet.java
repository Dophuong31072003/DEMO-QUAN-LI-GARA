package com.quanli.quanligara.controller.admin;

import com.quanli.quanligara.service.InvoiceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "adminPaymentsServlet", value = "/admin/payments")
public class AdminPaymentsServlet extends HttpServlet {

    private InvoiceService invoiceService;

    @Override
    public void init() {
        invoiceService = new InvoiceService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String q = request.getParameter("q");
        request.setAttribute("invoices", invoiceService.searchInvoicesByCustomerName(q == null ? "" : q));
        request.setAttribute("q", q == null ? "" : q);

        jakarta.servlet.http.HttpSession session = request.getSession();
        Object flash = session.getAttribute("paymentsFlashMessage");
        if (flash != null) {
            request.setAttribute("message", flash);
            session.removeAttribute("paymentsFlashMessage");
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/payments.jsp").forward(request, response);
    }
}
