package com.bidly.service;

import com.bidly.dao.AuctionDao;
import com.bidly.dao.BidDao;
import com.bidly.interceptor.Logged;
import com.bidly.jms.AuctionEventProducer;
import com.bidly.model.Auction;
import com.bidly.model.AuctionStatus;
import com.bidly.model.Bid;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.DependsOn;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
@DependsOn("DataInitializer")
@Logged
public class AuctionCloseService {

    private static final Logger LOG = Logger.getLogger(AuctionCloseService.class.getName());

    @EJB
    private AuctionDao auctionDao;

    @EJB
    private BidDao bidDao;

    @EJB
    private AuctionEventProducer eventProducer;

    @PostConstruct
    public void init() {
        LOG.info("AuctionCloseService started - checking expired auctions periodically");
    }

    @Schedule(minute = "*/1", hour = "*", persistent = false)
    public void closeExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> expired = auctionDao.findExpiredActive(now);

        if (expired.isEmpty()) {
            return;
        }

        LOG.info("Found " + expired.size() + " expired auction(s) to close.");

        for (Auction auction : expired) {
            try {
                closeAuction(auction.getId());
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error while closing auction #" + auction.getId(), e);
            }
        }
    }

    public void closeAuction(Long auctionId) {
        Auction auction = auctionDao.findById(auctionId);
        if (auction == null || auction.getStatus() != AuctionStatus.ACTIVE) {
            return;
        }

        Bid highest = bidDao.findHighestBid(auction.getId());
        auction.setStatus(AuctionStatus.CLOSED);

        if (highest != null) {
            auction.setWinner(highest.getBidder());
            auction.setCurrentPrice(highest.getAmount());
            auctionDao.update(auction);

            LOG.info("Auction #" + auction.getId() + " closed. Winner: " +
                    highest.getBidder().getUsername() + " ($" + highest.getAmount() + ")");

            eventProducer.sendAuctionWonEvent(
                    auction.getId(), auction.getTitle(),
                    highest.getBidder().getId(),
                    highest.getAmount());
            eventProducer.sendAuctionClosedToSeller(
                    auction.getId(), auction.getTitle(),
                    auction.getSeller().getId(),
                    highest.getBidder().getDisplayName(),
                    highest.getAmount());
        } else {
            auctionDao.update(auction);
            LOG.info("Auction #" + auction.getId() + " closed with no bids.");

            eventProducer.sendAuctionClosedNoBids(
                    auction.getId(), auction.getTitle(),
                    auction.getSeller().getId());
        }
    }
}
