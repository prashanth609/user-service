package com.example.userservice.util;


import com.example.userservice.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;

public final class EtagUtil {

    private EtagUtil() {}

    public static String forUserSearch(
            String name,
            String email,
            String phone,
            String role,
            Instant createdFrom,
            Instant createdTo,
            Pageable pageable,
            Page<User> page
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("name=").append(nullToEmpty(name)).append('|')
                .append("email=").append(nullToEmpty(email)).append('|')
                .append("phone=").append(nullToEmpty(phone)).append('|')
                .append("role=").append(nullToEmpty(role)).append('|')
                .append("createdFrom=").append(createdFrom == null ? "" : createdFrom.toEpochMilli()).append('|')
                .append("createdTo=").append(createdTo == null ? "" : createdTo.toEpochMilli()).append('|')
                .append("page=").append(pageable.getPageNumber()).append('|')
                .append("size=").append(pageable.getPageSize()).append('|')
                .append("sort=").append(pageable.getSort()).append('|')
                .append("total=").append(page.getTotalElements()).append('|');

        // include user id + updatedAt/createdAt to change ETag when data changes
        for (User u : page.getContent()) {
            long ts = safeEpoch(u.getUpdatedAt(), u.getCreatedAt());
            sb.append(u.getId()).append(':').append(ts).append(';');
        }

        String hash = md5Hex(sb.toString());
        return "W/\"" + hash + "\""; // weak etag
    }

    private static long safeEpoch(Instant updatedAt, Instant createdAt) {
        Instant v = updatedAt != null ? updatedAt : createdAt;
        return v != null ? v.toEpochMilli() : 0L;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute ETag hash", e);
        }
    }

    public static boolean matchesIfNoneMatch(String ifNoneMatch, String etag) {
        if (ifNoneMatch == null || ifNoneMatch.isBlank()) return false;
        // header can contain multiple etags like: W/"x", W/"y"
        return ifNoneMatch.contains(etag);
    }
}