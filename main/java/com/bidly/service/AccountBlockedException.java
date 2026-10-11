package com.bidly.service;

import jakarta.ejb.ApplicationException;

@ApplicationException
public class AccountBlockedException extends RuntimeException {

    private final String reason;

    public AccountBlockedException(String reason) {
        super("Account is blocked");
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public static String messageFor(String reason) {
        return "Your account has been blocked by an administrator."
                + (reason != null ? " Reason: " + reason : "");
    }
}
