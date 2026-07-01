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
 * @since 30-06-2026 17:12
 */
package me.marioogg.mlogin.velocity.service;

import com.google.gson.Gson;
import me.marioogg.mlogin.api.auth.AuthService;
import me.marioogg.mlogin.api.session.SessionService;
import me.marioogg.mlogin.api.user.UserRepository;
import me.marioogg.mlogin.core.database.RedisManager;
import me.marioogg.mlogin.core.model.AuthMessage;
import me.marioogg.mlogin.core.model.AuthSession;
import me.marioogg.mlogin.core.model.AuthState;
import me.marioogg.mlogin.core.protocol.MessageChannel;
import me.marioogg.mlogin.core.protocol.ProtocolVersion;

import java.util.UUID;
public class VelocityAuthService implements AuthService {

    private final SessionService sessions;
    private final UserRepository users;
    private final RedisManager redis;
    private final Gson gson = new Gson();

    public VelocityAuthService(SessionService sessions, UserRepository users, RedisManager redis) {
        this.sessions = sessions;
        this.users = users;
        this.redis = redis;
    }

    @Override
    public AuthState getState(UUID uuid) {
        return sessions.get(uuid)
                .map(AuthSession::getState)
                .orElseGet(() -> users.exists(uuid) ? AuthState.LOGIN_REQUIRED : AuthState.REGISTER_REQUIRED);
    }

    @Override
    public void setState(UUID uuid, AuthState state) {
        sessions.save(new AuthSession(uuid, state, System.currentTimeMillis()));
        broadcast(uuid, state);
    }

    @Override
    public boolean isAuthenticated(UUID uuid) {
        return getState(uuid) == AuthState.LOGGED_IN;
    }

    private void broadcast(UUID uuid, AuthState state) {
        AuthMessage message = new AuthMessage(uuid, state, System.currentTimeMillis(), ProtocolVersion.CURRENT);
        redis.publish(MessageChannel.AUTH_STATE, gson.toJson(message));
    }
}
