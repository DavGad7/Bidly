package com.bidly.dao;

import com.bidly.model.Bid;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class BidDao {

    @PersistenceContext(unitName = "BidlyPU")
    private EntityManager em;

    public List<Bid> findByAuction(Long auctionId) {
        return em.createNamedQuery("Bid.findByAuction", Bid.class)
                .setParameter("auctionId", auctionId)
                .getResultList();
    }

    public List<Bid> findByBidder(String bidderId) {
        return em.createNamedQuery("Bid.findByBidder", Bid.class)
                .setParameter("bidderId", bidderId)
                .getResultList();
    }

    public Bid findHighestBid(Long auctionId) {
        List<Bid> list = em.createNamedQuery("Bid.findHighestForAuction", Bid.class)
                .setParameter("auctionId", auctionId)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public long countByAuction(Long auctionId) {
        return em.createNamedQuery("Bid.countByAuction", Long.class)
                .setParameter("auctionId", auctionId)
                .getSingleResult();
    }

    public long countAll() {
        return em.createQuery("SELECT COUNT(b) FROM Bid b", Long.class)
                .getSingleResult();
    }

    public void save(Bid bid) {
        em.persist(bid);
    }
}

