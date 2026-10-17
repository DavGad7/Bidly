package com.bidly.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "AUCTION_PHOTOS")
public class AuctionPhoto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "IMAGE_DATA", nullable = false)
    private byte[] imageData;

    @Column(name = "CONTENT_TYPE", nullable = false, length = 50)
    private String contentType;

    @Column(name = "FILE_NAME", length = 255)
    private String fileName;

    @Column(nullable = false)
    private int position;

    @Column(name = "PRIMARY_PHOTO", nullable = false)
    private boolean primaryPhoto = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUCTION_ID", nullable = false)
    private Auction auction;

    public AuctionPhoto() {
    }

    public AuctionPhoto(byte[] imageData, String contentType, String fileName,
                        int position, boolean primaryPhoto, Auction auction) {
        this.imageData = imageData;
        this.contentType = contentType;
        this.fileName = fileName;
        this.position = position;
        this.primaryPhoto = primaryPhoto;
        this.auction = auction;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isPrimaryPhoto() {
        return primaryPhoto;
    }

    public void setPrimaryPhoto(boolean primaryPhoto) {
        this.primaryPhoto = primaryPhoto;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }
}
