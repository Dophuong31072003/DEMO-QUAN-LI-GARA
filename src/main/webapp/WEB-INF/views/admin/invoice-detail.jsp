<%@ page import="com.quanli.quanligara.model.Invoice" %>
<%@ page import="com.quanli.quanligara.model.InvoiceLine" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Invoice Detail</title>
    <%@ include file="/WEB-INF/views/common/admin-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/admin-navbar.jspf" %>
<div class="content">
    <div class="card">
        <%
            Invoice inv = (Invoice) request.getAttribute("invoice");
            String cp = request.getContextPath();
        %>
        <% if (inv != null) { %>
        <h1>Invoice <%= inv.getInvoiceNumber() %></h1>
        <p class="muted">Status: <%= inv.getStatus() %> — Issued: <%= inv.getIssuedAt() %></p>
        <p>Customer: <%= inv.getUser() != null ? inv.getUser().getFullName() : "" %></p>
        <p>Work order: #<%= inv.getWorkOrder() != null ? inv.getWorkOrder().getId() : "" %></p>

        <table>
            <thead>
            <tr><th>Type</th><th>Code</th><th>Name</th><th>Unit</th><th>Qty</th><th>Line total</th></tr>
            </thead>
            <tbody>
            <% for (InvoiceLine line : inv.getLines()) { %>
            <tr>
                <td><%= line.getItemType() %></td>
                <td><%= line.getItemCode() %></td>
                <td><%= line.getItemName() %></td>
                <td><%= line.getUnitPrice().toPlainString() %></td>
                <td><%= line.getQuantity() %></td>
                <td><%= line.getLineTotal().toPlainString() %></td>
            </tr>
            <% } %>
            </tbody>
        </table>
        <p><strong>Total:</strong> <%= inv.getTotalAmount().toPlainString() %></p>
        <% } %>
        <p><a href="<%= cp %>/admin/invoices">← Back</a></p>
    </div>
</div>
</body>
</html>
