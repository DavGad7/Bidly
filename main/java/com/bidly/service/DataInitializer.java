package com.bidly.service;

import com.bidly.dao.*;
import com.bidly.model.*;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.logging.Logger;

@Singleton
@Startup
public class DataInitializer {

    private static final Logger LOG = Logger.getLogger(DataInitializer.class.getName());

    @EJB
    private CategoryDao categoryDao;
    @EJB
    private BidIncrementDao bidIncrementDao;
    @EJB
    private UserDao userDao;
    @EJB
    private AuctionDao auctionDao;
    @EJB
    private AuctionPhotoDao auctionPhotoDao;
    @EJB
    private BidDao bidDao;

    @PostConstruct
    public void init() {
        // skip if already seeded
        if (!categoryDao.findAll().isEmpty()) {
            LOG.info("db not empty, no seed");
            return;
        }

        LOG.info("empty db - adding demo data");

        seedCategories();
        seedBidIncrements();
        seedAdminUser();
        User[] users = seedSampleUsers();
        seedSampleAuctions(users);

        LOG.info("demo data done");
    }

    private void seedCategories() {
        String[] cats = {"Electronics", "Fashion", "Home & Garden", "Sports", "Collectibles",
                "Vehicles", "Books", "Art", "Jewelry", "Other"};
        for (int i = 0; i < cats.length; i++) {
            categoryDao.save(new Category(cats[i], i + 1));
        }
        LOG.info("Seeded " + cats.length + " categories.");
    }

    private void seedBidIncrements() {
        bidIncrementDao.save(new BidIncrement(bd("0.01"), bd("9.99"), bd("0.50")));
        bidIncrementDao.save(new BidIncrement(bd("10.00"), bd("49.99"), bd("1.00")));
        bidIncrementDao.save(new BidIncrement(bd("50.00"), bd("199.99"), bd("2.00")));
        bidIncrementDao.save(new BidIncrement(bd("200.00"), bd("499.99"), bd("5.00")));
        bidIncrementDao.save(new BidIncrement(bd("500.00"), bd("999.99"), bd("10.00")));
        bidIncrementDao.save(new BidIncrement(bd("1000.00"), bd("4999.99"), bd("25.00")));
        bidIncrementDao.save(new BidIncrement(bd("5000.00"), null, bd("50.00")));
    }

    private void seedAdminUser() {
        User admin = newUser("admin", "admin@bidly.com", "Admin123!", "Admin", "User", 30);
        admin.setRole(UserRole.ADMIN);
        userDao.save(admin);
    }

    private User[] seedSampleUsers() {
        User[] users = {
            newUser("yossi123", "yossi@example.com", "Password1", "Yossi", "Cohen", 20),
            newUser("dana_m", "dana@example.com", "Password1", "Dana", "Mizrahi", 15),
            newUser("avi_k", "avi@example.com", "Password1", "Avi", "Katz", 10)
        };
        for (User user : users) {
            userDao.save(user);
        }
        return users;
    }

    private User newUser(String username, String email, String password,
                         String firstName, String lastName, int daysAgo) {
        User user = new User(username, email, UserService.hashPassword(password), firstName, lastName);
        user.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
        return user;
    }

