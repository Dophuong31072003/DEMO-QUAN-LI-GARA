package com.quanli.quanligara.controller.admin;

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

@WebServlet(name = "adminInvoiceIssueServlet", value = "/admin/invoices/issue")
public class AdminInvoiceIssueServlet extends HttpServlet {

    private InvoiceService invoiceService;

    @Override
    public void init() {
        invoiceService = new InvoiceService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User admin = session == null ? null : (User) session.getAttribute("currentUser");
        String cp = request.getContextPath();
        Long workOrderId = Long.parseLong(request.getParameter("workOrderId"));
        try {
            Invoice inv = invoiceService.issueInvoice(workOrderId, admin);
            response.sendRedirect(cp + "/admin/invoices/detail?id=" + inv.getId());
        } catch (Exception e) {
            if (session != null) {
                session.setAttribute("issueError", e.getMessage());
            }
            response.sendRedirect(cp + "/admin/work-orders/detail?id=" + workOrderId);
        }
    }
}
