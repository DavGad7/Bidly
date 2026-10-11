package com.bidly.service;

import com.bidly.dao.AuctionDao;
import com.bidly.dao.AuctionPhotoDao;
import com.bidly.dao.UserDao;
import com.bidly.interceptor.Logged;
import com.bidly.model.Auction;
import com.bidly.model.AuctionPhoto;
import com.bidly.model.User;
import com.bidly.model.UserRole;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@Logged
public class PhotoService {

    private static final Logger LOG = Logger.getLogger(PhotoService.class.getName());

    @EJB
    private AuctionPhotoDao photoDao;

    @EJB
    private AuctionDao auctionDao;

    @EJB
    private UserDao userDao;

    public void savePhotos(Long auctionId, List<Part> parts, int primaryIndex) {
        Auction auction = auctionDao.findById(auctionId);
        if (auction == null) {
            LOG.warning("Auction not found for photo upload: " + auctionId);
            return;
        }

        int position = 0;
        Long thumbId = null;

        for (int i = 0; i < parts.size(); i++) {
            Part part = parts.get(i);
            if (part == null || part.getSize() == 0) continue;

            try {
                byte[] data = part.getInputStream().readAllBytes();
                String contentType = part.getContentType();
                String fileName = extractFileName(part);
                boolean isPrimary = (i == primaryIndex);

                AuctionPhoto photo = new AuctionPhoto(
                        data, contentType, fileName, position, isPrimary, auction);
                photoDao.save(photo);

                if (isPrimary || thumbId == null) {
                    thumbId = photo.getId();
                }

                position++;
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Failed to read uploaded file", e);
            }
        }

        if (thumbId != null) {
            auction.setThumbnailPhotoId(thumbId);
            auctionDao.update(auction);
        }
    }

    public AuctionPhoto getPhoto(Long photoId) {
        return photoDao.findById(photoId);
    }

    public List<AuctionPhoto> getAuctionPhotos(Long auctionId) {
        return photoDao.findByAuction(auctionId);
    }

    public void deletePhoto(Long photoId, String callerId) {
        AuctionPhoto photo = photoDao.findById(photoId);
        if (photo == null) return;
        Auction auction = auctionDao.findById(photo.getAuction().getId());
        checkCanEdit(auction, callerId);

        photoDao.delete(photo);

        List<AuctionPhoto> remaining = photoDao.findByAuction(auction.getId());
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setPosition(i);
            remaining.get(i).setPrimaryPhoto(i == 0);
        }
        auction.setThumbnailPhotoId(remaining.isEmpty() ? null : remaining.get(0).getId());
        auctionDao.update(auction);
    }

    public void setPrimaryPhoto(Long auctionId, Long photoId, String callerId) {
        Auction auction = auctionDao.findById(auctionId);
        if (auction == null) return;
        checkCanEdit(auction, callerId);

        List<AuctionPhoto> photos = photoDao.findByAuction(auctionId);
        boolean found = false;
        for (AuctionPhoto p : photos) {
            if (p.getId().equals(photoId)) {
                found = true;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Photo not found in this auction.");
        }

        for (AuctionPhoto p : photos) {
            p.setPrimaryPhoto(p.getId().equals(photoId));
        }
        auction.setThumbnailPhotoId(photoId);
        auctionDao.update(auction);
    }

    private void checkCanEdit(Auction auction, String callerId) {
        User caller = userDao.findById(callerId);
        boolean isAdmin = caller != null && caller.getRole() == UserRole.ADMIN;
        boolean isSeller = caller != null && auction.getSeller().getId().equals(caller.getId());
        if (!isAdmin && !isSeller) {
            throw new SecurityException("Only the seller or an administrator can change this auction's photos.");
        }
    }

    private String extractFileName(Part part) {
        String header = part.getHeader("content-disposition");
        if (header != null) {
            for (String token : header.split(";")) {
                if (token.trim().startsWith("filename")) {
                    String name = token.substring(token.indexOf('=') + 1).trim()
                                       .replace("\"", "");
                    // can be a full windows path
                    int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
                    if (slash >= 0) {
                        name = name.substring(slash + 1);
                    }
                    return name;
                }
            }
        }
        return "unknown";
    }
}
