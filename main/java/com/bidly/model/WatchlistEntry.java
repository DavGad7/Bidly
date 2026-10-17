package com.bidly.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "WATCHLIST",
       uniqueConstraints = @UniqueConstraint(columnNames = {"USER_ID", "AUCTION_ID"}))
@NamedQueries({
    @NamedQuery(name = "WatchlistEntry.findByUser",
                query = "SELECT w FROM WatchlistEntry w WHERE w.user.id = :userId " +
                        "ORDER BY w.addedAt DESC"),
    @NamedQuery(name = "WatchlistEntry.findByUserAndAuction",
                query = "SELECT w FROM WatchlistEntry w " +
                        "WHERE w.user.id = :userId AND w.auction.id = :auctionId"),
    @NamedQuery(name = "WatchlistEntry.findWatchersForAuction",
                query = "SELECT w.user.id FROM WatchlistEntry w " +
                        "WHERE w.auction.id = :auctionId")
})
public class WatchlistEntry implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ADDED_AT", nullable = false)
    private LocalDateTime addedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "AUCTION_ID", nullable = false)
    private Auction auction;

    public WatchlistEntry() {
    }

    public WatchlistEntry(User user, Auction auction) {
        this.user = user;
        this.auction = auction;
        this.addedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    @Override
    public String toString() {
        return "WatchlistEntry{id=" + id +
               ", userId=" + (user != null ? user.getId() : null) +
               ", auctionId=" + (auction != null ? auction.getId() : null) + '}';
    }
}
