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
 * @since 30-06-2026 17:40
 */
package me.marioogg.mlogin.spigot.cache;

import me.marioogg.mlogin.core.model.AuthState;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthStateCache {

    private final Map<UUID, AuthState> states = new ConcurrentHashMap<>();

    public AuthState get(UUID uuid) {
        return states.get(uuid);
    }

    public void set(UUID uuid, AuthState state) {
        states.put(uuid, state);
    }

    public void remove(UUID uuid) {
        states.remove(uuid);
    }

    public boolean isLoggedIn(UUID uuid) {
        return states.get(uuid) == AuthState.LOGGED_IN;
    }
}
