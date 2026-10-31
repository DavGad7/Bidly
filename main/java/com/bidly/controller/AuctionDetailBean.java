package com.bidly.controller;

import com.bidly.model.*;
import com.bidly.service.*;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Named
@ViewScoped
public class AuctionDetailBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private AuctionService auctionService;

    @EJB
    private BidService bidService;

    @EJB
    private AutoBidService autoBidService;

    @EJB
    private PhotoService photoService;

    @EJB
    private WatchlistService watchlistService;

    @Inject
    private NavigationBean navigationBean;

    private Long auctionId;

    private Auction auction;
    private List<AuctionPhoto> photos;
    private List<Bid> bids;
    private BigDecimal bidAmount;
    private BigDecimal autoBidMax;
    private BigDecimal minimumBid;
    private long bidCount;
    private boolean watching;
    private AutoBid existingAutoBid;
    private String errorMessage;
    private String successMessage;

    public void loadAuction() {
        if (auctionId == null) {
            errorMessage = "Missing auction id";
            return;
        }

        auction = auctionService.getAuction(auctionId);
        if (auction == null) {
            errorMessage = "Auction not found.";
            return;
        }

        photos = photoService.getAuctionPhotos(auctionId);
        bids = bidService.getAuctionBids(auctionId);
        bidCount = bidService.getBidCount(auctionId);
        minimumBid = bidService.getMinimumBidAmount(auctionId);

        if (navigationBean.isLoggedIn()) {
            String userId = navigationBean.getLoggedInUser().getId();
            watching = watchlistService.isWatching(userId, auctionId);
            existingAutoBid = autoBidService.getAutoBid(auctionId, userId);
        }
    }

    public void placeBid() {
        if (!navigationBean.isLoggedIn()) {
            showMessage("You need to login before bidding.", true);
            return;
        }
        try {
            bidService.placeBid(auctionId, navigationBean.getUserId(), bidAmount);
            showMessage("Bid of $" + bidAmount.toPlainString() + " placed!", false);
            bidAmount = null;
            reloadData();
        } catch (IllegalArgumentException | IllegalStateException e) {
            showMessage(e.getMessage(), true);
        } catch (Exception e) {
            showMessage("Error placing bid, please try again", true);
        }
    }

    public void setAutoBid() {
        errorMessage = null;
        successMessage = null;

        if (!navigationBean.isLoggedIn()) {
            errorMessage = "Log in first.";
            return;
        }

        try {
            autoBidService.setAutoBid(auctionId, navigationBean.getUserId(), autoBidMax);
            successMessage = "Auto-bid set with maximum of $" + autoBidMax.toPlainString() + ".";
            autoBidMax = null;
            reloadData();
        } catch (IllegalArgumentException e) {
            errorMessage = e.getMessage();
        } catch (Exception e) {
            errorMessage = "Auto bid failed.";
        }
    }

    public void cancelAutoBid() {
        if (navigationBean.isLoggedIn()) {
            autoBidService.cancelAutoBid(auctionId, navigationBean.getLoggedInUser().getId());
            showMessage("Auto-bid cancelled.", false);
            reloadData();
        }
    }

    public String adminDeleteAuction() {
        if (navigationBean.isAdmin() && auction != null) {
            auctionService.deleteAuction(auctionId, navigationBean.getUserId());
            return "/index?faces-redirect=true";
        }
        return null;
    }

    public void toggleWatchlist() {
        if (!navigationBean.isLoggedIn()) return;
        String userId = navigationBean.getLoggedInUser().getId();
        if (watching) {
            watchlistService.removeFromWatchlist(userId, auctionId);
            watching = false;
        } else {
            watchlistService.addToWatchlist(userId, auctionId);
            watching = true;
        }
    }

    public boolean isCanEditPhotos() {
        return isSeller() || navigationBean.isAdmin();
    }

    public void makeMainPhoto(Long photoId) {
        try {
            photoService.setPrimaryPhoto(auctionId, photoId, navigationBean.getUserId());
            showMessage("Main photo updated.", false);
        } catch (IllegalArgumentException | SecurityException e) {
            showMessage(e.getMessage(), true);
        }
        reloadPhotos();
    }

    public void deletePhoto(Long photoId) {
        try {
            photoService.deletePhoto(photoId, navigationBean.getUserId());
            showMessage("Photo deleted", false);
        } catch (SecurityException e) {
            showMessage(e.getMessage(), true);
        }
        reloadPhotos();
    }

    // shows one message and hides the other one
    private void showMessage(String text, boolean isError) {
        if (isError) {
            errorMessage = text;
            successMessage = null;
        } else {
            successMessage = text;
            errorMessage = null;
        }
    }

    private void reloadPhotos() {
        auction = auctionService.getAuctionNoView(auctionId);
        photos = photoService.getAuctionPhotos(auctionId);
    }

    private void reloadData() {
        auction = auctionService.getAuctionNoView(auctionId);
        bids = bidService.getAuctionBids(auctionId);
        bidCount = bidService.getBidCount(auctionId);
        minimumBid = bidService.getMinimumBidAmount(auctionId);
        if (navigationBean.isLoggedIn()) {
            existingAutoBid = autoBidService.getAutoBid(
                    auctionId, navigationBean.getLoggedInUser().getId());
        }
    }

    public boolean isSeller() {
        if (!navigationBean.isLoggedIn() || auction == null) {
            return false;
        }
        return auction.getSeller().getId().equals(
                navigationBean.getLoggedInUser().getId());
    }

    public boolean isWinner() {
        if (!navigationBean.isLoggedIn() || auction == null || auction.getWinner() == null) {
            return false;
        }
        return auction.getWinner().getId().equals(
                navigationBean.getLoggedInUser().getId());
    }

    public boolean isBiddable() {
        return auction != null
                && auction.getStatus() == AuctionStatus.ACTIVE
                && !auction.isExpired();
    }

    // getters and setters

    public Long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public Auction getAuction() {
        return auction;
    }

    public List<AuctionPhoto> getPhotos() {
        return photos;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public long getBidCount() {
        return bidCount;
    }

    public BigDecimal getMinimumBid() {
        return minimumBid;
    }

    public boolean isWatching() {
        return watching;
    }

    public AutoBid getExistingAutoBid() {
        return existingAutoBid;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    public BigDecimal getAutoBidMax() {
        return autoBidMax;
    }

    public void setAutoBidMax(BigDecimal autoBidMax) {
        this.autoBidMax = autoBidMax;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }
}
