package com.hotel.stayease.security;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory blacklist of invalidated JWT tokens (e.g. via logout).
 * Note: this is cleared on application restart. For production use, back this
 * with a persistent or distributed store (Redis, DB) and prune by token expiry.
 */
@Component
public class TokenBlacklist {

    private final Set<String> blacklisted = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void blacklist(String token) {
        if (token != null && !token.isBlank()) {
            blacklisted.add(token);
        }
    }

    public boolean isBlacklisted(String token) {
        return token != null && blacklisted.contains(token);
    }
}

