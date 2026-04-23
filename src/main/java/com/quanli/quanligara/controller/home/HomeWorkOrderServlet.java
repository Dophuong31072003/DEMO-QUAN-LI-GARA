package com.quanli.quanligara.controller.home;

import com.quanli.quanligara.dao.WorkOrderDAO;
import com.quanli.quanligara.model.User;
import com.quanli.quanligara.model.WorkOrder;
import com.quanli.quanligara.service.WorkOrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "homeWorkOrderServlet", value = "/home/work-order")
public class HomeWorkOrderServlet extends HttpServlet {

    private WorkOrderService workOrderService;
    private WorkOrderDAO workOrderDAO;

    @Override
    public void init() {
        workOrderService = new WorkOrderService();
        workOrderDAO = new WorkOrderDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Object fe = session.getAttribute("flashError");
        if (fe != null) {
            request.setAttribute("error", fe);
            session.removeAttribute("flashError");
        }
        Object fm = session.getAttribute("flashMessage");
        if (fm != null) {
            request.setAttribute("message", fm);
            session.removeAttribute("flashMessage");
        }
        WorkOrder shell = workOrderService.getOrCreateOpenWorkOrder(user);
        Optional<WorkOrder> withLines = workOrderDAO.findWithLines(shell.getId());
        request.setAttribute("workOrder", withLines.orElse(shell));
        request.getRequestDispatcher("/WEB-INF/views/user/work-order.jsp").forward(request, response);
    }
}
