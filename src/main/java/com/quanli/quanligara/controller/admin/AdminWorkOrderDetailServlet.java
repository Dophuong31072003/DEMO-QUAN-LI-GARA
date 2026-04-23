package com.quanli.quanligara.controller.admin;

import com.quanli.quanligara.model.WorkOrder;
import com.quanli.quanligara.service.CatalogService;
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
    private CatalogService catalogService;

    @Override
    public void init() {
        workOrderService = new WorkOrderService();
        invoiceService = new InvoiceService();
        catalogService = new CatalogService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing id");
            return;
        }
        Long id;
        try {
            id = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid id");
            return;
        }
        Optional<WorkOrder> wo = workOrderService.loadWorkOrderForDisplay(id);
        if (wo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("workOrder", wo.get());
        request.setAttribute("canIssue", invoiceService.canIssueInvoice(id));
        request.setAttribute("catalogParts", catalogService.searchActiveParts(""));
        request.setAttribute("catalogServices", catalogService.searchActiveServices(""));

        jakarta.servlet.http.HttpSession session = request.getSession();
        Object err = session.getAttribute("issueError");
        if (err != null) {
            request.setAttribute("error", err);
            session.removeAttribute("issueError");
        }
        Object flashErr = session.getAttribute("adminWorkOrderFlashError");
        if (flashErr != null) {
            request.setAttribute("error", flashErr);
            session.removeAttribute("adminWorkOrderFlashError");
        }
        Object flashOk = session.getAttribute("adminWorkOrderFlashMessage");
        if (flashOk != null) {
            request.setAttribute("message", flashOk);
            session.removeAttribute("adminWorkOrderFlashMessage");
        }

        request.getRequestDispatcher("/WEB-INF/views/admin/work-order-detail.jsp").forward(request, response);
    }
}
