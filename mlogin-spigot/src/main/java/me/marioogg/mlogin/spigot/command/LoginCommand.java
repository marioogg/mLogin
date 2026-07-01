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
 * @since 01-07-2026 12:20
 */
package me.marioogg.mlogin.spigot.command;

import me.marioogg.mlogin.core.encryption.EncryptionUtils;
import me.marioogg.mlogin.core.model.AuthState;
import me.marioogg.mlogin.core.protocol.AuthRequest;
import me.marioogg.mlogin.core.protocol.AuthResponse;
import me.marioogg.mlogin.core.protocol.RequestType;
import me.marioogg.mlogin.spigot.SpigotPlugin;
import me.marioogg.mlogin.spigot.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;

public class LoginCommand {
    private static final long TIMEOUT_MILLIS = 5000;
    private final SpigotPlugin plugin = SpigotPlugin.getInstance();

    @Command("login")
    @Description("Log in with your password.")
    public void login(CommandSender sender, String password){
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Locale.ONLY_PLAYERS_CMD);
            return;
        }

        if (plugin.getAuthCache().isLoggedIn(player.getUniqueId())){
            player.sendMessage(Locale.ALREADY_LOGGED_IN);
        }

        try {
            String encrypted = EncryptionUtils.encrypt(password, plugin.getSecretKey());
            AuthRequest request = AuthRequest.of(RequestType.LOGIN, player.getUniqueId(), player.getName(), encrypted);

            plugin.getRequestManager().sendRequest(request, TIMEOUT_MILLIS).thenAccept(response ->
                    Bukkit.getScheduler().runTask(plugin, () -> handle(player, response)));
        } catch (Exception e) {
            player.sendMessage(Locale.CANT_PROCESS_PASS);
            plugin.getLog().error(("Exception cifrating '"+player.getName()+"' password. "), e);
        }
    }

    private void handle(Player player, AuthResponse response) {
        if (!player.isOnline()) return;

        if (response.isSuccess()) {
            plugin.getAuthCache().set(player.getUniqueId(), AuthState.LOGGED_IN);
            player.sendMessage(Locale.SUCCESSFUL_LOGIN);
            return;
        }

        switch (response.getReason()) {
            case WRONG_PASSWORD -> player.sendMessage(Locale.WRONG_PASSWORD);
            case NOT_REGISTERED -> player.sendMessage(Locale.NOT_REGISTERED);
            case ALREADY_LOGGED_IN -> player.sendMessage(Locale.ALREADY_LOGGED_IN);
            case TIMEOUT -> player.sendMessage(Locale.TIMEOUT);
            default -> player.sendMessage(Locale.ERROR_LOGIN);
        }
    }
}
