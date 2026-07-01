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
 * @since 30-06-2026 10:33
 */
package me.marioogg.mlogin.velocity.listener;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.Player;
import me.marioogg.mlogin.api.auth.AuthService;
import me.marioogg.mlogin.api.session.SessionService;
import me.marioogg.mlogin.api.user.UserRepository;
import me.marioogg.mlogin.core.model.AuthState;
import me.marioogg.mlogin.core.util.UuidUtil;
import me.marioogg.mlogin.velocity.util.MojangApi;

import java.util.UUID;

public class LoginListener {

    private final AuthService authService;
    private final SessionService sessionService;
    private final UserRepository users;

    public LoginListener(AuthService authService, SessionService sessionService, UserRepository users) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.users = users;
    }

    @Subscribe(order = PostOrder.EARLY)
    public EventTask onPreLogin(PreLoginEvent event) {
        String username = event.getUsername();
        UUID offlineUuid = UuidUtil.offlineUuid(username);

        if (users.exists(offlineUuid)) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
            return null;
        }

        return EventTask.async(() -> {
            boolean premiumName = MojangApi.usernameExists(username);
            event.setResult(premiumName
                    ? PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
                    : PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
        });
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.isOnlineMode()) {
            authService.setState(uuid, AuthState.LOGGED_IN);
        } else {
            AuthState state = users.exists(uuid) ? AuthState.LOGIN_REQUIRED : AuthState.REGISTER_REQUIRED;
            authService.setState(uuid, state);
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        sessionService.remove(event.getPlayer().getUniqueId());
    }
}
