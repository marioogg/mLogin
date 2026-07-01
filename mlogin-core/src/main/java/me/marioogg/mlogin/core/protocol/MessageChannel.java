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
 * @since 29-06-2026 23:57
 */
package me.marioogg.mlogin.core.protocol;

public class MessageChannel {
    public static final String AUTH_STATE = "mlogin:auth:state";
    public static final String AUTH_REQUEST = "mlogin:auth:request";
    public static final String AUTH_RESPONSE_PREFIX = "mlogin:auth:response:";
    public static String responseChannel(java.util.UUID requestId) {
        return AUTH_RESPONSE_PREFIX + requestId;
    }
}