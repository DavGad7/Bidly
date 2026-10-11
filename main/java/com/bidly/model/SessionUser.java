package com.bidly.model;

import java.io.Serializable;

public class SessionUser implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String SESSION_KEY = "user";

    private final String id;
    private final String username;
    private final String displayName;
    private final UserRole role;

    public SessionUser(User user) {
        id = user.getId();
        username = user.getUsername();
        displayName = user.getDisplayName();
        role = user.getRole();
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    @Override
    public String toString() {
        return "SessionUser{id=" + id + ", username='" + username + "'}";
    }
}

