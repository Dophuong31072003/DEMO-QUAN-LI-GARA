<%@ page import="com.quanli.quanligara.model.WorkOrder" %>
<%@ page import="com.quanli.quanligara.model.WorkOrderPartLine" %>
<%@ page import="com.quanli.quanligara.model.WorkOrderServiceLine" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Work Order Detail</title>
    <%@ include file="/WEB-INF/views/common/user-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/user-navbar.jspf" %>
<div class="content">
    <div class="card">
        <%
            WorkOrder wo = (WorkOrder) request.getAttribute("workOrder");
            Object invId = request.getAttribute("invoiceId");
            String cp = request.getContextPath();
            BigDecimal subtotal = BigDecimal.ZERO;
        %>
        <% if (wo != null) { %>
        <h1>Work Order #<%= wo.getId() %></h1>
        <p>Status: <strong><%= wo.getStatus() %></strong></p>
        <p class="muted">Submitted: <%= wo.getSubmittedAt() != null ? wo.getSubmittedAt().toString() : "—" %> |
            Invoiced: <%= wo.getInvoicedAt() != null ? wo.getInvoicedAt().toString() : "—" %></p>

        <h2>Parts</h2>
        <table>
            <thead>
            <tr><th>Code</th><th>Name</th><th>Unit</th><th>Qty</th><th>Line</th></tr>
            </thead>
            <tbody>
            <% for (WorkOrderPartLine pl : wo.getPartLines()) {
                BigDecimal unit = pl.getSparePart().getUnitPrice();
                BigDecimal line = unit.multiply(BigDecimal.valueOf(pl.getQuantity()));
                subtotal = subtotal.add(line);
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
            <tr><th>Code</th><th>Name</th><th>Unit</th><th>Qty</th><th>Line</th></tr>
            </thead>
            <tbody>
            <% for (WorkOrderServiceLine sl : wo.getServiceLines()) {
                BigDecimal unit = sl.getServiceOffering().getUnitPrice();
                BigDecimal line = unit.multiply(BigDecimal.valueOf(sl.getQuantity()));
                subtotal = subtotal.add(line);
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

        <p><strong>Subtotal (preview):</strong> <%= subtotal.toPlainString() %></p>

        <% if (invId != null) { %>
        <p><a href="<%= cp %>/home/invoices/detail?id=<%= invId %>">View related invoice</a></p>
        <% } %>

        <p><a href="<%= cp %>/home/work-orders">← Back to list</a></p>
        <% } %>
    </div>
</div>
</body>
</html>

