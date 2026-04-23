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

@WebServlet(name = "homePartsSelectServlet", value = "/home/parts/select")
public class HomePartsSelectServlet extends HttpServlet {

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
            Long partId = Long.parseLong(request.getParameter("partId"));
            int qty = Integer.parseInt(request.getParameter("quantity").trim());
            workOrderService.addPartLine(user, partId, qty);
            session.setAttribute("flashMessage", "Added to Work Order");
        } catch (Exception e) {
            session.setAttribute("flashError", e.getMessage());
        }
        String q = request.getParameter("q");
        response.sendRedirect(cp + "/home/parts" + (q != null && !q.isBlank() ? "?q=" + java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8) : ""));
    }
}
