<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String as = request.getParameter("as");
    boolean isAdminLogin = "admin".equalsIgnoreCase(as);
    boolean isCustomerLogin = "customer".equalsIgnoreCase(as) || "user".equalsIgnoreCase(as);
    String preUser = isAdminLogin ? "admin" : (isCustomerLogin ? "user" : "");
    String prePass = isAdminLogin ? "admin" : (isCustomerLogin ? "user" : "");
    String title = isAdminLogin ? "Admin Login" : (isCustomerLogin ? "Customer Login" : "Login");
%>
<!DOCTYPE html>
<html>
<head>
    <title>Gara Management - <%= title %></title>
    <style>
        body {
            font-family: Arial, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }
        .login-box {
            background: white;
            padding: 40px;
            border-radius: 10px;
            box-shadow: 0 0 20px rgba(0,0,0,0.1);
            width: 300px;
        }
        h2 {
            text-align: center;
            color: #333;
            margin-bottom: 30px;
        }
        .form-group {
            margin-bottom: 20px;
        }
        label {
            display: block;
            margin-bottom: 5px;
            color: #555;
        }
        input[type="text"], input[type="password"] {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 5px;
            box-sizing: border-box;
        }
        button {
            width: 100%;
            padding: 12px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 16px;
        }
        button:hover {
            background: #5a6fda;
        }
        .error {
            color: red;
            text-align: center;
            margin-bottom: 15px;
        }
        .info {
            text-align: center;
            margin-top: 20px;
            font-size: 12px;
            color: #777;
        }
    </style>
</head>
<body>
    <div class="login-box">
        <h2><%= title %></h2>
        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>
        <form action="<%= request.getContextPath() %>/login" method="post">
            <div class="form-group">
                <label>Username</label>
                <input type="text" name="username" value="<%= preUser %>" required>
            </div>
            <div class="form-group">
                <label>Password</label>
                <input type="password" name="password" value="<%= prePass %>" required>
            </div>
            <button type="submit">Login</button>
        </form>
        <div class="info">
            <p>Demo accounts:</p>
            <p>Admin: <strong>admin / admin</strong></p>
            <p>Customer: <strong>user / user</strong></p>
        </div>
    </div>
</body>
</html>
