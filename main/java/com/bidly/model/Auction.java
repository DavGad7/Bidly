package com.bidly.model;

import com.bidly.util.DateUtil;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "AUCTIONS", indexes = {
    @Index(name = "idx_auction_status", columnList = "STATUS"),
    @Index(name = "idx_auction_endtime", columnList = "END_TIME"),
    @Index(name = "idx_auction_category", columnList = "CATEGORY_ID")
})
@NamedQueries({
    @NamedQuery(name = "Auction.findBySeller",
                query = "SELECT a FROM Auction a WHERE a.seller.id = :sellerId ORDER BY a.createdAt DESC"),
    @NamedQuery(name = "Auction.findExpiredActive",
                query = "SELECT a FROM Auction a WHERE a.status = com.bidly.model.AuctionStatus.ACTIVE " +
                        "AND a.endTime <= :now"),
    @NamedQuery(name = "Auction.countByStatus",
                query = "SELECT COUNT(a) FROM Auction a WHERE a.status = :status")
})
public class Auction implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum SuspendedBy {
        SELLER, ADMIN, BLOCK
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(name = "START_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal startPrice;

    @Column(name = "CURRENT_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "BUY_NOW_PRICE", precision = 10, scale = 2)
    private BigDecimal buyNowPrice;

    @Column(name = "START_TIME", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "END_TIME", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private AuctionStatus status = AuctionStatus.ACTIVE;

    @Column(name = "VIEW_COUNT")
    private int viewCount = 0;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "THUMBNAIL_PHOTO_ID")
    private Long thumbnailPhotoId;

    @Column(name = "CONTACT_INFO", length = 500)
    private String contactInfo;

    @Enumerated(EnumType.STRING)
    @Column(name = "SUSPENDED_BY", length = 10)
    private SuspendedBy suspendedBy;

    @Column(name = "SUSPEND_REASON", length = 500)
    private String suspendReason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SELLER_ID", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "WINNER_ID")
    private User winner;

    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true,
               fetch = FetchType.LAZY)
    @OrderBy("position ASC")
    private List<AuctionPhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "auction", fetch = FetchType.LAZY)
    @OrderBy("bidTime DESC")
    private List<Bid> bids = new ArrayList<>();

    public Auction() {
    }

    public String getTimeRemaining() {
        if (status == AuctionStatus.SUSPENDED) {
            return "Suspended";
        }
        if (status != AuctionStatus.ACTIVE) {
            return "Ended";
        }
        return DateUtil.formatTimeRemaining(endTime);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endTime);
    }

    public boolean hasBuyNow() {
        return buyNowPrice != null;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(BigDecimal startPrice) {
        this.startPrice = startPrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getBuyNowPrice() {
        return buyNowPrice;
    }

    public void setBuyNowPrice(BigDecimal buyNowPrice) {
        this.buyNowPrice = buyNowPrice;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getThumbnailPhotoId() {
        return thumbnailPhotoId;
    }

    public void setThumbnailPhotoId(Long thumbnailPhotoId) {
        this.thumbnailPhotoId = thumbnailPhotoId;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public SuspendedBy getSuspendedBy() {
        return suspendedBy;
    }

    public void setSuspendedBy(SuspendedBy suspendedBy) {
        this.suspendedBy = suspendedBy;
    }

    public String getSuspendReason() {
        return suspendReason;
    }

    public void setSuspendReason(String suspendReason) {
        this.suspendReason = suspendReason;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    public List<AuctionPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<AuctionPhoto> photos) {
        this.photos = photos;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public void setBids(List<Bid> bids) {
        this.bids = bids;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Auction other = (Auction) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
