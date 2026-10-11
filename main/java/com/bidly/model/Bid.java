package com.bidly.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "BIDS")
@NamedQueries({
    @NamedQuery(name = "Bid.findByAuction",
                query = "SELECT b FROM Bid b WHERE b.auction.id = :auctionId AND b.bidder.blocked = false " +
                        "ORDER BY b.amount DESC"),
    @NamedQuery(name = "Bid.findByBidder",
                query = "SELECT b FROM Bid b WHERE b.bidder.id = :bidderId " +
                        "ORDER BY b.bidTime DESC"),
    @NamedQuery(name = "Bid.findHighestForAuction",
                query = "SELECT b FROM Bid b WHERE b.auction.id = :auctionId AND b.bidder.blocked = false " +
                        "ORDER BY b.amount DESC, b.bidTime ASC"),
    @NamedQuery(name = "Bid.countByAuction",
                query = "SELECT COUNT(b) FROM Bid b WHERE b.auction.id = :auctionId AND b.bidder.blocked = false")
})
public class Bid implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "BID_TIME", nullable = false)
    private LocalDateTime bidTime;

    @Column(name = "AUTOMATIC_BID", nullable = false)
    private boolean automaticBid = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "AUCTION_ID", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BIDDER_ID", nullable = false)
    private User bidder;

    public Bid() {
    }

    public Bid(Auction auction, User bidder, BigDecimal amount,
               LocalDateTime bidTime, boolean automaticBid) {
        this.auction = auction;
        this.bidder = bidder;
        this.amount = amount;
        this.bidTime = bidTime;
        this.automaticBid = automaticBid;
    }

    // masked username for bid history
    public String getAnonymousDisplayName() {
        if (bidder == null || bidder.getUsername() == null) {
            return "User ***";
        }
        String uname = bidder.getUsername();
        if (uname.length() <= 2) {
            return uname.charAt(0) + "***";
        }
        return uname.charAt(0) + "***" + uname.charAt(uname.length() - 1);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }

    public boolean isAutomaticBid() {
        return automaticBid;
    }

    public void setAutomaticBid(boolean automaticBid) {
        this.automaticBid = automaticBid;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bid other = (Bid) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Bid #" + id + ": $" + amount + " at " + bidTime;
    }
}
