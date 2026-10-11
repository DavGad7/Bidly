package com.bidly.service;

import com.bidly.dao.AuctionDao;
import com.bidly.dao.AutoBidDao;
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
import java.util.logging.Logger;

@Stateless
@Logged
public class AutoBidService {

    private static final Logger LOG = Logger.getLogger(AutoBidService.class.getName());

    @EJB
    private AutoBidDao autoBidDao;
    @EJB
    private BidDao bidDao;
    @EJB
    private AuctionDao auctionDao;
    @EJB
    private BidIncrementDao bidIncrementDao;
    @EJB
    private UserDao userDao;

    @EJB
    private AuctionEventProducer eventProducer;

    public void setAutoBid(Long auctionId, String bidderId, BigDecimal maxAmount) {
        Auction auction = auctionDao.findById(auctionId);
        if (auction == null) throw new IllegalArgumentException("Auction not found.");
        User bidder = userDao.findById(bidderId);
        if (bidder == null) throw new IllegalArgumentException("User not found.");
        if (auction.getStatus() != AuctionStatus.ACTIVE) throw new IllegalArgumentException("Auction is not active.");
        if (auction.getSeller().getId().equals(bidder.getId())) {
            throw new IllegalArgumentException("Seller cannot bid on their own auction.");
        }
        if (bidder.isBlocked()) {
            throw new IllegalArgumentException("Your account is blocked.");
        }

        BigDecimal minBid = calculateMinimumBid(auction.getCurrentPrice());
        if (maxAmount.compareTo(minBid) < 0) {
            throw new IllegalArgumentException(
                    "Auto-bid maximum must be at least $" + minBid.toPlainString());
        }

        AutoBid existing = autoBidDao.findByAuctionAndBidder(auctionId, bidder.getId());
        if (existing != null) {
            existing.setMaxAmount(maxAmount);
            existing.setActive(true);
            autoBidDao.update(existing);
        } else {
            autoBidDao.save(new AutoBid(auction, bidder, maxAmount));
        }

        processAutoBids(auction);
    }

    public void cancelAutoBid(Long auctionId, String userId) {
        AutoBid autoBid = autoBidDao.findByAuctionAndBidder(auctionId, userId);
        if (autoBid != null) {
            autoBid.setActive(false);
            autoBidDao.update(autoBid);
        }
    }

    public AutoBid getAutoBid(Long auctionId, String userId) {
        return autoBidDao.findByAuctionAndBidder(auctionId, userId);
    }

    /**
     * Runs proxy bidding for the auction. Places at most one automatic bid per call.
     *
     * @param auction the auction, reloaded from the database
     */
    public void processAutoBids(Auction auction) {
        auction = auctionDao.findById(auction.getId());
        if (auction == null || auction.getStatus() != AuctionStatus.ACTIVE) {
            return;
        }

        BigDecimal currentPrice = auction.getCurrentPrice();
        BigDecimal minNextBid = calculateMinimumBid(currentPrice);

        Bid top = bidDao.findHighestBid(auction.getId());
        String leaderId = (top != null) ? top.getBidder().getId() : null;

        List<AutoBid> active = autoBidDao.findActiveByAuction(auction.getId());
        if (active.isEmpty()) {
            return;
        }

        AutoBid leaderBid = null;
        AutoBid topChallenger = null;
        AutoBid otherChallenger = null;

        // sorted by maxAmount desc
        for (AutoBid ab : active) {
            if (ab.getBidder().isBlocked()) {
                continue;
            }
            if (ab.getBidder().getId().equals(leaderId)) {
                leaderBid = ab;
            } else if (ab.getMaxAmount().compareTo(minNextBid) >= 0) {
                if (topChallenger == null) {
                    topChallenger = ab;
                } else if (otherChallenger == null) {
                    otherChallenger = ab;
                }
            }
        }

        if (topChallenger == null) {
            return;
        }

        AutoBid winning;
        BigDecimal secondPrice;

        // leader keeps the lead on a tie
        if (leaderBid != null && leaderBid.getMaxAmount().compareTo(topChallenger.getMaxAmount()) >= 0) {
            winning = leaderBid;
            secondPrice = topChallenger.getMaxAmount();
        } else {
            winning = topChallenger;
            secondPrice = currentPrice;
            if (leaderBid != null) {
                secondPrice = secondPrice.max(leaderBid.getMaxAmount());
            }
            if (otherChallenger != null) {
                secondPrice = secondPrice.max(otherChallenger.getMaxAmount());
            }
        }

        BigDecimal newPrice = calculateMinimumBid(secondPrice);
        if (newPrice.compareTo(winning.getMaxAmount()) > 0) {
            newPrice = winning.getMaxAmount();
        }
        if (newPrice.compareTo(minNextBid) < 0) {
            newPrice = minNextBid;
        }

        boolean hitBuyNow = auction.hasBuyNow() && newPrice.compareTo(auction.getBuyNowPrice()) >= 0;
        if (hitBuyNow) {
            newPrice = auction.getBuyNowPrice();
        }

        User winner = winning.getBidder();
        Bid bid = new Bid(auction, winner, newPrice, LocalDateTime.now(), true);
        bidDao.save(bid);

        auction.setCurrentPrice(newPrice);
        if (hitBuyNow) {
            auction.setStatus(AuctionStatus.CLOSED);
            auction.setWinner(winner);
        }
        auctionDao.update(auction);

        LOG.info("Auto-bid placed for " + winner.getUsername() + " at $" + newPrice +
                " on auction #" + auction.getId());

        if (leaderId != null && !leaderId.equals(winner.getId())) {
            eventProducer.sendOutbidEvent(auction.getId(), auction.getTitle(), leaderId, newPrice);
        }

        if (hitBuyNow) {
            eventProducer.sendAuctionClosedToSeller(
                    auction.getId(), auction.getTitle(),
                    auction.getSeller().getId(),
                    winner.getDisplayName(), newPrice);
            eventProducer.sendAuctionWonEvent(
                    auction.getId(), auction.getTitle(),
                    winner.getId(), newPrice);
            return;
        }

        deactivateExhaustedAutoBids(auction);
    }

    private void deactivateExhaustedAutoBids(Auction auction) {
        BigDecimal minBid = calculateMinimumBid(auction.getCurrentPrice());
        List<AutoBid> list = autoBidDao.findActiveByAuction(auction.getId());

        Bid top = bidDao.findHighestBid(auction.getId());
        String leaderId = (top != null) ? top.getBidder().getId() : null;

        for (AutoBid ab : list) {
            if (ab.getBidder().getId().equals(leaderId)) continue;

            if (ab.getMaxAmount().compareTo(minBid) < 0) {
                ab.setActive(false);
                autoBidDao.update(ab);

                eventProducer.sendOutbidEvent(
                        auction.getId(), auction.getTitle(),
                        ab.getBidder().getId(), auction.getCurrentPrice());
            }
        }
    }

    private BigDecimal calculateMinimumBid(BigDecimal currentPrice) {
        return currentPrice.add(bidIncrementDao.incrementFor(currentPrice));
    }
}
