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
    <title>Home - Customer</title>
    <%@ include file="/WEB-INF/views/common/user-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/user-navbar.jspf" %>
<div class="content">
    <div class="card">
        <h1>Welcome</h1>
        <p>Search spare parts and services by name or code. Open an item to view full details.</p>
        <ul>
            <li><a href="<%= request.getContextPath() %>/home/parts">Browse parts</a></li>
            <li><a href="<%= request.getContextPath() %>/home/services">Browse services</a></li>
        </ul>
    </div>
</div>
</body>
</html>
