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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(name = "homeServicesSelectServlet", value = "/home/services/select")
public class HomeServicesSelectServlet extends HttpServlet {

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
            workOrderService.getOrCreateOpenWorkOrder(user);
            Long serviceId = Long.parseLong(request.getParameter("serviceId"));
            int qty = Integer.parseInt(request.getParameter("quantity").trim());
            workOrderService.addServiceLine(user, serviceId, qty);
            session.setAttribute("flashMessage", "Added to Work Order");
        } catch (Exception e) {
            session.setAttribute("flashError", e.getMessage());
        }
        String q = request.getParameter("q");
        response.sendRedirect(cp + "/home/services" + (q != null && !q.isBlank() ? "?q=" + URLEncoder.encode(q, StandardCharsets.UTF_8) : ""));
    }
}
