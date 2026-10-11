package com.bidly.controller;

import com.bidly.model.SessionUser;
import com.bidly.model.User;
import com.bidly.service.AccountBlockedException;
import com.bidly.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@RequestScoped
public class LoginBean {

    @EJB
    private UserService userService;

    @Inject
    private NavigationBean navigationBean;

    private String username;
    private String password;
    private String errorMessage;

    @PostConstruct
    public void init() {
        Object notice = FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().remove("loginNotice");
        if (notice != null) {
            errorMessage = notice.toString();
        }
    }

    public String login() {
        User user;
        try {
            user = userService.login(username, password);
        } catch (AccountBlockedException e) {
            errorMessage = AccountBlockedException.messageFor(e.getReason());
            return null;
        }

        if (user == null) {
            errorMessage = "Invalid username or password.";
            return null;
        }

        navigationBean.login(new SessionUser(user));

        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        String redirectUrl = (String) ec.getSessionMap().remove("redirectUrl");
        // relative URLs only
        if (redirectUrl != null && !redirectUrl.isBlank() && !redirectUrl.contains("://")) {
            try {
                ec.redirect(redirectUrl);
                return null;
            } catch (Exception ignored) {
            }
        }

        return "/index.xhtml?faces-redirect=true";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
