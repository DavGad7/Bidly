package com.bidly.model;

import com.bidly.util.DateUtil;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATIONS")
@NamedQueries({
    @NamedQuery(name = "Notification.findByUser",
                query = "SELECT n FROM Notification n WHERE n.user.id = :userId " +
                        "ORDER BY n.createdAt DESC"),
    @NamedQuery(name = "Notification.countUnread",
                query = "SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId " +
                        "AND n.readStatus = false")
})
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "RELATED_AUCTION_ID")
    private Long relatedAuctionId;

    @Column(name = "READ_STATUS", nullable = false)
    private boolean readStatus = false;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    public Notification() {
    }

    public Notification(User user, String title, String message,
                        NotificationType type, Long relatedAuctionId) {
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedAuctionId = relatedAuctionId;
        this.readStatus = false;
        this.createdAt = LocalDateTime.now();
    }

    public String getIconClass() {
        return type != null ? type.getIconClass() : "bi-bell";
    }

    public String getRelativeTime() {
        return DateUtil.formatRelativeTime(createdAt);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public Long getRelatedAuctionId() {
        return relatedAuctionId;
    }

    public void setRelatedAuctionId(Long relatedAuctionId) {
        this.relatedAuctionId = relatedAuctionId;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
