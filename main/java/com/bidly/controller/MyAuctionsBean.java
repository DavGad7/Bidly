package com.bidly.controller;

import com.bidly.model.Auction;
import com.bidly.model.AuctionStatus;
import com.bidly.service.AdminService;
import com.bidly.service.AuctionService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class MyAuctionsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private AuctionService auctionService;

    @EJB
    private AdminService adminService;

    @Inject
    private NavigationBean navigationBean;

    private List<Auction> allAuctions;
    private List<Auction> filteredAuctions;
    private String statusFilter = "ALL";
    private String errorMessage;
    private String successMessage;

    @PostConstruct
    public void init() {
        loadAuctions();
    }

    private void loadAuctions() {
        if (navigationBean.isLoggedIn()) {
            allAuctions = auctionService.getAuctionsBySeller(
                    navigationBean.getLoggedInUser().getId());
            filterByStatus();
        }
    }

    public void filterByStatus() {
        if ("ALL".equals(statusFilter) || statusFilter == null) {
            filteredAuctions = allAuctions;
        } else {
            AuctionStatus status = AuctionStatus.valueOf(statusFilter);
            List<Auction> res = new ArrayList<>();
            for (Auction a : allAuctions) {
                if (a.getStatus() == status) {
                    res.add(a);
                }
            }
            filteredAuctions = res;
        }
    }

    public void cancelAuction(Long auctionId) {
        clearMessages();
        try {
            auctionService.cancelAuction(auctionId,
                    navigationBean.getLoggedInUser().getId());
            successMessage = "Auction cancelled.";
            loadAuctions();
        } catch (IllegalStateException | IllegalArgumentException e) {
            errorMessage = e.getMessage();
        }
    }

    public void suspendAuction(Long auctionId) {
        clearMessages();
        try {
            auctionService.suspendAuction(auctionId, navigationBean.getUserId(), null);
            successMessage = "Auction suspended - no one can bid until you resume it";
        } catch (IllegalStateException | IllegalArgumentException | SecurityException e) {
            errorMessage = e.getMessage();
        }
        loadAuctions();
    }

    public void resumeAuction(Long auctionId) {
        clearMessages();
        try {
            auctionService.resumeAuction(auctionId, navigationBean.getUserId());
            successMessage = "Auction resumed.";
        } catch (IllegalStateException | IllegalArgumentException | SecurityException e) {
            errorMessage = e.getMessage();
        }
        loadAuctions();
    }

    private void clearMessages() {
        errorMessage = null;
        successMessage = null;
    }

    public void exportCsv() {
        if (!navigationBean.isLoggedIn()) return;

        String csv = adminService.exportUserAuctionsCsv(
                navigationBean.getLoggedInUser().getId());

        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();

            ec.responseReset();
            ec.setResponseContentType("text/csv");
            ec.setResponseCharacterEncoding("UTF-8");
            ec.setResponseHeader("Content-Disposition",
                    "attachment; filename=\"my-auctions.csv\"");

            ec.getResponseOutputStream().write(csv.getBytes("UTF-8"));
            fc.responseComplete();
        } catch (IOException e) {
            errorMessage = "Failed to export CSV: " + e.getMessage();
        }
    }

    public List<Auction> getFilteredAuctions() {
        return filteredAuctions;
    }

    public String getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getSuccessMessage() {
        return successMessage;
    }
}
