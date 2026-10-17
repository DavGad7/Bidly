package com.bidly.controller;

import com.bidly.model.Notification;
import com.bidly.service.NotificationService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class NotificationBean implements Serializable {

    @EJB
    private NotificationService notificationService;

    @Inject
    private NavigationBean navigationBean;

    private List<Notification> notifications;

    @PostConstruct
    public void init() {
        loadNotifications();
    }

    private void loadNotifications() {
        if (navigationBean.isLoggedIn()) {
            notifications = notificationService.getAllUserNotifications(
                    navigationBean.getLoggedInUser().getId());
        }
    }

    public void markAsRead(Long notificationId) {
        if (!navigationBean.isLoggedIn()) return;
        notificationService.markAsRead(notificationId, navigationBean.getLoggedInUser().getId());
        loadNotifications();
        navigationBean.refreshNotificationCount();
    }

    public void markAllAsRead() {
        if (navigationBean.isLoggedIn()) {
            notificationService.markAllAsRead(navigationBean.getLoggedInUser().getId());
            loadNotifications();
            navigationBean.refreshNotificationCount();
        }
    }

    public List<Notification> getNotifications() {
        return notifications;
    }
}
