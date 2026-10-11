package com.bidly.dao;

import com.bidly.model.WatchlistEntry;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class WatchlistDao {

    @PersistenceContext(unitName = "BidlyPU")
    private EntityManager em;

    public List<WatchlistEntry> findByUser(String userId) {
        return em.createNamedQuery("WatchlistEntry.findByUser", WatchlistEntry.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public WatchlistEntry findByUserAndAuction(String userId, Long auctionId) {
        List<WatchlistEntry> list = em.createNamedQuery("WatchlistEntry.findByUserAndAuction", WatchlistEntry.class)
                .setParameter("userId", userId)
                .setParameter("auctionId", auctionId)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public List<String> findWatchersForAuction(Long auctionId) {
        return em.createNamedQuery("WatchlistEntry.findWatchersForAuction", String.class)
                .setParameter("auctionId", auctionId)
                .getResultList();
    }

    public void save(WatchlistEntry entry) {
        em.persist(entry);
    }

    public void delete(WatchlistEntry entry) {
        WatchlistEntry managed = em.find(WatchlistEntry.class, entry.getId());
        if (managed != null) {
            em.remove(managed);
        }
    }
}
