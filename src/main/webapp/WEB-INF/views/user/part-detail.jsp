<%@ page import="com.quanli.quanligara.model.SparePart" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Part detail</title>
    <%@ include file="/WEB-INF/views/common/user-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/user-navbar.jspf" %>
<div class="content">
    <div class="card">
        <%
            SparePart p = (SparePart) request.getAttribute("part");
            String cp = request.getContextPath();
        %>
        <% if (p != null) { %>
        <h1><%= p.getName() %></h1>
        <p><strong>Code:</strong> <%= p.getCode() %></p>
        <p><strong>Unit price:</strong> <%= p.getUnitPrice() != null ? p.getUnitPrice().toPlainString() : "" %></p>
        <p><strong>Unit:</strong> <%= p.getUnitName() != null ? p.getUnitName() : "—" %></p>
        <p><strong>Stock:</strong> <%= p.getStockQuantity() %></p>
        <p><strong>Description:</strong><br><%= p.getDescription() != null && !p.getDescription().isEmpty() ? p.getDescription() : "—" %></p>
        <% } %>
        <p><a href="<%= cp %>/home/parts">← Back to search</a></p>
    </div>
</div>
</body>
</html>
