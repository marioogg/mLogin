/**
 * Copyright © 2026 marioogg <a href="https://github.com/marioogg">https://github.com/marioogg</a>
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * @author marioogg
 * @since 30-06-2026 18:01
 */
package me.marioogg.mlogin.core.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private final UUID requestId;
    private final boolean success;
    private final ResponseReason reason;
    private final long timestamp;
    // only relevant when reason is RATE_LIMITED, otherwise 0
    private final long retryAfterSeconds;

    public static AuthResponse of(UUID requestId, boolean success, ResponseReason reason) {
        return new AuthResponse(requestId, success, reason, System.currentTimeMillis(), 0);
    }

    public static AuthResponse ok(UUID requestId) {
        return of(requestId, true, ResponseReason.OK);
    }

    public static AuthResponse fail(UUID requestId, ResponseReason reason) {
        return of(requestId, false, reason);
    }

    public static AuthResponse rateLimited(UUID requestId, long retryAfterSeconds) {
        return new AuthResponse(requestId, false, ResponseReason.RATE_LIMITED,
                System.currentTimeMillis(), retryAfterSeconds);
    }
}