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
public class AuthRequest {
    private final UUID requestId;
    private final RequestType type;
    private final UUID playerUuid;
    private final String username;
    private final String encryptedPassword;
    private final String extra;
    private final long timestamp;
    private final int protocolVersion;
    // Client IP address, used for IP-based rate limiting. Nullable for
    // backwards compatibility with older clients that do not send it.
    private final String ip;

    public static AuthRequest of(RequestType type, UUID playerUuid, String username, String encryptedPassword) {
        return of(type, playerUuid, username, encryptedPassword, null, null);
    }

    public static AuthRequest of(RequestType type, UUID playerUuid, String username, String encryptedPassword, String extra) {
        return of(type, playerUuid, username, encryptedPassword, extra, null);
    }

    public static AuthRequest of(RequestType type, UUID playerUuid, String username, String encryptedPassword, String extra, String ip) {
        return new AuthRequest(
                UUID.randomUUID(),
                type,
                playerUuid,
                username,
                encryptedPassword,
                extra,
                System.currentTimeMillis(),
                ProtocolVersion.CURRENT,
                ip
        );
    }
}
