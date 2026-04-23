<%@ page import="com.quanli.quanligara.model.SparePart" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Parts</title>
    <%@ include file="/WEB-INF/views/common/admin-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/admin-navbar.jspf" %>
<div class="content">
    <div class="card">
        <h1>Parts</h1>
        <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("message") != null) { %>
            <p class="muted"><%= request.getAttribute("message") %></p>
        <% } %>

        <h2>Create part</h2>
        <form method="post" action="<%= request.getContextPath() %>/admin/parts">
            <p><label>Code<br><input type="text" name="code" required></label></p>
            <p><label>Name<br><input type="text" name="name" required></label></p>
            <p><label>Description<br><textarea name="description" rows="2"></textarea></label></p>
            <p><label>Unit name<br><input type="text" name="unitName"></label></p>
            <p><label>Unit price<br><input type="text" name="unitPrice" required></label></p>
            <p><label>Stock<br><input type="number" name="stockQuantity" min="0" value="0" required></label></p>
            <p><button type="submit">Create</button></p>
        </form>

        <h2>Active parts</h2>
        <table>
            <thead>
            <tr><th>Code</th><th>Name</th><th>Price</th><th>Stock</th><th>Actions</th></tr>
            </thead>
            <tbody>
            <%
                @SuppressWarnings("unchecked")
                List<SparePart> parts = (List<SparePart>) request.getAttribute("parts");
                if (parts != null) {
                    for (SparePart p : parts) {
            %>
            <% if (!p.isActive()) { continue; } %>
            <tr>
                <td><%= p.getCode() %></td>
                <td><%= p.getName() %></td>
                <td><%= p.getUnitPrice() != null ? p.getUnitPrice().toPlainString() : "" %></td>
                <td><%= p.getStockQuantity() %></td>
                <td>
                    <form method="post" action="<%= request.getContextPath() %>/admin/parts/update" class="inline">
                        <input type="hidden" name="id" value="<%= p.getId() %>">
                        <input type="text" name="code" value="<%= p.getCode() %>" style="max-width:100px">
                        <input type="text" name="name" value="<%= p.getName() %>" style="max-width:120px">
                        <input type="text" name="unitPrice" value="<%= p.getUnitPrice() != null ? p.getUnitPrice().toPlainString() : "" %>" style="max-width:80px">
                        <input type="number" name="stockQuantity" value="<%= p.getStockQuantity() %>" style="max-width:60px">
                        <button type="submit">Save</button>
                    </form>
                    <form method="post" action="<%= request.getContextPath() %>/admin/parts/deactivate" class="inline" onsubmit="return confirm('Soft delete this part?');">
                        <input type="hidden" name="id" value="<%= p.getId() %>">
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
