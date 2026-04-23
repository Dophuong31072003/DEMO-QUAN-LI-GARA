package com.quanli.quanligara.controller.auth;

import com.quanli.quanligara.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(filterName = "authFilter", urlPatterns = {"/admin/*", "/home/*", "/profile/*"})
public class AuthFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        String path = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (path.startsWith(contextPath + "/admin")) {
            if (currentUser == null) {
                response.sendRedirect(contextPath + "/login");
                return;
            }
            if (!currentUser.isAdmin()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. Admin role required.");
                return;
            }
        } else if (path.startsWith(contextPath + "/home") || path.startsWith(contextPath + "/profile")) {
            if (currentUser == null) {
                response.sendRedirect(contextPath + "/login");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
