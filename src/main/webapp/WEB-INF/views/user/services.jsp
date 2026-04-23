<%@ page import="com.quanli.quanligara.model.ServiceOffering" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Services</title>
    <%@ include file="/WEB-INF/views/common/user-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/user-navbar.jspf" %>
<div class="content">
    <div class="card">
        <h1>Services</h1>
        <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <form method="get" action="<%= request.getContextPath() %>/home/services">
            <label>Search <input type="text" name="q" value="<%= request.getAttribute("q") == null ? "" : request.getAttribute("q") %>"></label>
            <button type="submit">Search</button>
        </form>
        <table>
            <thead>
            <tr><th>Code</th><th>Name</th><th>Price</th><th></th></tr>
            </thead>
            <tbody>
            <%
                @SuppressWarnings("unchecked")
                List<ServiceOffering> services = (List<ServiceOffering>) request.getAttribute("services");
                String cp = request.getContextPath();
                if (services != null) {
                    for (ServiceOffering s : services) {
            %>
            <tr>
                <td><%= s.getCode() %></td>
                <td><%= s.getName() %></td>
                <td><%= s.getUnitPrice() != null ? s.getUnitPrice().toPlainString() : "" %></td>
                <td><a href="<%= cp %>/home/services/detail?id=<%= s.getId() %>">View details</a></td>
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
