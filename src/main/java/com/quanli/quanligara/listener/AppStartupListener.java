package com.quanli.quanligara.listener;

import com.quanli.quanligara.model.User;
import com.quanli.quanligara.service.AuthService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {

    private AuthService authService;

    public void contextInitialized(ServletContextEvent sce) {
        authService = new AuthService();
        seedUsers();
    }

    private void seedUsers() {
        if (authService.getUserByUsername("admin").isEmpty()) {
            User admin = new User("admin", "admin", "System Administrator", "admin@quanligara.com", "0900000000", true);
            authService.register(admin);
        }

        if (authService.getUserByUsername("user").isEmpty()) {
            User user = new User("user", "user", "Standard User", "user@quanligara.com", "0900000001", false);
            authService.register(user);
        }

        String[] fullNames = new String[]{
                "James Wilson",
                "Emily Johnson",
                "Michael Brown",
                "Olivia Davis",
                "Daniel Miller",
                "Sophia Anderson",
                "William Taylor",
                "Ava Thomas",
                "Benjamin Moore",
                "Mia Jackson"
        };
        for (int i = 1; i <= 10; i++) {
            String username = "user" + i;
            if (authService.getUserByUsername(username).isPresent()) {
                continue;
            }
            String fullName = fullNames[i - 1];
            String email = "user" + i + "@example.com";
            String phone = String.format("09000001%02d", i);
            User u = new User(username, "password", fullName, email, phone, false);
            authService.register(u);
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
