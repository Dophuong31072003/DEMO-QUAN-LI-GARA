package com.quanli.quanligara.controller.home;

import com.quanli.quanligara.model.User;
import com.quanli.quanligara.service.WorkOrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "homeWorkOrderUpdatePartServlet", value = "/home/work-order/update-part")
public class HomeWorkOrderUpdatePartServlet extends HttpServlet {

    private WorkOrderService workOrderService;

    @Override
    public void init() {
        workOrderService = new WorkOrderService();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("currentUser");
        String cp = request.getContextPath();
        if (user == null) {
            response.sendRedirect(cp + "/login");
            return;
        }
        try {
            Long lineId = Long.parseLong(request.getParameter("lineId"));
            int qty = Integer.parseInt(request.getParameter("quantity").trim());
            workOrderService.updatePartLineQuantity(user, lineId, qty);
        } catch (Exception e) {
            session.setAttribute("flashError", e.getMessage());
        }
        response.sendRedirect(cp + "/home/work-order");
    }
}
