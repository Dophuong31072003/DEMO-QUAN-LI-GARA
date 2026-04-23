<%@ page import="com.quanli.quanligara.model.Invoice" %>
<%@ page import="com.quanli.quanligara.model.InvoiceLine" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Invoice</title>
    <%@ include file="/WEB-INF/views/common/user-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/user-navbar.jspf" %>
<div class="content">
    <div class="card">
        <%
            Invoice inv = (Invoice) request.getAttribute("invoice");
            String cp = request.getContextPath();
        %>
        <% if (inv != null) { %>
        <h1><%= inv.getInvoiceNumber() %></h1>
        <p>Status: <%= inv.getStatus() %> — <%= inv.getIssuedAt() %></p>
        <table>
            <thead>
            <tr><th>Type</th><th>Code</th><th>Name</th><th>Unit</th><th>Qty</th><th>Line</th></tr>
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
        <p><a href="<%= cp %>/home/invoices">← Back</a></p>
    </div>
</div>
</body>
</html>
