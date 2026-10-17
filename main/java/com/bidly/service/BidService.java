package com.bidly.service;

import com.bidly.dao.AuctionDao;
import com.bidly.dao.BidDao;
import com.bidly.dao.BidIncrementDao;
import com.bidly.dao.UserDao;
import com.bidly.jms.AuctionEventProducer;
import com.bidly.model.*;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@Logged
public class BidService {

    private static final Logger LOG = Logger.getLogger(BidService.class.getName());

    @EJB
    private BidDao bidDao;
    @EJB
    private AuctionDao auctionDao;
    @EJB
    private BidIncrementDao bidIncrementDao;
    @EJB
    private UserDao userDao;

    @EJB
    private AutoBidService autoBidService;

    @EJB
    private WatchlistService watchlistService;

    @EJB
    private AuctionEventProducer eventProducer;

    public Bid placeBid(Long auctionId, String bidderId, BigDecimal amount) {
        Auction auction = auctionDao.findById(auctionId);
        if (auction == null) throw new IllegalArgumentException("Auction not found.");
        User bidder = userDao.findById(bidderId);
        if (bidder == null) throw new IllegalArgumentException("User not found.");

        if (auction.getStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalStateException("Auction isn't active.");
        }
        // timer runs every minute so it can still be ACTIVE here
        if (auction.isExpired()) {
            throw new IllegalStateException("Auction already ended.");
        }

        if (auction.getSeller().getId().equals(bidder.getId())) {
            throw new IllegalArgumentException("You can't bid on your own auction.");
        }
        if (bidder.isBlocked()) {
            throw new IllegalArgumentException("Your account is blocked.");
        }

        BigDecimal currentPrice = auction.getCurrentPrice();
        BigDecimal step = bidIncrementDao.incrementFor(currentPrice);
        BigDecimal minBid = currentPrice.add(step);
        if (amount.compareTo(minBid) < 0) {
            throw new IllegalArgumentException(
                    "Bid has to be at least $" + minBid.toPlainString());
        }

        // bid >= buy now price -> they just buy it at that price
        boolean isBuyNow = auction.hasBuyNow()
                && amount.compareTo(auction.getBuyNowPrice()) >= 0;
        if (isBuyNow) {
            amount = auction.getBuyNowPrice();
        }

        // leader before the new bid is saved
        Bid prevTop = bidDao.findHighestBid(auctionId);
        String prevLeaderId = prevTop != null ? prevTop.getBidder().getId() : null;

        Bid bid = new Bid(auction, bidder, amount, LocalDateTime.now(), false);
        bidDao.save(bid);

        auction.setCurrentPrice(amount);

        if (isBuyNow) {
            auction.setStatus(AuctionStatus.CLOSED);
            auction.setWinner(bidder);
            auctionDao.update(auction);

            LOG.log(Level.INFO, "Buy-now triggered on auction {0} by user {1}",
                    new Object[]{auctionId, bidder.getUsername()});

            eventProducer.sendAuctionClosedToSeller(
                    auctionId, auction.getTitle(),
                    auction.getSeller().getId(),
                    bidder.getDisplayName(), amount);
            eventProducer.sendAuctionWonEvent(
                    auctionId, auction.getTitle(),
                    bidder.getId(), amount);
            return bid;
        }

        auctionDao.update(auction);

        eventProducer.sendNewBidEvent(
                auctionId, auction.getTitle(),
                auction.getSeller().getId(),
                bidder.getDisplayName(), amount);

        if (prevLeaderId != null && !prevLeaderId.equals(bidder.getId())) {
            eventProducer.sendOutbidEvent(
                auctionId, auction.getTitle(),
                prevLeaderId, amount);
        }

        for (String w : watchlistService.getWatchersForAuction(auctionId)) {
            if (!w.equals(bidder.getId()) && !w.equals(auction.getSeller().getId())) {
                eventProducer.sendWatchlistEvent(auctionId, auction.getTitle(), w, amount);
            }
        }

        autoBidService.processAutoBids(auction);

        return bid;
    }

    public List<Bid> getAuctionBids(Long auctionId) {
        return bidDao.findByAuction(auctionId);
    }

    public List<Bid> getUserBids(String userId) {
        return bidDao.findByBidder(userId);
    }

    public long getBidCount(Long auctionId) {
        return bidDao.countByAuction(auctionId);
    }

    public BigDecimal getMinimumBidAmount(Long auctionId) {
        Auction auction = auctionDao.findById(auctionId);
        if (auction == null) return BigDecimal.ZERO;
        return auction.getCurrentPrice().add(bidIncrementDao.incrementFor(auction.getCurrentPrice()));
    }

    public String getBidStatus(Bid bid) {
        Auction auction = bid.getAuction();
        if (auction.getStatus() == AuctionStatus.CLOSED) {
            if (auction.getWinner() != null
                    && auction.getWinner().getId().equals(bid.getBidder().getId())) {
                return "WON";
            }
            return "LOST";
        }
        if (auction.getStatus() == AuctionStatus.CANCELLED) {
            return "CANCELLED";
        }
        if (auction.getStatus() == AuctionStatus.SUSPENDED) {
            return "SUSPENDED";
        }
        Bid highest = bidDao.findHighestBid(auction.getId());
        if (highest != null && highest.getBidder().getId().equals(bid.getBidder().getId())) {
            return "LEADING";
        }
        return "OUTBID";
    }
}
