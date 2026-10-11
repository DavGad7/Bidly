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
        // Only seed an empty DB
        if (!categoryDao.findAll().isEmpty()) {
            LOG.info("DB already has data, not seeding");
            return;
        }

        LOG.info("Seeding demo data");

        seedCategories();
        seedBidIncrements();
        seedAdminUser();
        User[] users = seedSampleUsers();
        seedSampleAuctions(users);

        LOG.info("Seed done");
    }

    private void seedCategories() {
        String[][] cats = {
            {"Electronics", "Computers, phones, gadgets and accessories", "bi-laptop"},
            {"Fashion", "Clothing, shoes, bags and accessories", "bi-handbag"},
            {"Home & Garden", "Furniture, decor, kitchen and garden items", "bi-house"},
            {"Sports", "Equipment, apparel and outdoor gear", "bi-bicycle"},
            {"Collectibles", "Coins, stamps, trading cards and memorabilia", "bi-gem"},
            {"Vehicles", "Cars, motorcycles, boats and parts", "bi-car-front"},
            {"Books", "Fiction, non-fiction, textbooks and comics", "bi-book"},
            {"Art", "Paintings, sculptures and prints", "bi-palette"},
            {"Jewelry", "Rings, necklaces, watches and accessories", "bi-watch"},
            {"Other", "Items that don't fit other categories", "bi-grid"}
        };
        for (int i = 0; i < cats.length; i++) {
            categoryDao.save(new Category(cats[i][0], cats[i][1], i + 1, cats[i][2]));
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
                "MacBook Pro 14-inch (M3 chip, 16GB RAM, 512GB SSD). Excellent condition, used for studies. Includes original MagSafe charger.",
                bd("800.00"), bd("1500.00"), 5, users[0], electronics);

        Auction jordans = createAuction("Air Jordan 1 Retro High OG size 10",
                "Air Jordan 1 High OG, US size 10. Brand new in box, never worn with tags.",
                bd("200.00"), null, 7, users[1], fashion);

        Auction bike = createAuction("Trek Marlin 7 Mountain Bike 2024",
                "Trek Marlin 7 model 2024, size M. Well maintained, tuned up recently, minor wear on frame.",
                bd("350.00"), bd("700.00"), 3, users[2], sports);

        Auction pokemon = createAuction("Pokemon Base Set Complete + Charizard",
                "Complete original base set including holo Charizard. Kept in plastic protective binders.",
                bd("500.00"), null, 10, users[0], collectibles);

        Auction textbooks = createAuction("CS Textbook Bundle (8 books)",
                "Collection of CS undergraduate textbooks: CLRS Algorithms, OS Concepts, Computer Networks and Data Structures.",
                bd("50.00"), null, 4, users[1], books);

        Auction painting = createAuction("Oil Painting - Sunset",
                "Framed oil painting on canvas (60x40 cm). Sunset landscape, ready to hang.",
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

        LOG.info("Seeded 6 sample auctions (3 with bids)");
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
        auction.setViewCount((int) (Math.random() * 50) + 5);
        auctionDao.save(auction);
        return auction;
    }

    private void seedBids(Auction auction, User[] users) {
        User seller = auction.getSeller();
        BigDecimal price = auction.getCurrentPrice();

        for (User user : users) {
            if (user.getId().equals(seller.getId())) continue;

            BigDecimal amount = price.add(bidIncrementDao.incrementFor(price));
            Bid bid = new Bid(auction, user, amount,
                              LocalDateTime.now().minusHours((long) (Math.random() * 20) + 1),
                              false);
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
