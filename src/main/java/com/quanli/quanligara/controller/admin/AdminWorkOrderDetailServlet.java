package com.quanli.quanligara.controller.admin;

import com.quanli.quanligara.model.WorkOrder;
import com.quanli.quanligara.service.InvoiceService;
import com.quanli.quanligara.service.WorkOrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "adminWorkOrderDetailServlet", value = "/admin/work-orders/detail")
public class AdminWorkOrderDetailServlet extends HttpServlet {

    private WorkOrderService workOrderService;
    private InvoiceService invoiceService;

    @Override
    public void init() {
        workOrderService = new WorkOrderService();
        invoiceService = new InvoiceService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id");
            return;
        }
        Long id = Long.parseLong(idParam);
        Optional<WorkOrder> wo = workOrderService.loadWorkOrderForDisplay(id);
        if (wo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("workOrder", wo.get());
        request.setAttribute("canIssue", invoiceService.canIssueInvoice(id));
        Object err = request.getSession().getAttribute("issueError");
        if (err != null) {
            request.setAttribute("error", err);
            request.getSession().removeAttribute("issueError");
        }
        request.getRequestDispatcher("/WEB-INF/views/admin/work-order-detail.jsp").forward(request, response);
    }
}
