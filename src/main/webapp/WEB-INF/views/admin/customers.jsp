<%@ page import="com.quanli.quanligara.model.User" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Customers</title>
    <%@ include file="/WEB-INF/views/common/admin-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/admin-navbar.jspf" %>
<div class="content">
    <div class="card">
        <h1>Customers</h1>
        <p class="muted">Search by name, username, or email. Open or create a quotation (work order) for a customer.</p>
        <%
            String cp = request.getContextPath();
            jakarta.servlet.http.HttpSession sess = request.getSession(false);
            if (sess != null && sess.getAttribute("adminCustomersFlashError") != null) {
        %>
        <p class="error"><%= sess.getAttribute("adminCustomersFlashError") %></p>
        <% sess.removeAttribute("adminCustomersFlashError"); } %>
        <form method="get" action="<%= cp %>/admin/customers">
            <label>Search <input type="text" name="q" value="<%= request.getAttribute("q") == null ? "" : request.getAttribute("q") %>"></label>
            <button type="submit">Search</button>
        </form>
        <table>
            <thead>
            <tr><th>Name</th><th>Username</th><th>Email</th><th>Actions</th></tr>
            </thead>
            <tbody>
            <%
                @SuppressWarnings("unchecked")
                List<User> customers = (List<User>) request.getAttribute("customers");
                if (customers != null) {
                    for (User u : customers) {
            %>
            <tr>
                <td><%= u.getFullName() != null ? u.getFullName() : "—" %></td>
                <td><%= u.getUsername() %></td>
                <td><%= u.getEmail() != null ? u.getEmail() : "—" %></td>
                <td>
                    <a href="<%= cp %>/admin/work-orders/new?userId=<%= u.getId() %>">Open quotation</a>
                    ·
                    <a href="<%= cp %>/admin/customers/invoices?userId=<%= u.getId() %>">View invoices</a>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
        <p><a href="<%= cp %>/admin">← Dashboard</a></p>
    </div>
</div>
</body>
</html>
