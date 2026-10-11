package com.bidly.controller;

import com.bidly.model.WatchlistEntry;
import com.bidly.service.WatchlistService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class WatchlistBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private WatchlistService watchlistService;

    @Inject
    private NavigationBean navigationBean;

    private List<WatchlistEntry> entries;

    @PostConstruct
    public void init() {
        loadWatchlist();
    }

    private void loadWatchlist() {
        if (navigationBean.isLoggedIn()) {
            entries = watchlistService.getUserWatchlist(
                    navigationBean.getLoggedInUser().getId());
        }
    }

    public void removeFromWatchlist(Long auctionId) {
        if (navigationBean.isLoggedIn()) {
            watchlistService.removeFromWatchlist(
                    navigationBean.getLoggedInUser().getId(), auctionId);
            loadWatchlist();
        }
    }

    public List<WatchlistEntry> getEntries() {
        return entries;
    }
}
