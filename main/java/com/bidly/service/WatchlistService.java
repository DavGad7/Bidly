package com.bidly.service;

import com.bidly.dao.AuctionDao;
import com.bidly.dao.UserDao;
import com.bidly.dao.WatchlistDao;
import com.bidly.model.Auction;
import com.bidly.model.User;
import com.bidly.model.WatchlistEntry;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;

@Stateless
public class WatchlistService {

    @EJB
    private WatchlistDao watchlistDao;

    @EJB
    private UserDao userDao;

    @EJB
    private AuctionDao auctionDao;

    public void addToWatchlist(String userId, Long auctionId) {
        if (watchlistDao.findByUserAndAuction(userId, auctionId) != null) {
            return;
        }

        User user = userDao.findById(userId);
        Auction auction = auctionDao.findById(auctionId);
        if (user != null && auction != null) {
            watchlistDao.save(new WatchlistEntry(user, auction));
        }
    }

    public void removeFromWatchlist(String userId, Long auctionId) {
        WatchlistEntry entry = watchlistDao.findByUserAndAuction(userId, auctionId);
        if (entry != null) {
            watchlistDao.delete(entry);
        }
    }

    public boolean isWatching(String userId, Long auctionId) {
        return watchlistDao.findByUserAndAuction(userId, auctionId) != null;
    }

    public List<WatchlistEntry> getUserWatchlist(String userId) {
        return watchlistDao.findByUser(userId);
    }

    public List<String> getWatchersForAuction(Long auctionId) {
        return watchlistDao.findWatchersForAuction(auctionId);
    }
}
