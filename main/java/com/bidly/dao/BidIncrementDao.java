package com.bidly.dao;

import com.bidly.model.BidIncrement;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;

@Stateless
public class BidIncrementDao {

    @PersistenceContext
    private EntityManager em;

    public BidIncrement findForPrice(BigDecimal price) {
        List<BidIncrement> list = em.createNamedQuery("BidIncrement.findForPrice", BidIncrement.class)
                .setParameter("price", price)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public BigDecimal incrementFor(BigDecimal price) {
        BidIncrement increment = findForPrice(price);
        return increment != null ? increment.getIncrement() : BigDecimal.ONE;
    }

    public void save(BidIncrement bidIncrement) {
        em.persist(bidIncrement);
    }
}
