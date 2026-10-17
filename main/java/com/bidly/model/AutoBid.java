package com.bidly.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "AUTO_BIDS",
       uniqueConstraints = @UniqueConstraint(columnNames = {"AUCTION_ID", "BIDDER_ID"}))
@NamedQueries({
    @NamedQuery(name = "AutoBid.findByAuctionAndBidder",
                query = "SELECT a FROM AutoBid a " +
                        "WHERE a.auction.id = :auctionId AND a.bidder.id = :bidderId"),
    @NamedQuery(name = "AutoBid.findActiveByAuction",
                query = "SELECT a FROM AutoBid a " +
                        "WHERE a.auction.id = :auctionId AND a.active = true " +
                        "ORDER BY a.maxAmount DESC, a.createdAt ASC")
})
public class AutoBid implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "MAX_AMOUNT", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxAmount;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "AUCTION_ID", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BIDDER_ID", nullable = false)
    private User bidder;

    public AutoBid() {
    }

    public AutoBid(Auction auction, User bidder, BigDecimal maxAmount) {
        this.auction = auction;
        this.bidder = bidder;
        this.maxAmount = maxAmount;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public User getBidder() {
        return bidder;
    }

    public void setBidder(User bidder) {
        this.bidder = bidder;
    }

    @Override
    public String toString() {
        return "AutoBid " + id + " (max " + maxAmount + (active ? ")" : ", off)");
    }
}
