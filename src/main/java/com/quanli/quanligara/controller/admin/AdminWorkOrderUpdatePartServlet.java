package com.quanli.quanligara.controller.admin;

import com.quanli.quanligara.service.WorkOrderService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "adminWorkOrderUpdatePartServlet", value = "/admin/work-orders/update-part")
public class AdminWorkOrderUpdatePartServlet extends HttpServlet {

    private WorkOrderService workOrderService;

    @Override
    public void init() {
        workOrderService = new WorkOrderService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String cp = request.getContextPath();
        try {
            Long workOrderId = Long.parseLong(request.getParameter("workOrderId"));
            Long lineId = Long.parseLong(request.getParameter("lineId"));
            int qty = Integer.parseInt(request.getParameter("quantity").trim());
            workOrderService.updatePartLineQuantityOnWorkOrder(workOrderId, lineId, qty);
            request.getSession().setAttribute("adminWorkOrderFlashMessage", "Part quantity updated.");
            response.sendRedirect(cp + "/admin/work-orders/detail?id=" + workOrderId);
        } catch (Exception e) {
            Long workOrderId = parseLongOrNull(request.getParameter("workOrderId"));
            if (workOrderId != null) {
                request.getSession().setAttribute("adminWorkOrderFlashError", e.getMessage());
                response.sendRedirect(cp + "/admin/work-orders/detail?id=" + workOrderId);
            } else {
                response.sendRedirect(cp + "/admin/work-orders");
            }
        }
    }

    private static Long parseLongOrNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
