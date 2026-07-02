package me.marioogg.mlogin.spigot.command;

import me.marioogg.mlogin.core.protocol.AuthRequest;
import me.marioogg.mlogin.core.protocol.AuthResponse;
import me.marioogg.mlogin.core.protocol.RequestType;
import me.marioogg.mlogin.spigot.SpigotPlugin;
import me.marioogg.mlogin.spigot.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;

import java.util.UUID;

public class UnregisterCommand {
    private static final SpigotPlugin plugin = SpigotPlugin.getInstance();
    private static final long TIMEOUT_MILLIS = 5000;

    @Command("unregister")
    @Description("Unregister a player's account (admin only).")
    public void unregister(CommandSender sender, String targetName) {
        // permission check: require 'mlogin.admin' or operator
        if (!(sender.hasPermission("mlogin.admin") || sender.isOp())) {
            sender.sendMessage(Locale.NO_PERMISSION);
            return;
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
        UUID targetUuid = offline.getUniqueId();

        // send request to remove account
        AuthRequest request = AuthRequest.of(RequestType.UNREGISTER, targetUuid, targetName, "");

        plugin.getRequestManager().sendRequest(request, TIMEOUT_MILLIS).thenAccept(response ->
                Bukkit.getScheduler().runTask(plugin, () -> handle(sender, targetName, response)));
    }

    private void handle(CommandSender sender, String targetName, AuthResponse response) {
        if (response.isSuccess()) {
            sender.sendMessage(Locale.UNREGISTER_SUCCESS.replace("<player>", targetName));
            return;
        }

        switch (response.getReason()) {
            case NOT_REGISTERED -> sender.sendMessage(Locale.NOT_REGISTERED.replace("<player>", targetName));
            case TIMEOUT -> sender.sendMessage(Locale.TIMEOUT);
            default -> sender.sendMessage(Locale.UNREGISTER_FAIL.replace("<player>", targetName));
        }
    }
}

