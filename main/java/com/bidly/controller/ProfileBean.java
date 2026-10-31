package com.bidly.controller;

import com.bidly.model.Auction;
import com.bidly.model.AuctionStatus;
import com.bidly.model.User;
import com.bidly.service.AuctionService;
import com.bidly.service.UserService;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class ProfileBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private UserService userService;

    @EJB
    private AuctionService auctionService;

    @Inject
    private NavigationBean navigationBean;

    private String userId;

    private User profileUser;
    private boolean ownProfile;
    private boolean editMode;

    private String firstName;
    private String lastName;
    private String email;

    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;

    private List<Auction> userAuctions;
    private int reputation;
    private int completedAuctions;
    private int auctionsWon;

    private String errorMessage;
    private String successMessage;

    private String blockReason;
    private final Map<Long, String> suspendReasons = new HashMap<>();

    public void loadProfile() {
        if (userId != null) {
            profileUser = userService.findById(userId);
            ownProfile = navigationBean.isLoggedIn()
                    && navigationBean.getLoggedInUser().getId().equals(userId);
        } else if (navigationBean.isLoggedIn()) {
            profileUser = userService.findById(navigationBean.getLoggedInUser().getId());
            ownProfile = true;
        }

        if (profileUser == null) {
            errorMessage = "User not found.";
            return;
        }

        firstName = profileUser.getFirstName();
        lastName = profileUser.getLastName();
        email = profileUser.getEmail();

        reputation = userService.getReputation(profileUser.getId());

        userAuctions = auctionService.getAuctionsBySeller(profileUser.getId());
        int count = 0;
        for (Auction a : userAuctions) {
            if (a.getStatus() == AuctionStatus.CLOSED && a.getWinner() != null) {
                count++;
            }
        }
        completedAuctions = count;

        auctionsWon = auctionService.getAuctionsWonByUser(profileUser.getId()).size();
    }

    public String getThumbnailUrl(Auction auction) {
        if (auction != null && auction.getThumbnailPhotoId() != null) {
            String contextPath = FacesContext.getCurrentInstance()
                    .getExternalContext().getRequestContextPath();
            return contextPath + "/image?id=" + auction.getThumbnailPhotoId();
        }
        return "";
    }

    public String blockUser() {
        if (profileUser != null && navigationBean.isAdmin()) {
            userService.blockUser(profileUser.getId(), navigationBean.getUserId(), blockReason);
            blockReason = null;
            successMessage = "User blocked.";
            loadProfile();
        }
        return null;
    }

    public String unblockUser() {
        if (profileUser != null && navigationBean.isAdmin()) {
            userService.unblockUser(profileUser.getId(), navigationBean.getUserId());
            successMessage = "User unblocked.";
            loadProfile();
        }
        return null;
    }

    public String deleteAuction(Long auctionId) {
        try {
            auctionService.deleteAuction(auctionId, navigationBean.getUserId());
            successMessage = "Auction deleted.";
            loadProfile();
        } catch (Exception e) {
            errorMessage = "Failed to delete auction.";
        }
        return null;
    }

    public boolean isCanSuspend() {
        return ownProfile || navigationBean.isAdmin();
    }

    public String suspendAuction(Long auctionId) {
        successMessage = null;
        boolean ok = false;
        try {
            auctionService.suspendAuction(auctionId, navigationBean.getUserId(), suspendReasons.get(auctionId));
            suspendReasons.clear();
            ok = true;
        } catch (IllegalArgumentException | IllegalStateException | SecurityException e) {
            errorMessage = e.getMessage();
        }
        if (ok) {
            errorMessage = null;
            successMessage = "Auction suspended.";
        }
        loadProfile();
        return null;
    }

    public String resumeAuction(Long auctionId) {
        errorMessage = null;
        successMessage = null;
        try {
            auctionService.resumeAuction(auctionId, navigationBean.getUserId());
            successMessage = "Auction resumed.";
        } catch (SecurityException e) {
            errorMessage = e.getMessage();
        } catch (IllegalStateException | IllegalArgumentException e) {
            errorMessage = e.getMessage();
        }
        loadProfile();
        return null;
    }

    public void saveProfile() {
        errorMessage = null;
        successMessage = null;

        try {
            userService.updateProfile(profileUser.getId(), firstName, lastName, email);
            successMessage = "Your profile was updated successfully!";
            editMode = false;
            loadProfile();
        } catch (IllegalArgumentException e) {
            errorMessage = e.getMessage();
        }
    }

    public void changePassword() {
        errorMessage = null;
        successMessage = null;

        if (newPassword == null || !newPassword.equals(confirmNewPassword)) {
            errorMessage = "New passwords do not match.";
            return;
        }

        try {
            userService.changePassword(profileUser.getId(), oldPassword, newPassword);
            successMessage = "Password changed successfully.";
            oldPassword = null;
            newPassword = null;
            confirmNewPassword = null;
        } catch (IllegalArgumentException e) {
            errorMessage = e.getMessage();
        }
    }

    public void toggleEditMode() {
        editMode = !editMode;
    }

    // ---- getters & setters ----

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User getProfileUser() {
        return profileUser;
    }

    public boolean isOwnProfile() {
        return ownProfile;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }

    public List<Auction> getUserAuctions() {
        return userAuctions;
    }

    public int getReputation() { return reputation; }
    public int getCompletedAuctions() { return completedAuctions; }
    public int getAuctionsWon() { return auctionsWon; }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }

    public Map<Long, String> getSuspendReasons() {
        return suspendReasons;
    }
}
