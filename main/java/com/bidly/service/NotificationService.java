package com.bidly.service;

import com.bidly.dao.NotificationDao;
import com.bidly.dao.UserDao;
import com.bidly.model.Notification;
import com.bidly.model.NotificationType;
import com.bidly.model.User;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;

@Stateless
public class NotificationService {

    @EJB
    private NotificationDao notificationDao;

    @EJB
    private UserDao userDao;

    public void createNotification(String userId, String title, String message,
                                   NotificationType type, Long auctionId) {
        User user = userDao.findById(userId);
        if (user == null) {
            return;
        }
        notificationDao.save(new Notification(user, title, message, type, auctionId));
    }

    public List<Notification> getAllUserNotifications(String userId) {
        return notificationDao.findAllByUser(userId);
    }

    public long getUnreadCount(String userId) {
        return notificationDao.countUnread(userId);
    }

    public void markAsRead(Long notificationId, String userId) {
        notificationDao.markAsRead(notificationId, userId);
    }

    public void markAllAsRead(String userId) {
        notificationDao.markAllAsRead(userId);
    }
}
