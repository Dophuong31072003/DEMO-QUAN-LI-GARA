<%@ page import="com.quanli.quanligara.model.Invoice" %>
<%@ page import="com.quanli.quanligara.model.User" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Customer Invoices</title>
    <%@ include file="/WEB-INF/views/common/admin-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/admin-navbar.jspf" %>
<div class="content">
    <div class="card">
        <%
            String cp = request.getContextPath();
            User customer = (User) request.getAttribute("customer");
        %>
        <h1>Invoices</h1>
        <p class="muted">
            Customer:
            <strong><%= customer != null ? (customer.getFullName() != null ? customer.getFullName() : customer.getUsername()) : "" %></strong>
        </p>

        <table>
            <thead>
            <tr><th>Number</th><th>Issued</th><th>Total</th><th>Payment</th><th>Actions</th></tr>
            </thead>
            <tbody>
            <%
                @SuppressWarnings("unchecked")
                List<Invoice> invoices = (List<Invoice>) request.getAttribute("invoices");
                if (invoices != null) {
                    for (Invoice i : invoices) {
            %>
            <tr>
                <td><%= i.getInvoiceNumber() %></td>
                <td><%= i.getIssuedAt() != null ? i.getIssuedAt().toString() : "" %></td>
                <td><%= i.getTotalAmount() != null ? i.getTotalAmount().toPlainString() : "" %></td>
                <td><%= i.getPaidAt() == null ? "UNPAID" : ("PAID (" + i.getPaidAt() + ")") %></td>
                <td>
                    <a class="btn" href="<%= cp %>/admin/invoices/detail?id=<%= i.getId() %>">View</a>
                    <a class="btn secondary" href="<%= cp %>/admin/invoices/detail?id=<%= i.getId() %>&print=1" target="_blank" rel="noopener">Print</a>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>

        <% if (customer != null) { %>
        <p class="no-print">
            <a href="<%= cp %>/admin/work-orders/new?userId=<%= customer.getId() %>">Open quotation</a>
            ·
            <a href="<%= cp %>/admin/customers">← Customers</a>
        </p>
        <% } %>
    </div>
</div>
</body>
</html>

