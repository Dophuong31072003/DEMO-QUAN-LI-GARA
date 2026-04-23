<%@ page import="com.quanli.quanligara.model.WorkOrder" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Work Orders</title>
    <%@ include file="/WEB-INF/views/common/admin-styles.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/common/admin-navbar.jspf" %>
<div class="content">
    <div class="card">
        <h1>Work Orders</h1>
        <p class="muted">Draft and submitted work orders (open pipeline).</p>
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>User</th>
                <th>Status</th>
                <th>Part lines</th>
                <th>Service lines</th>
                <th>Submitted</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <%
                @SuppressWarnings("unchecked")
                List<WorkOrder> orders = (List<WorkOrder>) request.getAttribute("workOrders");
                @SuppressWarnings("unchecked")
                Map<Long, int[]> lineCounts = (Map<Long, int[]>) request.getAttribute("lineCounts");
                String cp = request.getContextPath();
                if (orders != null) {
                    for (WorkOrder w : orders) {
                        int[] lc = lineCounts != null ? lineCounts.get(w.getId()) : new int[]{0, 0};
                        int pc = lc != null ? lc[0] : 0;
                        int sc = lc != null ? lc[1] : 0;
            %>
            <tr>
                <td><%= w.getId() %></td>
                <td><%= w.getUser() != null ? w.getUser().getFullName() : "" %> (<%= w.getUser() != null ? w.getUser().getUsername() : "" %>)</td>
                <td><%= w.getStatus() %></td>
                <td><%= pc %></td>
                <td><%= sc %></td>
                <td><%= w.getSubmittedAt() != null ? w.getSubmittedAt().toString() : "—" %></td>
                <td><a class="btn" href="<%= cp %>/admin/work-orders/detail?id=<%= w.getId() %>">Detail</a></td>
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
