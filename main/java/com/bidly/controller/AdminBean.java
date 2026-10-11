package com.bidly.controller;

import com.bidly.model.Auction;
import com.bidly.model.User;
import com.bidly.service.AdminService;
import com.bidly.service.AuctionService;
import com.bidly.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class AdminBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(AdminBean.class.getName());

    @EJB
    private AdminService adminService;

    @EJB
    private UserService userService;

    @EJB
    private AuctionService auctionService;

    @Inject
    private NavigationBean navigationBean;

    private List<User> users;
    private List<Auction> auctions;
    private long totalUsers;
    private long activeAuctionCount;
    private long closedAuctionCount;
    private long totalBidsCount;
    private String userSearchKeyword;
    private String successMessage;
    private String errorMessage;

    private final Map<String, String> blockReasons = new HashMap<>();
    private final Map<Long, String> suspendReasons = new HashMap<>();

    @PostConstruct
    public void init() {
        loadStats();
        loadUsers();
        loadAuctions();
    }

    private void loadStats() {
        totalUsers = adminService.getTotalUsers();
        activeAuctionCount = adminService.getActiveAuctionCount();
        closedAuctionCount = adminService.getClosedAuctionCount();
        totalBidsCount = adminService.getTotalBidsCount();
    }

    private void loadUsers() {
        users = adminService.searchUsers(userSearchKeyword);
    }

    private void loadAuctions() {
        auctions = adminService.getAllAuctions();
    }

    public void blockUser(String userId) {
        errorMessage = null;
        userService.blockUser(userId, navigationBean.getUserId(), blockReasons.get(userId));
        blockReasons.clear();
        successMessage = "User blocked successfully.";
        loadUsers();
        loadAuctions();
        loadStats();
    }

    public void unblockUser(String userId) {
        errorMessage = null;
        userService.unblockUser(userId, navigationBean.getUserId());
        successMessage = "User unblocked successfully.";
        loadUsers();
        loadAuctions();
        loadStats();
    }

    public void deleteAuction(Long auctionId) {
        errorMessage = null;
        try {
            auctionService.deleteAuction(auctionId, navigationBean.getUserId());
            successMessage = "Auction deleted successfully.";
            loadAuctions();
            loadStats();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Admin delete failed for auction " + auctionId, e);
            errorMessage = "Failed to delete auction #" + auctionId + ". See the server log for details.";
        }
    }

    public void suspendAuction(Long auctionId) {
        errorMessage = null;
        successMessage = null;
        try {
            auctionService.suspendAuction(auctionId, navigationBean.getUserId(), suspendReasons.get(auctionId));
            successMessage = "Auction #" + auctionId + " suspended.";
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            errorMessage = e.getMessage();
        }
        suspendReasons.clear();
        loadAuctions();
        loadStats();
    }

    public void resumeAuction(Long auctionId) {
        errorMessage = null;
        successMessage = null;
        try {
            auctionService.resumeAuction(auctionId, navigationBean.getUserId());
            successMessage = "Auction #" + auctionId + " is active again.";
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            errorMessage = e.getMessage();
        }
        loadAuctions();
        loadStats();
    }

    public void searchUsers() {
        loadUsers();
    }

    public void clearUserSearch() {
        userSearchKeyword = null;
        loadUsers();
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Auction> getAuctions() {
        return auctions;
    }

    public long getTotalUsers() { return totalUsers; }
    public long getActiveAuctionCount() { return activeAuctionCount; }
    public long getClosedAuctionCount() { return closedAuctionCount; }
    public long getTotalBidsCount() { return totalBidsCount; }

    public String getUserSearchKeyword() {
        return userSearchKeyword;
    }

    public void setUserSearchKeyword(String userSearchKeyword) {
        this.userSearchKeyword = userSearchKeyword;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Map<String, String> getBlockReasons() {
        return blockReasons;
    }

    public Map<Long, String> getSuspendReasons() {
        return suspendReasons;
    }
}
