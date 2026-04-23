<%@ page import="com.quanli.quanligara.model.Invoice" %>
<%@ page import="com.quanli.quanligara.model.InvoiceLine" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Invoice Detail</title>
    <%@ include file="/WEB-INF/views/common/admin-styles.jspf" %>
</head>
<body<% if (Boolean.TRUE.equals(request.getAttribute("autoPrint"))) { %> onload="window.print()"<% } %>>
<%@ include file="/WEB-INF/views/common/admin-navbar.jspf" %>
<div class="content">
    <div class="card">
        <%
            Invoice inv = (Invoice) request.getAttribute("invoice");
            String cp = request.getContextPath();
        %>
        <% if (request.getAttribute("message") != null) { %>
            <p class="message"><%= request.getAttribute("message") %></p>
        <% } %>
        <% if (inv != null) { %>
        <h1>Invoice <%= inv.getInvoiceNumber() %></h1>
        <p class="muted">Status: <%= inv.getStatus() %> — Issued: <%= inv.getIssuedAt() %></p>
        <p class="muted">Payment: <strong><%= inv.getPaidAt() == null ? "UNPAID" : "PAID" %></strong><% if (inv.getPaidAt() != null) { %> — <%= inv.getPaidAt() %><% } %></p>
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
        <p class="no-print">
            <button type="button" class="btn secondary" onclick="window.print()">Print</button>
            <% if (inv != null) { %>
                <% if (inv.getPaidAt() == null) { %>
                <form method="post" action="<%= cp %>/admin/payments/confirm" class="inline" style="display:inline;">
                    <input type="hidden" name="invoiceId" value="<%= inv.getId() %>">
                    <input type="hidden" name="invoiceNumber" value="<%= inv.getInvoiceNumber() %>">
                    <input type="hidden" name="returnTo" value="<%= cp %>/admin/invoices/detail?id=<%= inv.getId() %>">
                    <button type="submit">Confirm payment</button>
                </form>
                <% } else { %>
                <span class="muted">Paid</span>
                <% } %>
                <% if (inv.getUser() != null) { %>
                    <a class="btn secondary" href="<%= cp %>/admin/customers/invoices?userId=<%= inv.getUser().getId() %>">Back to customer invoices</a>
                <% } %>
            <% } %>
        </p>
    </div>
</div>
</body>
</html>
