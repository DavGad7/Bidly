package com.bidly.controller;

import com.bidly.model.SessionUser;
import com.bidly.model.User;
import com.bidly.service.UserService;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@RequestScoped
public class RegisterBean {

    @EJB
    private UserService userService;

    @Inject
    private NavigationBean navigationBean;

    private String username;
    private String email;
    private String password;
    private String confirmPassword;
    private String firstName;
    private String lastName;
    private String errorMessage;

    public String register() {
        if (!password.equals(confirmPassword)) {
            errorMessage = "Passwords do not match.";
            return null;
        }

        try {
            User user = userService.register(username, email, password, firstName, lastName);
            navigationBean.login(new SessionUser(user));
            return "/index.xhtml?faces-redirect=true";
        } catch (IllegalArgumentException e) {
            errorMessage = e.getMessage();
            return null;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
