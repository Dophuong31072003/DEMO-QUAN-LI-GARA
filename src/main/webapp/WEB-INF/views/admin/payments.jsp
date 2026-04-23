<%@ page import="com.quanli.quanligara.model.Invoice" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Payments</title>
    <%@ include file="/WEB-INF/views/common/admin-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/admin-navbar.jspf" %>
<div class="content">
    <div class="card">
        <h1>Payments</h1>
        <p class="muted">Search invoices by customer name or username. Print or record a payment acknowledgement (UI only).</p>
        <%
            String cp = request.getContextPath();
            String qAttr = request.getAttribute("q") == null ? "" : String.valueOf(request.getAttribute("q"));
        %>
        <% if (request.getAttribute("message") != null) { %>
            <p class="message"><%= request.getAttribute("message") %></p>
        <% } %>
        <form method="get" action="<%= cp %>/admin/payments">
            <label>Customer search <input type="text" name="q" value="<%= qAttr %>"></label>
            <button type="submit">Search</button>
        </form>
        <table>
            <thead>
            <tr><th>Invoice #</th><th>Customer</th><th>Issued</th><th>Total</th><th>Actions</th></tr>
            </thead>
            <tbody>
            <%
                @SuppressWarnings("unchecked")
                List<Invoice> invoices = (List<Invoice>) request.getAttribute("invoices");
                if (invoices != null) {
                    for (Invoice inv : invoices) {
                        String cust = inv.getUser() != null ? inv.getUser().getFullName() : "";
                        if (cust == null || cust.isBlank()) {
                            cust = inv.getUser() != null ? inv.getUser().getUsername() : "";
                        }
            %>
            <tr>
                <td><%= inv.getInvoiceNumber() %></td>
                <td><%= cust %></td>
                <td><%= inv.getIssuedAt() %></td>
                <td><%= inv.getTotalAmount() != null ? inv.getTotalAmount().toPlainString() : "" %></td>
                <td>
                    <a href="<%= cp %>/admin/invoices/detail?id=<%= inv.getId() %>">View</a>
                    ·
                    <a href="<%= cp %>/admin/invoices/detail?id=<%= inv.getId() %>&print=1" target="_blank" rel="noopener">Print</a>
                    ·
                    <% if (inv.getPaidAt() == null) { %>
                        <form method="post" action="<%= cp %>/admin/payments/confirm" style="display:inline;">
                            <input type="hidden" name="invoiceId" value="<%= inv.getId() %>">
                            <input type="hidden" name="invoiceNumber" value="<%= inv.getInvoiceNumber() %>">
                            <input type="hidden" name="q" value="<%= qAttr %>">
                            <button type="submit">Confirm payment</button>
                        </form>
                    <% } else { %>
                        <span class="muted">Paid</span>
                    <% } %>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
        <p><a href="<%= cp %>/admin">← Dashboard</a></p>
    </div>
</div>
</body>
</html>
