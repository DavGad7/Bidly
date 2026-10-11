package com.bidly.dao;

import com.bidly.model.AuctionPhoto;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class AuctionPhotoDao {

    @PersistenceContext(unitName = "BidlyPU")
    private EntityManager em;

    public AuctionPhoto findById(Long id) {
        return em.find(AuctionPhoto.class, id);
    }

    public List<AuctionPhoto> findByAuction(Long auctionId) {
        return em.createQuery(
                "SELECT p FROM AuctionPhoto p WHERE p.auction.id = :auctionId " +
                "ORDER BY p.primaryPhoto DESC, p.position ASC", AuctionPhoto.class)
                 .setParameter("auctionId", auctionId)
                 .getResultList();
    }

    public void save(AuctionPhoto photo) {
        em.persist(photo);
        em.flush();
    }

    public void delete(AuctionPhoto photo) {
        AuctionPhoto managed = em.find(AuctionPhoto.class, photo.getId());
        if (managed != null) {
            em.remove(managed);
        }
    }
}
