package com.bidly.controller;

import com.bidly.model.Bid;
import com.bidly.service.BidService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class MyBidsBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private BidService bidService;

    @Inject
    private NavigationBean navigationBean;

    private List<Bid> bids;

    @PostConstruct
    public void init() {
        if (navigationBean.isLoggedIn()) {
            bids = bidService.getUserBids(navigationBean.getLoggedInUser().getId());
        }
    }

    public String getBidStatus(Bid bid) {
        return bidService.getBidStatus(bid);
    }

    public String getBidStatusClass(Bid bid) {
        String status = getBidStatus(bid);
        if ("LEADING".equals(status)) {
            return "badge bg-success";
        } else if ("OUTBID".equals(status)) {
            return "badge bg-danger";
        } else if ("WON".equals(status)) {
            return "badge bg-warning text-dark";
        } else if ("LOST".equals(status)) {
            return "badge bg-secondary";
        } else if ("CANCELLED".equals(status)) {
            return "badge bg-dark";
        } else if ("SUSPENDED".equals(status)) {
            return "badge bg-warning text-dark";
        }
        return "badge bg-secondary";
    }

    public List<Bid> getBids() {
        return bids;
    }
}
