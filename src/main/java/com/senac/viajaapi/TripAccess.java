package com.senac.viajaapi;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TripAccess {
    private final TripRepository trips;
    private final UserRepository users;

    public TripAccess(TripRepository trips, UserRepository users) {
        this.trips = trips;
        this.users = users;
    }

    public Long currentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new AccessDeniedException("Access denied");
        return users.findByEmail(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Access denied")).getId();
    }

    public Trip requireOwner(Long tripId) {
        if (tripId == null) throw new IllegalArgumentException("Trip is required");
        Trip trip = trips.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip does not exist"));
        if (!currentUserId().equals(trip.getUserId())) throw new AccessDeniedException("Access denied");
        return trip;
    }
}
