package com.bidly.jms;

import com.bidly.service.NotificationService;
import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJB;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import java.util.logging.Level;
import java.util.logging.Logger;

@MessageDriven(activationConfig = {
    @ActivationConfigProperty(
        propertyName = "destinationLookup",
        propertyValue = "java:app/jms/AuctionEventQueue"),
    @ActivationConfigProperty(
        propertyName = "destinationType",
        propertyValue = "jakarta.jms.Queue")
})
public class AuctionEventMDB implements MessageListener {

    private static final Logger LOG = Logger.getLogger(AuctionEventMDB.class.getName());

    @EJB
    private NotificationService notificationService;

    @Override
    public void onMessage(Message message) {
        try {
            if (!(message instanceof ObjectMessage)) {
                LOG.warning("Ignored incoming non-ObjectMessage on AuctionEventQueue");
                return;
            }
            AuctionEvent event = (AuctionEvent) ((ObjectMessage) message).getObject();
            LOG.log(Level.FINE, "Processing JMS event: {0}", event);

            String title = buildTitle(event);
            String body = buildBody(event);
            notificationService.createNotification(event.getTargetUserId(), title, body,
                    event.getType(), event.getAuctionId());

            LOG.log(Level.FINE, "Notification created for user {0}: {1}",
                    new Object[]{event.getTargetUserId(), title});
        } catch (Exception e) {
            // Don't rethrow here. The container would just keep redelivering the message.
            LOG.log(Level.SEVERE, "Error processing JMS message", e);
        }
    }

    private String buildTitle(AuctionEvent event) {
        String auction = truncate(event.getAuctionTitle(), 50);
        switch (event.getType()) {
            case NEW_BID:
                return "New bid on '" + auction + "'";
            case OUTBID:
                return "You have been outbid on '" + auction + "'";
            case AUCTION_WON:
                return "You won '" + auction + "'";
            case AUCTION_CLOSED_SELLER:
                return "Your auction '" + auction + "' has ended";
            case AUCTION_CLOSED_NO_BIDS:
                return "Your auction '" + auction + "' ended with no bids";
            case WATCHLIST_UPDATE:
                return "New activity on watched auction '" + auction + "'";
            default:
                return "Auction update";
        }
    }

    private String buildBody(AuctionEvent event) {
        switch (event.getType()) {
            case NEW_BID:
                return "A bid of $" + event.getBidAmount().toPlainString() + " was placed.";
            case OUTBID:
                return "The current price is now $" + event.getBidAmount().toPlainString() + ".";
            case AUCTION_WON:
                return "Your winning bid: $" + event.getBidAmount().toPlainString() + ".";
            case AUCTION_CLOSED_SELLER:
                if (event.getBidderName() != null && event.getBidAmount() != null) {
                    return "Winner: " + event.getBidderName() +
                           " at $" + event.getBidAmount().toPlainString() + ".";
                }
                return "The auction has ended.";
            case AUCTION_CLOSED_NO_BIDS:
                return "Nobody placed a bid on your auction.";
            case WATCHLIST_UPDATE:
                return "Current price: $" +
                       (event.getBidAmount() != null ? event.getBidAmount().toPlainString() : "N/A") + ".";
            default:
                return "";
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 3) + "...";
    }
}