    private void seedSampleAuctions(User[] users) {
        Category electronics = categoryDao.findByName("Electronics");
        Category fashion = categoryDao.findByName("Fashion");
        Category sports = categoryDao.findByName("Sports");
        Category collectibles = categoryDao.findByName("Collectibles");
        Category books = categoryDao.findByName("Books");
        Category art = categoryDao.findByName("Art");

        Auction macbook = createAuction("MacBook Pro 14 M3",
                "macbook pro 14 m3, 16gb ram, 512gb. good condition, used it for uni. comes with the charger",
                bd("800.00"), bd("1500.00"), 5, users[0], electronics);

        Auction jordans = createAuction("Air Jordan 1 Retro High OG size 10",
                "New in box, size 10 US. Never worn",
                bd("200.00"), null, 7, users[1], fashion);

        Auction bike = createAuction("Trek Marlin 7 Mountain Bike 2024",
                "Trek Marlin 7, 2024, size M. some scratches on the frame but rides fine",
                bd("350.00"), bd("700.00"), 3, users[2], sports);

        Auction pokemon = createAuction("Pokemon Base Set Complete + Charizard",
                "full base set + holo charizard",
                bd("500.00"), null, 10, users[0], collectibles);

        Auction textbooks = createAuction("CS Textbook Bundle (8 books)",
                "8 books from my degree - algorithms (CLRS), OS, networks, data structures",
                bd("50.00"), null, 4, users[1], books);

        Auction painting = createAuction("Oil Painting - Sunset",
                "Oil painting with frame, 60x40",
                bd("150.00"), bd("500.00"), 6, users[2], art);

        attachPhotos(macbook, "mac1.webp", "mac2.webp");
        attachPhotos(jordans, "air_jordan.jfif");
        attachPhotos(bike, "bike.jfif");
        attachPhotos(pokemon, "pokemon.jfif");
        attachPhotos(textbooks, "textbooks.jfif");
        attachPhotos(painting, "oilPaint.jfif");

        seedBids(macbook, users);
        seedBids(jordans, users);
        seedBids(pokemon, users);

        LOG.info("Seeded 6 sample auctions with bids.");
    }

    private void attachPhotos(Auction auction, String... resourceNames) {
        for (int i = 0; i < resourceNames.length; i++) {
            String name = resourceNames[i];
            try (InputStream in = DataInitializer.class.getResourceAsStream("/sample-images/" + name)) {
                if (in == null) {
                    LOG.warning("Sample auction image not found: " + name);
                    continue;
                }
                AuctionPhoto photo = new AuctionPhoto(
                        in.readAllBytes(), contentTypeFor(name), name, i, i == 0, auction);
                auctionPhotoDao.save(photo);
                if (i == 0) {
                    auction.setThumbnailPhotoId(photo.getId());
                    auctionDao.update(auction);
                }
            } catch (IOException e) {
                LOG.warning("Failed to load sample auction image " + name + ": " + e.getMessage());
            }
        }
    }

    private String contentTypeFor(String resourceName) {
        String lower = resourceName.toLowerCase();
        if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        if (lower.endsWith(".jfif") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }

    private Auction createAuction(String title, String description,
                                  BigDecimal startPrice, BigDecimal buyNow,
                                  int daysUntilEnd, User seller, Category category) {
        Auction auction = new Auction();
        auction.setTitle(title);
        auction.setDescription(description);
        auction.setStartPrice(startPrice);
        auction.setCurrentPrice(startPrice);
        auction.setBuyNowPrice(buyNow);
        auction.setStartTime(LocalDateTime.now().minusDays(1));
        auction.setEndTime(LocalDateTime.now().plusDays(daysUntilEnd));
        auction.setStatus(AuctionStatus.ACTIVE);
        auction.setSeller(seller);
        auction.setCategory(category);
        auction.setCreatedAt(LocalDateTime.now().minusDays(1));
        auction.setViewCount(0);
        auctionDao.save(auction);
        return auction;
    }

    private void seedBids(Auction auction, User[] users) {
        User seller = auction.getSeller();
        BigDecimal price = auction.getCurrentPrice();
        int hoursAgo = 5;

        for (User user : users) {
            if (user.getId().equals(seller.getId())) continue;

            BigDecimal amount = price.add(bidIncrementDao.incrementFor(price));
            Bid bid = new Bid(auction, user, amount, LocalDateTime.now().minusHours(hoursAgo), false);
            hoursAgo--;
            bidDao.save(bid);

            price = amount;
        }

        auction.setCurrentPrice(price);
        auctionDao.update(auction);
    }

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }
}
