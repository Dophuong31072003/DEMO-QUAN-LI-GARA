package com.quanli.quanligara.service;

import com.quanli.quanligara.dao.UserDAO;
import com.quanli.quanligara.model.User;

import com.quanli.quanligara.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userDAO.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtil.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public Optional<User> getUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    public boolean register(User user) {
        if (userDAO.existsByUsername(user.getUsername())) {
            return false;
        }
        String hashedPassword = PasswordUtil.hash(user.getPassword());
        user.setPassword(hashedPassword);
        userDAO.save(user);
        return true;
    }

    public boolean isAdmin(User user) {
        return user != null && user.isAdmin();
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public List<User> getAllActiveUsers() {
        return userDAO.findAllActive();
    }

    public void updateUser(User user) {
        userDAO.update(user);
    }

    public void deleteUser(Long id) {
        userDAO.delete(id);
    }

    public void deactivateUser(Long id) {
        userDAO.deactivate(id);
    }
}
