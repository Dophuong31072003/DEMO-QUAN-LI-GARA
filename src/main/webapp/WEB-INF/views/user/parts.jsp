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
        <% if (request.getAttribute("message") != null) { %>
            <p><%= request.getAttribute("message") %></p>
        <% } %>
        <form method="get" action="<%= request.getContextPath() %>/home/parts">
            <label>Search <input type="text" name="q" value="<%= request.getAttribute("q") == null ? "" : request.getAttribute("q") %>"></label>
            <button type="submit">Search</button>
        </form>
        <table>
            <thead>
            <tr><th>Code</th><th>Name</th><th>Price</th><th>Stock</th><th>Qty</th><th></th></tr>
            </thead>
            <tbody>
            <%
                @SuppressWarnings("unchecked")
                List<SparePart> parts = (List<SparePart>) request.getAttribute("parts");
                String cp = request.getContextPath();
                String qVal = request.getAttribute("q") == null ? "" : String.valueOf(request.getAttribute("q"));
                if (parts != null) {
                    for (SparePart p : parts) {
            %>
            <tr>
                <td><%= p.getCode() %></td>
                <td><%= p.getName() %></td>
                <td><%= p.getUnitPrice() != null ? p.getUnitPrice().toPlainString() : "" %></td>
                <td><%= p.getStockQuantity() %></td>
                <td>
                    <form method="post" action="<%= cp %>/home/parts/select" style="display:flex;gap:6px;align-items:center;">
                        <input type="hidden" name="partId" value="<%= p.getId() %>">
                        <input type="hidden" name="q" value="<%= qVal %>">
                        <input type="number" name="quantity" value="1" min="1" style="width:70px">
                        <button type="submit" data-item-code="<%= p.getCode() %>">Add</button>
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
