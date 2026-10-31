package com.bidly.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CATEGORIES")
@NamedQueries({
    @NamedQuery(name = "Category.findAll",
                query = "SELECT c FROM Category c ORDER BY c.displayOrder ASC"),
    @NamedQuery(name = "Category.findByName",
                query = "SELECT c FROM Category c WHERE c.name = :name")
})
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "DISPLAY_ORDER")
    private int displayOrder;

    @OneToMany(mappedBy = "category")
    private List<Auction> auctions = new ArrayList<>();

    public Category() {
    }

    public Category(String name, int displayOrder) {
        this.name = name;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public List<Auction> getAuctions() {
        return auctions;
    }

    public void setAuctions(List<Auction> auctions) {
        this.auctions = auctions;
    }

    @Override
    public String toString() {
        return name;
    }
}

