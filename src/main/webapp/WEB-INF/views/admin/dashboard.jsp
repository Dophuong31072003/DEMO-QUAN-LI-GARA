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
    <%@ include file="/WEB-INF/views/common/admin-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/admin-navbar.jspf" %>
<div class="content">
    <div class="card">
        <h1>Admin Dashboard</h1>
        <p class="muted">Choose a module:</p>
        <ul>
            <li><a href="<%= request.getContextPath() %>/admin/customers">Customers</a> — search customers and open quotations</li>
            <li><a href="<%= request.getContextPath() %>/admin/parts">Parts</a> — spare parts catalog</li>
            <li><a href="<%= request.getContextPath() %>/admin/services">Services</a> — service offerings</li>
            <li><a href="<%= request.getContextPath() %>/admin/work-orders">Work Orders</a> — quotations &amp; submissions</li>
            <li><a href="<%= request.getContextPath() %>/admin/invoices">Invoices</a> — issued invoices</li>
            <li><a href="<%= request.getContextPath() %>/admin/payments">Payments</a> — find invoices by customer, print, confirm payment</li>
        </ul>
    </div>
</div>
</body>
</html>
