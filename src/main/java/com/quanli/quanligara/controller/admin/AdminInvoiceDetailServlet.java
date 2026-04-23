package com.quanli.quanligara.controller.admin;

import com.quanli.quanligara.model.Invoice;
import com.quanli.quanligara.service.InvoiceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "adminInvoiceDetailServlet", value = "/admin/invoices/detail")
public class AdminInvoiceDetailServlet extends HttpServlet {

    private InvoiceService invoiceService;

    @Override
    public void init() {
        invoiceService = new InvoiceService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Long id = Long.parseLong(idParam);
        Optional<Invoice> inv = invoiceService.loadInvoiceForDisplay(id);
        if (inv.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("invoice", inv.get());
        request.getRequestDispatcher("/WEB-INF/views/admin/invoice-detail.jsp").forward(request, response);
    }
}
