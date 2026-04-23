package com.quanli.quanligara.controller.admin;

import com.quanli.quanligara.model.WorkOrder;
import com.quanli.quanligara.service.WorkOrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "adminWorkOrdersNewServlet", value = "/admin/work-orders/new")
public class AdminWorkOrdersNewServlet extends HttpServlet {

    private WorkOrderService workOrderService;

    @Override
    public void init() {
        workOrderService = new WorkOrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String userIdParam = request.getParameter("userId");
        if (userIdParam == null || userIdParam.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing userId");
            return;
        }
        try {
            Long userId = Long.parseLong(userIdParam);
            WorkOrder wo = workOrderService.getOrCreateOpenQuotationForCustomer(userId);
            response.sendRedirect(request.getContextPath() + "/admin/work-orders/detail?id=" + wo.getId());
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid userId");
        } catch (RuntimeException e) {
            request.getSession().setAttribute("adminCustomersFlashError", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/customers");
        }
    }
}
