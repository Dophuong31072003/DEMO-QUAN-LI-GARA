<%@ page import="com.quanli.quanligara.model.ServiceOffering" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Services</title>
    <%@ include file="/WEB-INF/views/common/admin-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/admin-navbar.jspf" %>
<div class="content">
    <div class="card">
        <h1>Services</h1>
        <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("message") != null) { %>
            <p class="muted"><%= request.getAttribute("message") %></p>
        <% } %>

        <h2>Create service</h2>
        <form method="post" action="<%= request.getContextPath() %>/admin/services">
            <p><label>Code<br><input type="text" name="code" required></label></p>
            <p><label>Name<br><input type="text" name="name" required></label></p>
            <p><label>Description<br><textarea name="description" rows="2"></textarea></label></p>
            <p><label>Unit price<br><input type="text" name="unitPrice" required></label></p>
            <p><button type="submit">Create</button></p>
        </form>

        <h2>Active services</h2>
        <table>
            <thead>
            <tr><th>Code</th><th>Name</th><th>Price</th><th>Actions</th></tr>
            </thead>
            <tbody>
            <%
                @SuppressWarnings("unchecked")
                List<ServiceOffering> services = (List<ServiceOffering>) request.getAttribute("services");
                if (services != null) {
                    for (ServiceOffering s : services) {
            %>
            <% if (!s.isActive()) { continue; } %>
            <tr>
                <td><%= s.getCode() %></td>
                <td><%= s.getName() %></td>
                <td><%= s.getUnitPrice() != null ? s.getUnitPrice().toPlainString() : "" %></td>
                <td>
                    <form method="post" action="<%= request.getContextPath() %>/admin/services/update" class="inline">
                        <input type="hidden" name="id" value="<%= s.getId() %>">
                        <input type="text" name="code" value="<%= s.getCode() %>" style="max-width:100px">
                        <input type="text" name="name" value="<%= s.getName() %>" style="max-width:120px">
                        <input type="text" name="unitPrice" value="<%= s.getUnitPrice() != null ? s.getUnitPrice().toPlainString() : "" %>" style="max-width:80px">
                        <button type="submit">Save</button>
                    </form>
                    <form method="post" action="<%= request.getContextPath() %>/admin/services/deactivate" class="inline" onsubmit="return confirm('Soft delete this service?');">
                        <input type="hidden" name="id" value="<%= s.getId() %>">
                        <button type="submit" class="secondary">Delete</button>
                    </form>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
