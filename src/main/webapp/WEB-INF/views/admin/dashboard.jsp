<%@ page import="com.quanli.quanligara.model.User" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <title>Dashboard - Admin</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; background: #f5f5f5; }
        .navbar {
            background: #333; color: white; padding: 15px 30px;
            display: flex; justify-content: space-between; align-items: center;
        }
        .navbar a { color: white; text-decoration: none; margin-left: 20px; }
        .content { padding: 40px; }
        .card {
            background: white; padding: 30px; border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1); max-width: 800px;
        }
        h1 { color: #333; }
        .badge {
            background: #e74c3c; color: white; padding: 4px 12px;
            border-radius: 12px; font-size: 12px;
        }
    </style>
</head>
<body>
    <div class="navbar">
        <div><strong>Quan Li Gara</strong> - Admin Panel</div>
        <div>
            <span>Welcome, <%= currentUser.getFullName() %></span>
            <span class="badge"><%= currentUser.getRoleName() %></span>
            <a href="<%= request.getContextPath() %>/logout">Logout</a>
        </div>
    </div>
    <div class="content">
        <div class="card">
            <h1>Admin Dashboard</h1>
            <p>Day la trang danh cho quan tri vien.</p>
            <p>Chuc nang quan ly se duoc trien khai tai day.</p>
        </div>
    </div>
</body>
</html>
