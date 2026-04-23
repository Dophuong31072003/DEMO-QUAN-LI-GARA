<%@ page import="com.quanli.quanligara.model.SparePart" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Parts</title>
    <%@ include file="/WEB-INF/views/common/user-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/user-navbar.jspf" %>
<div class="content">
    <div class="card">
        <h1>Parts</h1>
        <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <form method="get" action="<%= request.getContextPath() %>/home/parts">
            <label>Search <input type="text" name="q" value="<%= request.getAttribute("q") == null ? "" : request.getAttribute("q") %>"></label>
            <button type="submit">Search</button>
        </form>
        <table>
            <thead>
            <tr><th>Code</th><th>Name</th><th>Price</th><th>Stock</th><th></th></tr>
            </thead>
            <tbody>
            <%
                @SuppressWarnings("unchecked")
                List<SparePart> parts = (List<SparePart>) request.getAttribute("parts");
                String cp = request.getContextPath();
                if (parts != null) {
                    for (SparePart p : parts) {
            %>
            <tr>
                <td><%= p.getCode() %></td>
                <td><%= p.getName() %></td>
                <td><%= p.getUnitPrice() != null ? p.getUnitPrice().toPlainString() : "" %></td>
                <td><%= p.getStockQuantity() %></td>
                <td><a href="<%= cp %>/home/parts/detail?id=<%= p.getId() %>">View details</a></td>
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
