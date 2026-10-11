package com.bidly.dao;

import com.bidly.model.Auction;
import com.bidly.model.AuctionStatus;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class AuctionDao {

    @PersistenceContext(unitName = "BidlyPU")
    private EntityManager em;

    public Auction findById(Long id) {
        return em.find(Auction.class, id);
    }

    public List<Auction> findBySeller(String sellerId) {
        return em.createNamedQuery("Auction.findBySeller", Auction.class)
                 .setParameter("sellerId", sellerId)
                 .getResultList();
    }

    public List<Auction> findByWinner(String userId) {
        return em.createQuery(
                "SELECT a FROM Auction a WHERE a.winner.id = :userId " +
                "ORDER BY a.endTime DESC", Auction.class)
                 .setParameter("userId", userId)
                 .getResultList();
    }

    public List<Auction> findOpenWithBidsFrom(String bidderId) {
        return em.createQuery(
                "SELECT DISTINCT b.auction FROM Bid b WHERE b.bidder.id = :bidderId " +
                "AND b.auction.status IN (com.bidly.model.AuctionStatus.ACTIVE, com.bidly.model.AuctionStatus.SUSPENDED)",
                Auction.class)
                 .setParameter("bidderId", bidderId)
                 .getResultList();
    }

    public List<Auction> search(String keyword, Long categoryId,
                                String sortBy, int page, int pageSize) {
        StringBuilder jpql = new StringBuilder(
                // also filter by end time
                "SELECT a FROM Auction a WHERE a.status = :status AND a.endTime > :now"
                + " AND a.seller.blocked = false");

        if (keyword != null && !keyword.isBlank()) {
            jpql.append(" AND (LOWER(a.title) LIKE :kw OR LOWER(a.description) LIKE :kw)");
        }
        if (categoryId != null) {
            jpql.append(" AND a.category.id = :catId");
        }

        String sort = (sortBy != null) ? sortBy : "ENDING_SOON";
        String orderBy;
        switch (sort) {
            case "PRICE_LOW":
                orderBy = " ORDER BY a.currentPrice ASC";
                break;
            case "PRICE_HIGH":
                orderBy = " ORDER BY a.currentPrice DESC";
                break;
            case "NEWEST":
                orderBy = " ORDER BY a.createdAt DESC";
                break;
            default:
                orderBy = " ORDER BY a.endTime ASC";
                break;
        }
        jpql.append(orderBy);

        TypedQuery<Auction> q = em.createQuery(jpql.toString(), Auction.class)
                .setParameter("status", AuctionStatus.ACTIVE)
                .setParameter("now", LocalDateTime.now());

        if (keyword != null && !keyword.isBlank()) {
            q.setParameter("kw", "%" + keyword.toLowerCase() + "%");
        }
        if (categoryId != null) {
            q.setParameter("catId", categoryId);
        }

        q.setFirstResult((page - 1) * pageSize);
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    public long countSearchResults(String keyword, Long categoryId) {
        StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(a) FROM Auction a WHERE a.status = :status AND a.endTime > :now"
                + " AND a.seller.blocked = false");

        if (keyword != null && !keyword.isBlank()) {
            jpql.append(" AND (LOWER(a.title) LIKE :kw OR LOWER(a.description) LIKE :kw)");
        }
        if (categoryId != null) {
            jpql.append(" AND a.category.id = :catId");
        }

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class)
                .setParameter("status", AuctionStatus.ACTIVE)
                .setParameter("now", LocalDateTime.now());

        if (keyword != null && !keyword.isBlank()) {
            query.setParameter("kw", "%" + keyword.toLowerCase() + "%");
        }
        if (categoryId != null) {
            query.setParameter("catId", categoryId);
        }

        return query.getSingleResult();
    }

    public List<Auction> findExpiredActive(LocalDateTime now) {
        return em.createNamedQuery("Auction.findExpiredActive", Auction.class)
                 .setParameter("now", now)
                 .getResultList();
    }

    public long countByStatus(AuctionStatus status) {
        return em.createNamedQuery("Auction.countByStatus", Long.class)
                 .setParameter("status", status)
                 .getSingleResult();
    }

    public List<Auction> findAll() {
        return em.createQuery(
                "SELECT a FROM Auction a ORDER BY a.createdAt DESC", Auction.class)
                 .getResultList();
    }

    public void incrementViewCount(Long id) {
        em.createQuery("UPDATE Auction a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
          .setParameter("id", id)
          .executeUpdate();
    }

    public void save(Auction auction) {
        em.persist(auction);
    }

    public Auction update(Auction auction) {
        return em.merge(auction);
    }

    public void delete(Auction auction) {
        Auction managed = em.find(Auction.class, auction.getId());
        if (managed == null) return;
        // delete dependent rows first (no cascade)
        em.createQuery("DELETE FROM Bid b WHERE b.auction.id = :id")
          .setParameter("id", managed.getId())
          .executeUpdate();
        em.createQuery("DELETE FROM AutoBid a WHERE a.auction.id = :id")
          .setParameter("id", managed.getId())
          .executeUpdate();
        em.createQuery("DELETE FROM WatchlistEntry w WHERE w.auction.id = :id")
          .setParameter("id", managed.getId())
          .executeUpdate();

        em.remove(managed);
    }
}
