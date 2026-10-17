package com.bidly.service;

import com.bidly.dao.AuctionDao;
import com.bidly.dao.BidDao;
import com.bidly.dao.UserDao;
import com.bidly.model.Auction;
import com.bidly.model.AuctionStatus;
import com.bidly.model.Bid;
import com.bidly.model.User;
import com.bidly.model.UserRole;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.time.LocalDateTime;
import org.mindrot.jbcrypt.BCrypt;

@Stateless
@Logged
public class UserService {

    @EJB
    private UserDao userDao;

    @EJB
    private AuctionDao auctionDao;

    @EJB
    private BidDao bidDao;

    public User register(String username, String email, String password,
                         String firstName, String lastName) {
        username = checkField(username, "Username", 50);
        email = checkField(email, "Email", 100);
        firstName = checkField(firstName, "First name", 50);
        lastName = checkField(lastName, "Last name", 50);
        checkPassword(password);

        if (userDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }
        if (userDao.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered.");
        }

        User user = new User(username, email, hashPassword(password), firstName, lastName);
        user.setCreatedAt(LocalDateTime.now());
        userDao.save(user);
        return user;
    }

    public User login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user == null) return null;
        if (!BCrypt.checkpw(password, user.getPasswordHash())) return null;
        if (user.isBlocked()) throw new AccountBlockedException(user.getBlockReason());
        return user;
    }

    public User findById(String id) {
        return userDao.findById(id);
    }

    public void updateProfile(String userId, String firstName, String lastName, String email) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        firstName = checkField(firstName, "First name", 50);
        lastName = checkField(lastName, "Last name", 50);
        email = checkField(email, "Email", 100);

        if (!user.getEmail().equals(email)) {
            User existing = userDao.findByEmail(email);
            if (existing != null && !existing.getId().equals(userId)) {
                throw new IllegalArgumentException("Email '" + email + "' is already registered.");
            }
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        userDao.update(user);
    }

    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        checkPassword(newPassword);

        if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        user.setPasswordHash(hashPassword(newPassword));
        userDao.update(user);
    }

    private static String checkField(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        String v = value.trim();
        if (v.length() > maxLength) {
            throw new IllegalArgumentException(field + " can be at most " + maxLength + " characters.");
        }
        return v;
    }

    private static void checkPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
    }

    public void blockUser(String userId, String callerId, String reason) {
        requireAdmin(callerId);
        User user = userDao.findById(userId);
        if (user == null) return;

        user.setBlocked(true);
        user.setBlockReason(cleanReason(reason));
        userDao.update(user);

        for (Auction auction : auctionDao.findBySeller(userId)) {
            if (auction.getStatus() == AuctionStatus.ACTIVE) {
                auction.setStatus(AuctionStatus.SUSPENDED);
                auction.setSuspendedBy(Auction.SuspendedBy.BLOCK);
                auctionDao.update(auction);
            }
        }

        recalcPrices(userId);
    }

    public void unblockUser(String userId, String callerId) {
        requireAdmin(callerId);
        User user = userDao.findById(userId);
        if (user == null) return;

        user.setBlocked(false);
        user.setBlockReason(null);
        userDao.update(user);

        // only the ones blockUser() suspended
        for (Auction auction : auctionDao.findBySeller(userId)) {
            if (auction.getStatus() == AuctionStatus.SUSPENDED
                    && auction.getSuspendedBy() == Auction.SuspendedBy.BLOCK) {
                auction.setStatus(AuctionStatus.ACTIVE);
                auction.setSuspendedBy(null);
                auctionDao.update(auction);
            }
        }

        recalcPrices(userId);
    }

    private void recalcPrices(String userId) {
        // flush first so the query sees the blocked flag
        userDao.flush();
        for (Auction auction : auctionDao.findOpenWithBidsFrom(userId)) {
            Bid top = bidDao.findHighestBid(auction.getId());
            auction.setCurrentPrice(top != null ? top.getAmount() : auction.getStartPrice());
            auctionDao.update(auction);
        }
    }

    static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    static String cleanReason(String reason) {
        if (reason == null || reason.isBlank()) return null;
        String r = reason.trim();
        return r.length() > 500 ? r.substring(0, 500) : r;
    }

    private void requireAdmin(String callerId) {
        User caller = userDao.findById(callerId);
        if (caller == null || caller.getRole() != UserRole.ADMIN) {
            throw new SecurityException("Admins only.");
        }
    }

    public int getReputation(String userId) {
        int sales = 0;
        for (Auction a : auctionDao.findBySeller(userId)) {
            if (a.getStatus() == AuctionStatus.CLOSED && a.getWinner() != null) {
                sales++;
            }
        }
        return sales + auctionDao.findByWinner(userId).size();
    }
}
