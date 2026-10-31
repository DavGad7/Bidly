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

            String auction = truncate(event.getAuctionTitle(), 50);
            String amount = event.getBidAmount() != null ? event.getBidAmount().toPlainString() : "N/A";
            String title;
            String body;
            switch (event.getType()) {
                case NEW_BID:
                    title = "New bid on '" + auction + "'";
                    body = "A bid of $" + amount + " was placed.";
                    break;
                case OUTBID:
                    title = "You have been outbid on '" + auction + "'";
                    body = "The current price is now $" + amount + ".";
                    break;
                case AUCTION_WON:
                    title = "You won '" + auction + "'";
                    body = "Your winning bid: $" + amount + ".";
                    break;
                case AUCTION_CLOSED_SELLER:
                    title = "Your auction '" + auction + "' has ended";
                    if (event.getBidderName() != null && event.getBidAmount() != null) {
                        body = "Winner: " + event.getBidderName() + " at $" + amount + ".";
                    } else {
                        body = "The auction has ended.";
                    }
                    break;
                case AUCTION_CLOSED_NO_BIDS:
                    title = "Your auction '" + auction + "' ended with no bids";
                    body = "Nobody placed a bid on your auction.";
                    break;
                default: // WATCHLIST_UPDATE
                    title = "New activity on watched auction '" + auction + "'";
                    body = "Current price: $" + amount + ".";
            }
            notificationService.createNotification(event.getTargetUserId(), title, body,
                    event.getType(), event.getAuctionId());

            LOG.log(Level.FINE, "Notification created for user {0}: {1}",
                    new Object[]{event.getTargetUserId(), title});
        } catch (Exception e) {
            // dont rethrow or it gets redelivered
            LOG.log(Level.SEVERE, "Error processing JMS message", e);
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 3) + "...";
    }
}

