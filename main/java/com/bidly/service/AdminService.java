package com.bidly.service;

import com.bidly.dao.AuctionDao;
import com.bidly.dao.BidDao;
import com.bidly.dao.UserDao;
import com.bidly.interceptor.Logged;
import com.bidly.model.Auction;
import com.bidly.model.AuctionStatus;
import com.bidly.model.User;
import com.bidly.util.DateUtil;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;

@Stateless
@Logged
public class AdminService {

    @EJB
    private UserDao userDao;

    @EJB
    private AuctionDao auctionDao;

    @EJB
    private BidDao bidDao;

    public long getTotalUsers() {
        return userDao.countAll();
    }

    public long getActiveAuctionCount() {
        return auctionDao.countByStatus(AuctionStatus.ACTIVE);
    }

    public long getClosedAuctionCount() {
        return auctionDao.countByStatus(AuctionStatus.CLOSED);
    }

    public long getTotalBidsCount() {
        return bidDao.countAll();
    }

    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.isBlank()) return userDao.findAll();
        return userDao.search(keyword.trim());
    }

    public List<Auction> getAllAuctions() {
        return auctionDao.findAll();
    }

    public String exportUserAuctionsCsv(String userId) {
        List<Auction> auctions = auctionDao.findBySeller(userId);
        StringBuilder sb = new StringBuilder();
        sb.append("Title,Status,Category,Start Price,Current Price,Winner,Start Time,End Time\n");

        for (Auction a : auctions) {
            String category = (a.getCategory() != null) ? a.getCategory().getName() : "";
            String winner = (a.getWinner() != null) ? a.getWinner().getDisplayName() : "";

            sb.append(escape(a.getTitle())).append(",")
              .append(escape(a.getStatus() != null ? a.getStatus().name() : "")).append(",")
              .append(escape(category)).append(",")
              .append(a.getStartPrice() != null ? a.getStartPrice() : "").append(",")
              .append(a.getCurrentPrice() != null ? a.getCurrentPrice() : "").append(",")
              .append(escape(winner)).append(",")
              .append(escape(DateUtil.formatDateTime(a.getStartTime()))).append(",")
              .append(escape(DateUtil.formatDateTime(a.getEndTime()))).append("\n");
        }
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
