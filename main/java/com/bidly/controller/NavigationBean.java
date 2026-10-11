package com.bidly.controller;

import com.bidly.model.SessionUser;
import com.bidly.service.NotificationService;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class NavigationBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private NotificationService notificationService;

    private SessionUser loggedInUser;

    private long unreadNotificationCount;

    public void login(SessionUser user) {
        loggedInUser = user;
        FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put(SessionUser.SESSION_KEY, user);
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public boolean isAdmin() {
        return loggedInUser != null && loggedInUser.isAdmin();
    }

    public String getUserId() {
        return loggedInUser != null ? loggedInUser.getId() : null;
    }

    public String logout() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    public void refreshNotificationCount() {
        if (loggedInUser != null) {
            unreadNotificationCount = notificationService.getUnreadCount(loggedInUser.getId());
        } else {
            unreadNotificationCount = 0;
        }
    }

    public SessionUser getLoggedInUser() {
        return loggedInUser;
    }

    public long getUnreadNotificationCount() {
        return unreadNotificationCount;
    }
}
