<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Gara Management</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
            margin: 0;
            background: linear-gradient(135deg, #111827 0%, #374151 100%);
        }
        .card {
            background: white;
            padding: 40px;
            border-radius: 12px;
            box-shadow: 0 0 24px rgba(0,0,0,0.15);
            width: 420px;
        }
        h1 { margin: 0 0 12px 0; color: #111827; }
        p { margin: 0 0 24px 0; color: #4b5563; }
        .actions { display: flex; gap: 12px; }
        a.btn {
            flex: 1;
            text-align: center;
            padding: 12px 14px;
            border-radius: 8px;
            text-decoration: none;
            font-weight: 600;
            border: 1px solid #e5e7eb;
        }
        a.btn.admin { background: #111827; color: #fff; }
        a.btn.customer { background: #667eea; color: #fff; border-color: #667eea; }
        .hint { margin-top: 18px; font-size: 12px; color: #6b7280; }
        code { background: #f3f4f6; padding: 2px 6px; border-radius: 6px; }
    </style>
</head>
<body>
<div class="card">
    <h1>Gara Management</h1>
    <p>Please choose how you want to sign in.</p>
    <div class="actions">
        <a class="btn admin" href="<%= request.getContextPath() %>/login?as=admin">Admin Login</a>
        <a class="btn customer" href="<%= request.getContextPath() %>/login?as=customer">Customer Login</a>
    </div>
    <div class="hint">
        Demo accounts: <code>admin/admin</code> and <code>user/user</code>
    </div>
</div>
</body>
</html>