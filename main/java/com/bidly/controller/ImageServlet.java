package com.bidly.controller;

import com.bidly.model.AuctionPhoto;
import com.bidly.service.PhotoService;
import jakarta.ejb.EJB;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/image")
public class ImageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ImageServlet.class.getName());

    static final Set<String> SAFE_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/pjpeg", "image/x-png"
    );

    @EJB
    private PhotoService photoService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing 'id' parameter.");
            return;
        }

        Long photoId;
        try {
            photoId = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid photo ID.");
            return;
        }

        AuctionPhoto photo = photoService.getPhoto(photoId);
        if (photo == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Photo not found.");
            return;
        }

        String contentType = photo.getContentType();
        if (contentType == null || !SAFE_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            contentType = "image/jpeg";
        }
        response.setContentType(contentType);
        response.setContentLength(photo.getImageData().length);

        try (OutputStream out = response.getOutputStream()) {
            out.write(photo.getImageData());
            out.flush();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error streaming photo " + photoId, e);
        }
    }
}
