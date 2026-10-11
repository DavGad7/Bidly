package com.bidly.dao;

import com.bidly.model.Category;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class CategoryDao {

    @PersistenceContext(unitName = "BidlyPU")
    private EntityManager em;

    public List<Category> findAll() {
        return em.createNamedQuery("Category.findAll", Category.class)
                .getResultList();
    }

    public Category findById(Long id) {
        return em.find(Category.class, id);
    }

    public Category findByName(String name) {
        List<Category> list = em.createNamedQuery("Category.findByName", Category.class)
                .setParameter("name", name)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public void save(Category category) {
        em.persist(category);
    }
}
