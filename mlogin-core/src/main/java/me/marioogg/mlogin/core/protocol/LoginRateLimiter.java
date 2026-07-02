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
 * @since 02-07-2026 10:00
 */
package me.marioogg.mlogin.core.protocol;

import me.marioogg.mlogin.core.database.RedisManager;

import java.util.UUID;

public class LoginRateLimiter {

    private static final String ATTEMPTS_PREFIX = "mlogin:rl:attempts:";
    private static final String BLOCK_PREFIX = "mlogin:rl:block:";
    private static final String STRIKES_PREFIX = "mlogin:rl:strikes:";

    private final RedisManager redis;
    private final int maxAttempts;
    private final int attemptWindowSeconds;
    private final int baseBlockSeconds;
    private final int maxBlockSeconds;
    private final int strikeWindowSeconds;

    public LoginRateLimiter(RedisManager redis, int maxAttempts, int attemptWindowSeconds,
                            int baseBlockSeconds, int maxBlockSeconds, int strikeWindowSeconds) {
        this.redis = redis;
        this.maxAttempts = maxAttempts;
        this.attemptWindowSeconds = attemptWindowSeconds;
        this.baseBlockSeconds = baseBlockSeconds;
        this.maxBlockSeconds = maxBlockSeconds;
        this.strikeWindowSeconds = strikeWindowSeconds;
    }

    public long getBlockedSecondsRemaining(UUID uuid) {
        return redis.getTtlSeconds(BLOCK_PREFIX + uuid);
    }

    public void registerFailure(UUID uuid) {
        long attempts = redis.incrementWithExpiry(ATTEMPTS_PREFIX + uuid, attemptWindowSeconds);
        if (attempts < maxAttempts) {
            return;
        }

        long strikes = redis.incrementWithExpiry(STRIKES_PREFIX + uuid, strikeWindowSeconds);
        long blockSeconds = Math.min(maxBlockSeconds, baseBlockSeconds * (1L << Math.min(strikes - 1, 20)));

        redis.setWithExpiry(BLOCK_PREFIX + uuid, (int) blockSeconds);
        redis.delete(ATTEMPTS_PREFIX + uuid);
    }

    public void registerSuccess(UUID uuid) {
        redis.delete(ATTEMPTS_PREFIX + uuid, BLOCK_PREFIX + uuid);
    }
}