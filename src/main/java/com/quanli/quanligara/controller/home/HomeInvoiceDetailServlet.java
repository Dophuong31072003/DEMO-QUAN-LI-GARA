package com.quanli.quanligara.controller.home;

import com.quanli.quanligara.model.Invoice;
import com.quanli.quanligara.model.User;
import com.quanli.quanligara.service.InvoiceService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "homeInvoiceDetailServlet", value = "/home/invoices/detail")
public class HomeInvoiceDetailServlet extends HttpServlet {

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
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Long id = Long.parseLong(idParam);
        if (!invoiceService.userOwnsInvoice(id, user.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not your invoice");
            return;
        }
        Optional<Invoice> inv = invoiceService.loadInvoiceForDisplay(id);
        if (inv.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("invoice", inv.get());
        request.getRequestDispatcher("/WEB-INF/views/user/invoice-detail.jsp").forward(request, response);
    }
}
