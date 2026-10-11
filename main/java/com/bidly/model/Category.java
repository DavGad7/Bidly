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

    @Column(length = 200)
    private String description;

    @Column(name = "DISPLAY_ORDER")
    private int displayOrder;

    @Column(name = "ICON_CLASS", length = 40)
    private String iconClass;

    @OneToMany(mappedBy = "category")
    private List<Auction> auctions = new ArrayList<>();

    public Category() {
    }

    public Category(String name, String description, int displayOrder, String iconClass) {
        this.name = name;
        this.description = description;
        this.displayOrder = displayOrder;
        this.iconClass = iconClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category other = (Category) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getIconClass() {
        return iconClass;
    }

    public void setIconClass(String iconClass) {
        this.iconClass = iconClass;
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

