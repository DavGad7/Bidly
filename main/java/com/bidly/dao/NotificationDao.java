package com.bidly.dao;

import com.bidly.model.Notification;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class NotificationDao {

    @PersistenceContext(unitName = "BidlyPU")
    private EntityManager em;

    public List<Notification> findAllByUser(String userId) {
        return em.createNamedQuery("Notification.findByUser", Notification.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public long countUnread(String userId) {
        return em.createNamedQuery("Notification.countUnread", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    public void save(Notification notification) {
        em.persist(notification);
    }

    public void markAsRead(Long notificationId, String userId) {
        em.createQuery("UPDATE Notification n SET n.readStatus = true " +
                "WHERE n.id = :id AND n.user.id = :userId")
                .setParameter("id", notificationId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public void markAllAsRead(String userId) {
        em.createQuery("UPDATE Notification n SET n.readStatus = true " +
                "WHERE n.user.id = :userId AND n.readStatus = false")
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
