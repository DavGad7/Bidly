package com.bidly.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "BID_INCREMENTS")
@NamedQueries({
    @NamedQuery(name = "BidIncrement.findForPrice",
                query = "SELECT b FROM BidIncrement b " +
                        "WHERE b.fromPrice <= :price " +
                        "AND (b.toPrice IS NULL OR b.toPrice >= :price)")
})
public class BidIncrement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "FROM_PRICE", nullable = false, precision = 10, scale = 2)
    private BigDecimal fromPrice;

    @Column(name = "TO_PRICE", precision = 10, scale = 2)
    private BigDecimal toPrice;

    @Column(name = "INCREMENT_STEP", nullable = false, precision = 10, scale = 2)
    private BigDecimal increment;

    public BidIncrement() {
    }

    public BidIncrement(BigDecimal fromPrice, BigDecimal toPrice, BigDecimal increment) {
        this.fromPrice = fromPrice;
        this.toPrice = toPrice;
        this.increment = increment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getFromPrice() {
        return fromPrice;
    }

    public void setFromPrice(BigDecimal fromPrice) {
        this.fromPrice = fromPrice;
    }

    public BigDecimal getToPrice() {
        return toPrice;
    }

    public void setToPrice(BigDecimal toPrice) {
        this.toPrice = toPrice;
    }

    public BigDecimal getIncrement() {
        return increment;
    }

    public void setIncrement(BigDecimal increment) {
        this.increment = increment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BidIncrement other = (BidIncrement) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
