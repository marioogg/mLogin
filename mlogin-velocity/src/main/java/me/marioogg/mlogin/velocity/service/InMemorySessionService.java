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
 * @since 30-06-2026 17:10
 */
package me.marioogg.mlogin.velocity.service;

import me.marioogg.mlogin.api.session.SessionService;
import me.marioogg.mlogin.core.model.AuthSession;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemorySessionService implements SessionService {

    private final Map<UUID, AuthSession> sessions = new ConcurrentHashMap<>();

    @Override
    public Optional<AuthSession> get(UUID uuid) {
        return Optional.ofNullable(sessions.get(uuid));
    }

    @Override
    public void save(AuthSession session) {
        sessions.put(session.getUuid(), session);
    }

    @Override
    public void remove(UUID uuid) {
        sessions.remove(uuid);
    }
}
