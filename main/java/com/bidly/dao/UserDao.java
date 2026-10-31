package com.bidly.dao;

import com.bidly.model.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class UserDao {

    @PersistenceContext(unitName = "BidlyPU")
    private EntityManager em;

    public User findById(String id) {
        return id == null ? null : em.find(User.class, id);
    }

    public User findByUsername(String username) {
        try {
            return em.createNamedQuery("User.findByUsername", User.class)
                     .setParameter("username", username)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public User findByEmail(String email) {
        try {
            return em.createNamedQuery("User.findByEmail", User.class)
                     .setParameter("email", email)
                     .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<User> findAll() {
        return em.createNamedQuery("User.findAll", User.class)
                 .getResultList();
    }

    public List<User> search(String keyword) {
        return em.createQuery(
                "SELECT u FROM User u WHERE LOWER(u.username) LIKE :kw OR LOWER(u.email) LIKE :kw " +
                "OR LOWER(u.firstName) LIKE :kw OR LOWER(u.lastName) LIKE :kw " +
                "ORDER BY u.createdAt DESC", User.class)
                 .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                 .getResultList();
    }

    public long countAll() {
        return em.createNamedQuery("User.countAll", Long.class)
                 .getSingleResult();
    }

    public void save(User user) {
        em.persist(user);
    }

    public User update(User user) {
        return em.merge(user);
    }

    public void flush() {
        em.flush();
    }
}
