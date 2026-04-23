<%@ page import="com.quanli.quanligara.model.Invoice" %>
<%@ page import="java.util.List" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Invoices</title>
    <%@ include file="/WEB-INF/views/common/user-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/user-navbar.jspf" %>
<div class="content">
    <div class="card">
        <h1>My invoices</h1>
        <table>
            <thead>
            <tr><th>Number</th><th>Issued</th><th>Total</th><th></th></tr>
            </thead>
            <tbody>
            <%
                @SuppressWarnings("unchecked")
                List<Invoice> invoices = (List<Invoice>) request.getAttribute("invoices");
                String cp = request.getContextPath();
                if (invoices != null) {
                    for (Invoice i : invoices) {
            %>
            <tr>
                <td><%= i.getInvoiceNumber() %></td>
                <td><%= i.getIssuedAt() != null ? i.getIssuedAt().toString() : "" %></td>
                <td><%= i.getTotalAmount() != null ? i.getTotalAmount().toPlainString() : "" %></td>
                <td><a href="<%= cp %>/home/invoices/detail?id=<%= i.getId() %>">Detail</a></td>
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
