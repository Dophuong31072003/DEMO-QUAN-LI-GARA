<%@ page import="com.quanli.quanligara.model.ServiceOffering" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Service detail</title>
    <%@ include file="/WEB-INF/views/common/user-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/user-navbar.jspf" %>
<div class="content">
    <div class="card">
        <%
            ServiceOffering s = (ServiceOffering) request.getAttribute("service");
            String cp = request.getContextPath();
        %>
        <% if (s != null) { %>
        <h1><%= s.getName() %></h1>
        <p><strong>Code:</strong> <%= s.getCode() %></p>
        <p><strong>Unit price:</strong> <%= s.getUnitPrice() != null ? s.getUnitPrice().toPlainString() : "" %></p>
        <p><strong>Description:</strong><br><%= s.getDescription() != null && !s.getDescription().isEmpty() ? s.getDescription() : "—" %></p>
        <% } %>
        <p><a href="<%= cp %>/home/services">← Back to search</a></p>
    </div>
</div>
</body>
</html>
