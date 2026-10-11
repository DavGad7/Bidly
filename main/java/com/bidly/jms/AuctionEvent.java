package com.bidly.jms;

import com.bidly.model.NotificationType;
import java.io.Serializable;
import java.math.BigDecimal;

public class AuctionEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final NotificationType type;
    private final Long auctionId;
    private final String auctionTitle;
    private final String targetUserId;
    private final String bidderName;
    private final BigDecimal bidAmount;

    public AuctionEvent(NotificationType type, Long auctionId, String auctionTitle,
                        String targetUserId, String bidderName, BigDecimal bidAmount) {
        this.type = type;
        this.auctionId = auctionId;
        this.auctionTitle = auctionTitle;
        this.targetUserId = targetUserId;
        this.bidderName = bidderName;
        this.bidAmount = bidAmount;
    }

    public NotificationType getType() {
        return type;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public String getAuctionTitle() {
        return auctionTitle;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public String getBidderName() {
        return bidderName;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    @Override
    public String toString() {
        return "AuctionEvent{type=" + type + ", auctionId=" + auctionId +
               ", targetUserId=" + targetUserId + '}';
    }
}

