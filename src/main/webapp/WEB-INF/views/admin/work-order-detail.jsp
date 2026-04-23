<%@ page import="com.quanli.quanligara.model.WorkOrder" %>
<%@ page import="com.quanli.quanligara.model.WorkOrderPartLine" %>
<%@ page import="com.quanli.quanligara.model.WorkOrderServiceLine" %>
<%@ page import="com.quanli.quanligara.model.enums.WorkOrderStatus" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Work Order Detail</title>
    <%@ include file="/WEB-INF/views/common/admin-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/admin-navbar.jspf" %>
<div class="content">
    <div class="card">
        <%
            WorkOrder wo = (WorkOrder) request.getAttribute("workOrder");
            Boolean canIssue = (Boolean) request.getAttribute("canIssue");
            String cp = request.getContextPath();
            BigDecimal previewTotal = BigDecimal.ZERO;
        %>
        <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (wo != null) { %>
        <h1>Work Order #<%= wo.getId() %></h1>
        <p class="muted">User: <%= wo.getUser() != null ? wo.getUser().getFullName() : "" %> — Status: <strong><%= wo.getStatus() %></strong></p>

        <h2>Parts (current catalog prices)</h2>
        <table>
            <thead>
            <tr><th>Code</th><th>Name</th><th>Unit price</th><th>Qty</th><th>Line</th></tr>
            </thead>
            <tbody>
            <% for (WorkOrderPartLine pl : wo.getPartLines()) {
                BigDecimal unit = pl.getSparePart().getUnitPrice();
                BigDecimal line = unit.multiply(BigDecimal.valueOf(pl.getQuantity()));
                previewTotal = previewTotal.add(line);
            %>
            <tr>
                <td><%= pl.getSparePart().getCode() %></td>
                <td><%= pl.getSparePart().getName() %></td>
                <td><%= unit.toPlainString() %></td>
                <td><%= pl.getQuantity() %></td>
                <td><%= line.toPlainString() %></td>
            </tr>
            <% } %>
            </tbody>
        </table>

        <h2>Services</h2>
        <table>
            <thead>
            <tr><th>Code</th><th>Name</th><th>Unit price</th><th>Qty</th><th>Line</th></tr>
            </thead>
            <tbody>
            <% for (WorkOrderServiceLine sl : wo.getServiceLines()) {
                BigDecimal unit = sl.getServiceOffering().getUnitPrice();
                BigDecimal line = unit.multiply(BigDecimal.valueOf(sl.getQuantity()));
                previewTotal = previewTotal.add(line);
            %>
            <tr>
                <td><%= sl.getServiceOffering().getCode() %></td>
                <td><%= sl.getServiceOffering().getName() %></td>
                <td><%= unit.toPlainString() %></td>
                <td><%= sl.getQuantity() %></td>
                <td><%= line.toPlainString() %></td>
            </tr>
            <% } %>
            </tbody>
        </table>

        <p><strong>Preview total:</strong> <%= previewTotal.toPlainString() %></p>

        <% if (Boolean.TRUE.equals(canIssue) && wo.getStatus() != WorkOrderStatus.INVOICED) { %>
        <form method="post" action="<%= cp %>/admin/invoices/issue">
            <input type="hidden" name="workOrderId" value="<%= wo.getId() %>">
            <button type="submit">Issue Invoice</button>
        </form>
        <% } else if (wo.getStatus() == WorkOrderStatus.INVOICED) { %>
        <p class="muted">This work order is already invoiced.</p>
        <% } else { %>
        <p class="muted">Add at least one line before issuing an invoice.</p>
        <% } %>

        <p><a href="<%= cp %>/admin/work-orders">← Back to list</a></p>
        <% } %>
    </div>
</div>
</body>
</html>
