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

    // otherName = bidder/winner name, only needed for some types
    public void send(NotificationType type, Long auctionId, String auctionTitle,
                     String userId, String otherName, BigDecimal amount) {
        sendEvent(new AuctionEvent(type, auctionId, auctionTitle, userId, otherName, amount));
    }

    public void sendOutbidEvent(Long auctionId, String auctionTitle,
                                String outbidUserId, BigDecimal newAmount) {
        sendEvent(new AuctionEvent(NotificationType.OUTBID, auctionId, auctionTitle,
                outbidUserId, null, newAmount));
    }
}
