package com.bidly.dao;

import com.bidly.model.AutoBid;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class AutoBidDao {

    @PersistenceContext(unitName = "BidlyPU")
    private EntityManager em;

    public AutoBid findByAuctionAndBidder(Long auctionId, String bidderId) {
        try {
            return em.createNamedQuery("AutoBid.findByAuctionAndBidder", AutoBid.class)
                     .setParameter("auctionId", auctionId)
                     .setParameter("bidderId", bidderId)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<AutoBid> findActiveByAuction(Long auctionId) {
        return em.createNamedQuery("AutoBid.findActiveByAuction", AutoBid.class)
                 .setParameter("auctionId", auctionId)
                 .getResultList();
    }

    public void save(AutoBid autoBid) {
        em.persist(autoBid);
    }

    public AutoBid update(AutoBid autoBid) {
        return em.merge(autoBid);
    }
}
