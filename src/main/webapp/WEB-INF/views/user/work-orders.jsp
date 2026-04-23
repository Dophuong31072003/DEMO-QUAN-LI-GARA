<%@ page import="com.quanli.quanligara.model.WorkOrder" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>My Work Orders</title>
    <%@ include file="/WEB-INF/views/common/user-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/user-navbar.jspf" %>
<div class="content">
    <div class="card">
        <h1>My Work Orders</h1>
        <table>
            <thead>
            <tr><th>ID</th><th>Status</th><th>Submitted</th><th>Invoiced</th><th></th></tr>
            </thead>
            <tbody>
            <%
                @SuppressWarnings("unchecked")
                List<WorkOrder> workOrders = (List<WorkOrder>) request.getAttribute("workOrders");
                String cp = request.getContextPath();
                if (workOrders != null) {
                    for (WorkOrder w : workOrders) {
            %>
            <tr>
                <td><%= w.getId() %></td>
                <td><%= w.getStatus() %></td>
                <td><%= w.getSubmittedAt() != null ? w.getSubmittedAt().toString() : "—" %></td>
                <td><%= w.getInvoicedAt() != null ? w.getInvoicedAt().toString() : "—" %></td>
                <td><a href="<%= cp %>/home/work-orders/detail?id=<%= w.getId() %>">Detail</a></td>
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

