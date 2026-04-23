package com.quanli.quanligara.controller.home;

import com.quanli.quanligara.dao.WorkOrderDAO;
import com.quanli.quanligara.model.User;
import com.quanli.quanligara.model.WorkOrder;
import com.quanli.quanligara.service.InvoiceService;
import com.quanli.quanligara.service.WorkOrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "homeWorkOrderDetailServlet", value = "/home/work-orders/detail")
public class HomeWorkOrderDetailServlet extends HttpServlet {

    private WorkOrderDAO workOrderDAO;
    private WorkOrderService workOrderService;
    private InvoiceService invoiceService;

    @Override
    public void init() {
        workOrderDAO = new WorkOrderDAO();
        workOrderService = new WorkOrderService();
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
        if (!workOrderService.userOwnsWorkOrder(id, user.getId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not your work order");
            return;
        }

        Optional<WorkOrder> wo = workOrderDAO.findWithLinesAndUser(id);
        if (wo.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        request.setAttribute("workOrder", wo.get());

        // If invoiced, try to find linked invoice id for convenience link
        invoiceService.findByWorkOrder(wo.get()).ifPresent(inv -> request.setAttribute("invoiceId", inv.getId()));

        request.getRequestDispatcher("/WEB-INF/views/user/work-order-detail.jsp").forward(request, response);
    }
}

