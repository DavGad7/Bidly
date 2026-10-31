package com.bidly.model;

public enum NotificationType {
    NEW_BID,
    OUTBID,
    AUCTION_WON,
    AUCTION_CLOSED_SELLER,
    AUCTION_CLOSED_NO_BIDS,
    WATCHLIST_UPDATE;

    public String getIconClass() {
        switch (this) {
            case NEW_BID:
                return "bi-cash-coin";
            case OUTBID:
                return "bi-arrow-down-circle";
            case AUCTION_WON:
                return "bi-trophy";
            case AUCTION_CLOSED_SELLER:
                return "bi-check-circle";
            case AUCTION_CLOSED_NO_BIDS:
                return "bi-x-circle";
            case WATCHLIST_UPDATE:
                return "bi-eye";
            default:
                return "bi-bell";
        }
    }
}
