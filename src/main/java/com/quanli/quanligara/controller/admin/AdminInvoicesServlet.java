package com.quanli.quanligara.controller.admin;

import com.quanli.quanligara.service.InvoiceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "adminInvoicesServlet", value = "/admin/invoices")
public class AdminInvoicesServlet extends HttpServlet {

    private InvoiceService invoiceService;

    @Override
    public void init() {
        invoiceService = new InvoiceService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("invoices", invoiceService.findAll());
        request.getRequestDispatcher("/WEB-INF/views/admin/invoices.jsp").forward(request, response);
    }
}
