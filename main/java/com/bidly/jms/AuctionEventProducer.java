package com.bidly.jms;

import com.bidly.model.NotificationType;
import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class AuctionEventProducer {

    private static final Logger LOG = Logger.getLogger(AuctionEventProducer.class.getName());

    @Resource(lookup = "java:app/jms/BidlyConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "java:app/jms/AuctionEventQueue")
    private Queue queue;

    public void sendEvent(AuctionEvent event) {
        try (JMSContext context = connectionFactory.createContext()) {
            ObjectMessage message = context.createObjectMessage(event);
            context.createProducer().send(queue, message);
            LOG.log(Level.FINE, "Sent JMS event: {0}", event);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to send JMS event: " + event, e);
        }
    }

    public void sendNewBidEvent(Long auctionId, String auctionTitle,
                                String sellerId, String bidderName, BigDecimal amount) {
        AuctionEvent event = new AuctionEvent(
                NotificationType.NEW_BID, auctionId, auctionTitle,
                sellerId, bidderName, amount);
        sendEvent(event);
    }

    public void sendOutbidEvent(Long auctionId, String auctionTitle,
                                String outbidUserId, BigDecimal newAmount) {
        sendEvent(new AuctionEvent(NotificationType.OUTBID, auctionId, auctionTitle,
                outbidUserId, null, newAmount));
    }

    public void sendAuctionWonEvent(Long auctionId, String auctionTitle,
                                    String winnerId, BigDecimal winningAmount) {
        sendEvent(new AuctionEvent(NotificationType.AUCTION_WON, auctionId, auctionTitle,
                winnerId, null, winningAmount));
    }

    public void sendAuctionClosedToSeller(Long auctionId, String auctionTitle,
                                          String sellerId, String winnerName,
                                          BigDecimal winningAmount) {
        AuctionEvent event = new AuctionEvent(
                NotificationType.AUCTION_CLOSED_SELLER, auctionId, auctionTitle,
                sellerId, winnerName, winningAmount);
        sendEvent(event);
    }

    public void sendAuctionClosedNoBids(Long auctionId, String auctionTitle, String sellerId) {
        sendEvent(new AuctionEvent(NotificationType.AUCTION_CLOSED_NO_BIDS, auctionId, auctionTitle,
                sellerId, null, null));
    }

    public void sendWatchlistEvent(Long auctionId, String auctionTitle,
                                   String watcherUserId, BigDecimal currentPrice) {
        sendEvent(new AuctionEvent(NotificationType.WATCHLIST_UPDATE, auctionId, auctionTitle,
                watcherUserId, null, currentPrice));
    }
}
