package com.quanli.quanligara.controller.admin;

import com.quanli.quanligara.service.InvoiceService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "adminPaymentsConfirmServlet", value = "/admin/payments/confirm")
public class AdminPaymentsConfirmServlet extends HttpServlet {

    private InvoiceService invoiceService;

    @Override
    public void init() {
        invoiceService = new InvoiceService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String cp = request.getContextPath();
        String q = request.getParameter("q");
        String returnTo = request.getParameter("returnTo");
        String idParam = request.getParameter("invoiceId");
        if (idParam == null || idParam.isBlank()) {
            response.sendRedirect(cp + "/admin/payments");
            return;
        }
        try {
            Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(cp + "/admin/payments");
            return;
        }
        String num = request.getParameter("invoiceNumber");
        if (num == null || num.isBlank()) {
            num = idParam;
        }

        try {
            invoiceService.confirmPayment(Long.parseLong(idParam));
        } catch (RuntimeException e) {
            request.getSession().setAttribute("paymentsFlashMessage", e.getMessage());
        }

        request.getSession().setAttribute("paymentsFlashMessage",
                "Payment confirmed. " + num);

        if (returnTo != null && !returnTo.isBlank() && returnTo.startsWith(cp + "/admin/")) {
            response.sendRedirect(returnTo);
            return;
        }

        String redirect = cp + "/admin/payments";
        if (q != null && !q.isBlank()) {
            redirect += "?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
        }
        response.sendRedirect(redirect);
    }
}
