package com.quanli.quanligara.controller.admin;

import com.quanli.quanligara.model.WorkOrder;
import com.quanli.quanligara.service.WorkOrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "adminWorkOrdersServlet", value = "/admin/work-orders")
public class AdminWorkOrdersServlet extends HttpServlet {

    private WorkOrderService workOrderService;

    @Override
    public void init() {
        workOrderService = new WorkOrderService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<WorkOrder> list = workOrderService.listOpenWorkOrdersForAdmin();
        request.setAttribute("workOrders", list);
        Map<Long, int[]> counts = new HashMap<>();
        for (WorkOrder w : list) {
            counts.put(w.getId(), new int[]{
                    workOrderService.countPartLines(w.getId()),
                    workOrderService.countServiceLines(w.getId())
            });
        }
        request.setAttribute("lineCounts", counts);
        request.getRequestDispatcher("/WEB-INF/views/admin/work-orders.jsp").forward(request, response);
    }
}
