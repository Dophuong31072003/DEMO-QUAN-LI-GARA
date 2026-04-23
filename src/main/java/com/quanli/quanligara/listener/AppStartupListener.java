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
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
