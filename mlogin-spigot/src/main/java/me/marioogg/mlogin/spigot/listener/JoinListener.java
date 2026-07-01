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
 * @since 30-06-2026 17:45
 */
package me.marioogg.mlogin.spigot.listener;

import me.marioogg.mlogin.core.model.AuthState;
import me.marioogg.mlogin.core.protocol.AuthRequest;
import me.marioogg.mlogin.core.protocol.AuthResponse;
import me.marioogg.mlogin.core.protocol.RequestType;
import me.marioogg.mlogin.spigot.SpigotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class JoinListener implements Listener {

    private static final long TIMEOUT_MILLIS = 5000;

    private final SpigotPlugin plugin;

    public JoinListener(SpigotPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        AuthState cached = plugin.getAuthCache().get(uuid);
        if (cached != null) {
            announce(player, cached);
            return;
        }

        AuthRequest request = AuthRequest.of(RequestType.CHECK_STATE, uuid, player.getName(), "");
        plugin.getRequestManager().sendRequest(request, TIMEOUT_MILLIS).thenAccept(response ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;
                    AuthState state = stateFromResponse(response);
                    plugin.getAuthCache().set(uuid, state);
                    announce(player, state);
                })
        );
    }

    private AuthState stateFromResponse(AuthResponse response) {
        return switch (response.getReason()) {
            case OK -> AuthState.LOGGED_IN;
            case REGISTER_REQUIRED -> AuthState.REGISTER_REQUIRED;
            // Cualquier otra cosa (LOGIN_REQUIRED, TIMEOUT, ERROR...) la tratamos
            // como "necesita login": es la opción más segura, nunca deja pasar de más.
            default -> AuthState.LOGIN_REQUIRED;
        };
    }

    private void announce(Player player, AuthState state) {
        switch (state) {
            case REGISTER_REQUIRED ->
                    player.sendMessage(ChatColor.YELLOW + "Bienvenido. Regístrate con /register <contraseña> <contraseña>");
            case LOGIN_REQUIRED ->
                    player.sendMessage(ChatColor.YELLOW + "Bienvenido de nuevo. Inicia sesión con /login <contraseña>");
            case LOGGED_IN ->
                    player.sendMessage(ChatColor.GREEN + "Sesión iniciada automáticamente (cuenta premium).");
        }
    }
}
