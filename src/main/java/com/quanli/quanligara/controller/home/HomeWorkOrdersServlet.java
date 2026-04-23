package com.quanli.quanligara.controller.home;

import com.quanli.quanligara.dao.WorkOrderDAO;
import com.quanli.quanligara.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(name = "homeWorkOrdersServlet", value = "/home/work-orders")
public class HomeWorkOrdersServlet extends HttpServlet {

    private WorkOrderDAO workOrderDAO;

    @Override
    public void init() {
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
        request.setAttribute("workOrders", workOrderDAO.findAllByUser(user));
        request.getRequestDispatcher("/WEB-INF/views/user/work-orders.jsp").forward(request, response);
    }
}

