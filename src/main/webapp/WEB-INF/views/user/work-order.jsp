<%@ page import="com.quanli.quanligara.model.WorkOrder" %>
<%@ page import="com.quanli.quanligara.model.WorkOrderPartLine" %>
<%@ page import="com.quanli.quanligara.model.WorkOrderServiceLine" %>
<%@ page import="com.quanli.quanligara.model.enums.WorkOrderStatus" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Work Order</title>
    <%@ include file="/WEB-INF/views/common/user-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/user-navbar.jspf" %>
<div class="content">
    <div class="card">
        <h1>Work Order</h1>
        <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (request.getAttribute("message") != null) { %>
            <p><%= request.getAttribute("message") %></p>
        <% } %>
        <%
            WorkOrder wo = (WorkOrder) request.getAttribute("workOrder");
            String cp = request.getContextPath();
            BigDecimal subtotal = BigDecimal.ZERO;
            boolean editable = wo != null && wo.getStatus() != WorkOrderStatus.INVOICED;
        %>
        <% if (wo != null) { %>
        <p>Status: <strong><%= wo.getStatus() %></strong></p>
        <h2>Parts</h2>
        <table>
            <thead>
            <tr><th>Code</th><th>Name</th><th>Price</th><th>Qty</th><th>Line</th><% if (editable) { %><th></th><% } %></tr>
            </thead>
            <tbody>
            <% for (WorkOrderPartLine pl : wo.getPartLines()) {
                BigDecimal unit = pl.getSparePart().getUnitPrice();
                BigDecimal line = unit.multiply(BigDecimal.valueOf(pl.getQuantity()));
                subtotal = subtotal.add(line);
            %>
            <tr data-line-type="part">
                <td><%= pl.getSparePart().getCode() %></td>
                <td><%= pl.getSparePart().getName() %></td>
                <td><%= unit.toPlainString() %></td>
                <td>
                    <% if (editable) { %>
                    <form method="post" action="<%= cp %>/home/work-order/update-part" style="display:flex;gap:4px;">
                        <input type="hidden" name="lineId" value="<%= pl.getId() %>">
                        <input type="number" name="quantity" value="<%= pl.getQuantity() %>" min="1" style="width:70px">
                        <button type="submit">Update</button>
                    </form>
                    <% } else { %>
                    <%= pl.getQuantity() %>
                    <% } %>
                </td>
                <td><%= line.toPlainString() %></td>
                <% if (editable) { %>
                <td>
                    <form method="post" action="<%= cp %>/home/work-order/remove-part">
                        <input type="hidden" name="lineId" value="<%= pl.getId() %>">
                        <button type="submit">Remove</button>
                    </form>
                </td>
                <% } %>
            </tr>
            <% } %>
            </tbody>
        </table>
        <h2>Services</h2>
        <table>
            <thead>
            <tr><th>Code</th><th>Name</th><th>Price</th><th>Qty</th><th>Line</th><% if (editable) { %><th></th><% } %></tr>
            </thead>
            <tbody>
            <% for (WorkOrderServiceLine sl : wo.getServiceLines()) {
                BigDecimal unit = sl.getServiceOffering().getUnitPrice();
                BigDecimal line = unit.multiply(BigDecimal.valueOf(sl.getQuantity()));
                subtotal = subtotal.add(line);
            %>
            <tr data-line-type="service">
                <td><%= sl.getServiceOffering().getCode() %></td>
                <td><%= sl.getServiceOffering().getName() %></td>
                <td><%= unit.toPlainString() %></td>
                <td>
                    <% if (editable) { %>
                    <form method="post" action="<%= cp %>/home/work-order/update-service" style="display:flex;gap:4px;">
                        <input type="hidden" name="lineId" value="<%= sl.getId() %>">
                        <input type="number" name="quantity" value="<%= sl.getQuantity() %>" min="1" style="width:70px">
                        <button type="submit">Update</button>
                    </form>
                    <% } else { %>
                    <%= sl.getQuantity() %>
                    <% } %>
                </td>
                <td><%= line.toPlainString() %></td>
                <% if (editable) { %>
                <td>
                    <form method="post" action="<%= cp %>/home/work-order/remove-service">
                        <input type="hidden" name="lineId" value="<%= sl.getId() %>">
                        <button type="submit">Remove</button>
                    </form>
                </td>
                <% } %>
            </tr>
            <% } %>
            </tbody>
        </table>
        <p><strong>Subtotal (preview):</strong> <%= subtotal.toPlainString() %></p>
        <% if (editable) { %>
        <form method="post" action="<%= cp %>/home/work-order/submit">
            <button type="submit">Submit to Admin</button>
        </form>
        <% } else { %>
        <p class="muted">This work order is invoiced and read-only.</p>
        <% } %>
        <% } %>
    </div>
</div>
</body>
</html>
