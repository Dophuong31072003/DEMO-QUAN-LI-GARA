<%@ page import="com.quanli.quanligara.model.WorkOrder" %>
<%@ page import="com.quanli.quanligara.model.WorkOrderPartLine" %>
<%@ page import="com.quanli.quanligara.model.WorkOrderServiceLine" %>
<%@ page import="com.quanli.quanligara.model.SparePart" %>
<%@ page import="com.quanli.quanligara.model.ServiceOffering" %>
<%@ page import="com.quanli.quanligara.model.enums.WorkOrderStatus" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.util.List" %>
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
            boolean editable = wo != null && wo.getStatus() != WorkOrderStatus.INVOICED;
            @SuppressWarnings("unchecked")
            List<SparePart> catalogParts = (List<SparePart>) request.getAttribute("catalogParts");
            @SuppressWarnings("unchecked")
            List<ServiceOffering> catalogServices = (List<ServiceOffering>) request.getAttribute("catalogServices");
        %>
        <% if (request.getAttribute("message") != null) { %>
            <p class="message"><%= request.getAttribute("message") %></p>
        <% } %>
        <% if (request.getAttribute("error") != null) { %>
            <p class="error"><%= request.getAttribute("error") %></p>
        <% } %>
        <% if (wo != null) { %>
        <h1>Work Order #<%= wo.getId() %></h1>
        <p class="muted">Customer: <%= wo.getUser() != null ? wo.getUser().getFullName() : "" %> — Status: <strong><%= wo.getStatus() %></strong></p>

        <div class="grid-2" style="margin-top:14px;">
            <!-- Left: Catalog (search + add) -->
            <div class="section">
                <h2>Catalog</h2>
                <div class="row">
                    <label>Search parts/services
                        <input id="catalogSearch" type="text" placeholder="Type code or name…">
                    </label>
                    <div class="tight muted small">Tip: search filters both tables.</div>
                </div>

                <h3 style="margin-bottom:6px;">Parts</h3>
                <div class="scrollbox">
                    <table class="table-compact" id="partsCatalogTable">
                        <thead>
                        <tr><th>Code</th><th>Name</th><th>Price</th><th>Stock</th><th class="no-print">Add</th></tr>
                        </thead>
                        <tbody>
                        <% if (catalogParts != null) { for (SparePart p : catalogParts) { %>
                        <tr data-filter="<%= (p.getCode() + " " + p.getName()).toLowerCase() %>">
                            <td><%= p.getCode() %></td>
                            <td><%= p.getName() %></td>
                            <td><%= p.getUnitPrice() != null ? p.getUnitPrice().toPlainString() : "" %></td>
                            <td><%= p.getStockQuantity() != null ? p.getStockQuantity() : "" %></td>
                            <td class="no-print">
                                <% if (editable) { %>
                                <form method="post" action="<%= cp %>/admin/work-orders/add-part" class="inline catalog-add">
                                    <input type="hidden" name="workOrderId" value="<%= wo.getId() %>">
                                    <input type="hidden" name="partId" value="<%= p.getId() %>">
                                    <input class="qty" type="number" name="quantity" min="1" value="1" required>
                                    <button type="submit">Add</button>
                                </form>
                                <% } else { %>
                                <span class="muted small">Locked</span>
                                <% } %>
                            </td>
                        </tr>
                        <% } } %>
                        </tbody>
                    </table>
                </div>

                <h3 style="margin:16px 0 6px;">Services</h3>
                <div class="scrollbox">
                    <table class="table-compact" id="servicesCatalogTable">
                        <thead>
                        <tr><th>Code</th><th>Name</th><th>Price</th><th class="no-print">Add</th></tr>
                        </thead>
                        <tbody>
                        <% if (catalogServices != null) { for (ServiceOffering s : catalogServices) { %>
                        <tr data-filter="<%= (s.getCode() + " " + s.getName()).toLowerCase() %>">
                            <td><%= s.getCode() %></td>
                            <td><%= s.getName() %></td>
                            <td><%= s.getUnitPrice() != null ? s.getUnitPrice().toPlainString() : "" %></td>
                            <td class="no-print">
                                <% if (editable) { %>
                            <form method="post" action="<%= cp %>/admin/work-orders/add-service" class="inline catalog-add">
                                    <input type="hidden" name="workOrderId" value="<%= wo.getId() %>">
                                    <input type="hidden" name="serviceId" value="<%= s.getId() %>">
                                    <input class="qty" type="number" name="quantity" min="1" value="1" required>
                                    <button type="submit">Add</button>
                                </form>
                                <% } else { %>
                                <span class="muted small">Locked</span>
                                <% } %>
                            </td>
                        </tr>
                        <% } } %>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Right: Work order -->
            <div class="section">
                <h2>Quotation (Work Order)</h2>

                <h3 style="margin-bottom:6px;">Parts</h3>
                <table class="table-compact">
                    <thead>
                    <tr><th>Code</th><th>Name</th><th>Unit price</th><th>Qty</th><th>Line</th><% if (editable) { %><th class="no-print"></th><% } %></tr>
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
                        <% if (editable) { %>
                        <td class="no-print">
                            <form method="post" action="<%= cp %>/admin/work-orders/update-part" class="inline line-update">
                                <input type="hidden" name="workOrderId" value="<%= wo.getId() %>">
                                <input type="hidden" name="lineId" value="<%= pl.getId() %>">
                                <input class="qty" type="number" name="quantity" min="1" value="<%= pl.getQuantity() %>">
                                <button type="submit">Update</button>
                            </form>
                        </td>
                        <td><%= line.toPlainString() %></td>
                        <td class="no-print">
                            <form method="post" action="<%= cp %>/admin/work-orders/remove-part" class="inline" onsubmit="return confirm('Remove this line?');">
                                <input type="hidden" name="workOrderId" value="<%= wo.getId() %>">
                                <input type="hidden" name="lineId" value="<%= pl.getId() %>">
                                <button type="submit">Remove</button>
                            </form>
                        </td>
                        <% } else { %>
                        <td><%= pl.getQuantity() %></td>
                        <td><%= line.toPlainString() %></td>
                        <% } %>
                    </tr>
                    <% } %>
                    </tbody>
                </table>

                <h3 style="margin:16px 0 6px;">Services</h3>
                <table class="table-compact">
                    <thead>
                    <tr><th>Code</th><th>Name</th><th>Unit price</th><th>Qty</th><th>Line</th><% if (editable) { %><th class="no-print"></th><% } %></tr>
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
                        <% if (editable) { %>
                        <td class="no-print">
                            <form method="post" action="<%= cp %>/admin/work-orders/update-service" class="inline line-update">
                                <input type="hidden" name="workOrderId" value="<%= wo.getId() %>">
                                <input type="hidden" name="lineId" value="<%= sl.getId() %>">
                                <input class="qty" type="number" name="quantity" min="1" value="<%= sl.getQuantity() %>">
                                <button type="submit">Update</button>
                            </form>
                        </td>
                        <td><%= line.toPlainString() %></td>
                        <td class="no-print">
                            <form method="post" action="<%= cp %>/admin/work-orders/remove-service" class="inline" onsubmit="return confirm('Remove this line?');">
                                <input type="hidden" name="workOrderId" value="<%= wo.getId() %>">
                                <input type="hidden" name="lineId" value="<%= sl.getId() %>">
                                <button type="submit">Remove</button>
                            </form>
                        </td>
                        <% } else { %>
                        <td><%= sl.getQuantity() %></td>
                        <td><%= line.toPlainString() %></td>
                        <% } %>
                    </tr>
                    <% } %>
                    </tbody>
                </table>

                <p style="margin-top:14px;"><strong>Preview total:</strong> <%= previewTotal.toPlainString() %></p>

                <% if (Boolean.TRUE.equals(canIssue) && wo.getStatus() != WorkOrderStatus.INVOICED) { %>
                <form method="post" action="<%= cp %>/admin/invoices/issue" class="no-print">
                    <input type="hidden" name="workOrderId" value="<%= wo.getId() %>">
                    <button type="submit">Issue Invoice</button>
                </form>
                <% } else if (wo.getStatus() == WorkOrderStatus.INVOICED) { %>
                <p class="muted">This work order is already invoiced.</p>
                <% } else { %>
                <p class="muted">Add at least one line before issuing an invoice.</p>
                <% } %>

                <p class="no-print"><a href="<%= cp %>/admin/work-orders">← Back to list</a> · <a href="<%= cp %>/admin/customers">Customers</a></p>
            </div>
        </div>

        <script>
            (function () {
                const input = document.getElementById('catalogSearch');
                if (!input) return;
                const filter = () => {
                    const q = (input.value || '').trim().toLowerCase();
                    const rows = document.querySelectorAll('#partsCatalogTable tbody tr, #servicesCatalogTable tbody tr');
                    rows.forEach(r => {
                        const hay = (r.getAttribute('data-filter') || '');
                        r.style.display = (!q || hay.includes(q)) ? '' : 'none';
                    });
                };
                input.addEventListener('input', filter);
                filter();
            })();
        </script>
        <% } %>
    </div>
</div>
</body>
</html>
