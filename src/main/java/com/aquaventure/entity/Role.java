package com.aquaventure.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The design ERD names the customer-facing role "TOURIST". This enum keeps the
 * constant SURFER instead, because a prior iteration of this backend already
 * persisted real user accounts with that role value -- renaming the stored
 * value would have meant discarding real data. "TOURIST" is still accepted
 * everywhere as an input alias (see {@link #fromJson}), so the external API
 * contract matches the spec exactly even though the internal constant name
 * differs.
 */
public enum Role {
    SURFER,
    PROVIDER,
    INSTRUCTOR,
    ADMIN;

    @JsonCreator
    public static Role fromJson(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.equals("TOURIST")) {
            normalized = "SURFER";
        }
        return Role.valueOf(normalized);
    }
}
