package com.bidly.service;

import com.bidly.dao.AuctionDao;
import com.bidly.dao.BidDao;
import com.bidly.dao.UserDao;
import com.bidly.interceptor.Logged;
import com.bidly.model.Auction;
import com.bidly.model.Auction.SuspendedBy;
import com.bidly.model.AuctionStatus;
import com.bidly.model.User;
import com.bidly.model.UserRole;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
@Logged
public class AuctionService {

    @EJB
    private AuctionDao auctionDao;

    @EJB
    private BidDao bidDao;

    @EJB
    private UserDao userDao;

    public Auction createAuction(Auction auction, String sellerId) {
        User seller = userDao.findById(sellerId);
        if (seller == null) throw new IllegalArgumentException("User not found.");

        auction.setSeller(seller);
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setCurrentPrice(auction.getStartPrice());
        auction.setStartTime(LocalDateTime.now());
        auction.setCreatedAt(LocalDateTime.now());
        auction.setViewCount(0);

        auctionDao.save(auction);
        return auction;
    }

    public Auction getAuction(Long id) {
        auctionDao.incrementViewCount(id);
        return auctionDao.findById(id);
    }

    public Auction getAuctionNoView(Long id) {
        return auctionDao.findById(id);
    }

    public List<Auction> searchAuctions(String keyword, Long categoryId,
                                        String sortBy, int page, int pageSize) {
        return auctionDao.search(keyword, categoryId, sortBy, page, pageSize);
    }

    public long countSearchResults(String keyword, Long categoryId) {
        return auctionDao.countSearchResults(keyword, categoryId);
    }

    public List<Auction> getAuctionsBySeller(String userId) {
        return auctionDao.findBySeller(userId);
    }

    public List<Auction> getAuctionsWonByUser(String userId) {
        return auctionDao.findByWinner(userId);
    }

    public void cancelAuction(Long auctionId, String userId) {
        Auction auction = auctionDao.findById(auctionId);
        if (auction == null) throw new IllegalArgumentException("Auction not found.");
        if (!auction.getSeller().getId().equals(userId)) {
            throw new IllegalStateException("Only the seller can cancel an auction.");
        }
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("Only active auctions can be cancelled.");
        }

        if (bidDao.countByAuction(auctionId) > 0) {
            throw new IllegalStateException("Cannot cancel an auction that already has bids.");
        }

        auction.setStatus(AuctionStatus.CANCELLED);
        auctionDao.update(auction);
    }

    public void deleteAuction(Long auctionId, String callerId) {
        User caller = userDao.findById(callerId);
        if (caller == null) throw new SecurityException("Authentication required.");
        Auction auction = auctionDao.findById(auctionId);
        if (auction == null) return;
        if (!isAdmin(caller) && !isSeller(auction, caller)) {
            throw new SecurityException("Only the auction owner or an administrator can delete this auction.");
        }
        auctionDao.delete(auction);
    }

    public void suspendAuction(Long auctionId, String callerId, String reason) {
        Auction auction = auctionDao.findById(auctionId);
        if (auction == null) throw new IllegalArgumentException("Auction not found.");
        User caller = userDao.findById(callerId);
        boolean isAdmin = isAdmin(caller);
        if (!isAdmin && !isSeller(auction, caller)) {
            throw new SecurityException("Only the seller or an administrator can suspend this auction.");
        }
        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("Only active auctions can be suspended.");
        }

        auction.setStatus(AuctionStatus.SUSPENDED);
        auction.setSuspendedBy(isAdmin ? SuspendedBy.ADMIN : SuspendedBy.SELLER);
        auction.setSuspendReason(isAdmin ? UserService.cleanReason(reason) : null);
        auctionDao.update(auction);
    }

    public void resumeAuction(Long auctionId, String callerId) {
        Auction auction = auctionDao.findById(auctionId);
        if (auction == null) throw new IllegalArgumentException("Auction not found.");
        User caller = userDao.findById(callerId);
        boolean isAdmin = isAdmin(caller);
        if (!isAdmin && !isSeller(auction, caller)) {
            throw new SecurityException("Only the seller or an administrator can resume this auction.");
        }
        if (auction.getStatus() != AuctionStatus.SUSPENDED) {
            throw new IllegalStateException("Only suspended auctions can be resumed.");
        }
        if (auction.getSeller().isBlocked()) {
            throw new IllegalStateException("Cannot resume auction: seller account is blocked.");
        }
        if (!isAdmin && auction.getSuspendedBy() != SuspendedBy.SELLER) {
            throw new SecurityException("Auction was suspended by an administrator.");
        }

        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setSuspendedBy(null);
        auction.setSuspendReason(null);
        auctionDao.update(auction);
    }

    private static boolean isAdmin(User user) {
        return user != null && user.getRole() == UserRole.ADMIN;
    }

    private static boolean isSeller(Auction auction, User user) {
        return user != null && auction.getSeller().getId().equals(user.getId());
    }
}
