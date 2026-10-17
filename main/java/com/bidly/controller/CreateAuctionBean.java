package com.bidly.controller;

import com.bidly.dao.CategoryDao;
import com.bidly.model.Auction;
import com.bidly.model.Category;
import com.bidly.service.AuctionService;
import com.bidly.service.PhotoService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class CreateAuctionBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(CreateAuctionBean.class.getName());

    private static final int MAX_PHOTOS = 10;
    private static final long MAX_PHOTO_BYTES = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "jfif"
    );
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/pjpeg", "image/x-png"
    );

    @EJB
    private AuctionService auctionService;

    @EJB
    private CategoryDao categoryDao;

    @EJB
    private PhotoService photoService;

    @Inject
    private NavigationBean navigationBean;

    private String title;
    private String description;
    private Long categoryId;
    private BigDecimal startPrice;
    private BigDecimal buyNowPrice;
    private String contactInfo;
    private String endDateStr;
    private String endTimeStr;
    private int durationDays = 7;
    private int primaryPhotoIndex = 0;
    private transient List<Part> photos;
    private String errorMessage;

    private List<Category> categories;

    @PostConstruct
    public void init() {
        categories = categoryDao.findAll();
    }

    public String createAuction() {
        errorMessage = null;

        if (!navigationBean.isLoggedIn()) {
            errorMessage = "Log in first.";
            return null;
        }

        if (title == null || title.isBlank()) {
            errorMessage = "Title is required.";
            return null;
        }
        if (description == null || description.isBlank()) {
            errorMessage = "Description is required.";
            return null;
        }
        if (categoryId == null) {
            errorMessage = "Please select a category.";
            return null;
        }
        if (startPrice == null || startPrice.compareTo(BigDecimal.ZERO) <= 0) {
            errorMessage = "Start price must be more than 0.";
            return null;
        }
        if (buyNowPrice != null && buyNowPrice.compareTo(startPrice) <= 0) {
            errorMessage = "Buy now price has to be higher than the start price.";
            return null;
        }

        LocalDateTime end;
        try {
            if (endDateStr != null && !endDateStr.isBlank()
                    && endTimeStr != null && !endTimeStr.isBlank()) {
                end = LocalDateTime.of(LocalDate.parse(endDateStr), LocalTime.parse(endTimeStr));
            } else {
                end = LocalDateTime.now().plusDays(durationDays);
            }
        } catch (Exception e) {
            errorMessage = "Invalid date or time format.";
            return null;
        }

        if (!end.isAfter(LocalDateTime.now())) {
            errorMessage = "End time must be in the future.";
            return null;
        }

        List<Part> parts = new ArrayList<>();
        if (photos != null) {
            for (Part p : photos) {
                if (p != null && p.getSize() > 0) {
                    if (p.getSize() > MAX_PHOTO_BYTES) {
                        errorMessage = String.format("%s is %.1f MB, photos can be at most 5 MB each.",
                                shortFileName(p), p.getSize() / (1024.0 * 1024));
                        return null;
                    }
                    if (!isAllowedImage(p)) {
                        errorMessage = String.format("'%s' isn't a jpg, png or webp image.",
                                shortFileName(p));
                        return null;
                    }
                    parts.add(p);
                }
            }
        }

        if (parts.size() > MAX_PHOTOS) {
            errorMessage = "You can upload up to " + MAX_PHOTOS + " photos.";
            return null;
        }

        try {
            Auction auction = new Auction();
            auction.setTitle(title.trim());
            auction.setDescription(description.trim());
            auction.setStartPrice(startPrice);
            auction.setBuyNowPrice(buyNowPrice);
            auction.setEndTime(end);
            auction.setCategory(categoryDao.findById(categoryId));
            if (contactInfo != null && !contactInfo.isBlank()) {
                auction.setContactInfo(contactInfo.trim());
            }

            auctionService.createAuction(auction, navigationBean.getUserId());

            if (!parts.isEmpty()) {
                photoService.savePhotos(auction.getId(), parts, primaryPhotoIndex);
            }

            return "/auction.xhtml?faces-redirect=true&id=" + auction.getId();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error creating auction", e);
            errorMessage = "Error creating auction: " + e.getMessage();
            return null;
        }
    }

    // ext and mime type both have to be in the lists. no svg (can have scripts)
    private static boolean isAllowedImage(Part part) {
        String fileName = part.getSubmittedFileName();
        if (fileName == null || fileName.isBlank()) {
            return false;
        }

        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            return false;
        }
        String ext = fileName.substring(dot + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return false;
        }

        String mime = part.getContentType();
        return mime != null && ALLOWED_MIME_TYPES.contains(mime.toLowerCase());
    }

    private static String shortFileName(Part part) {
        String name = part.getSubmittedFileName();
        if (name == null) return "One of the photos";
        return name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')) + 1);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(BigDecimal startPrice) {
        this.startPrice = startPrice;
    }

    public BigDecimal getBuyNowPrice() {
        return buyNowPrice;
    }

    public void setBuyNowPrice(BigDecimal buyNowPrice) {
        this.buyNowPrice = buyNowPrice;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getEndDateStr() {
        return endDateStr;
    }

    public void setEndDateStr(String endDateStr) {
        this.endDateStr = endDateStr;
    }

    public String getEndTimeStr() {
        return endTimeStr;
    }

    public void setEndTimeStr(String endTimeStr) {
        this.endTimeStr = endTimeStr;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public List<Part> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Part> photos) {
        this.photos = photos;
    }

    public int getPrimaryPhotoIndex() {
        return primaryPhotoIndex;
    }

    public void setPrimaryPhotoIndex(int primaryPhotoIndex) {
        this.primaryPhotoIndex = primaryPhotoIndex;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
