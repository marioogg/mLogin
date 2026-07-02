package me.marioogg.mlogin.spigot.command;

import me.marioogg.mlogin.core.encryption.EncryptionUtils;
import me.marioogg.mlogin.core.protocol.AuthRequest;
import me.marioogg.mlogin.core.protocol.AuthResponse;
import me.marioogg.mlogin.core.protocol.RequestType;
import me.marioogg.mlogin.spigot.SpigotPlugin;
import me.marioogg.mlogin.spigot.util.Locale;
import me.marioogg.mlogin.spigot.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;

import java.util.UUID;

public class ChangePasswordCommand {
    private static final SpigotPlugin plugin = SpigotPlugin.getInstance();
    private static final long TIMEOUT_MILLIS = 5000;
    private static final int MIN_PASSWORD_LENGTH = plugin.getConfig().getInt("security.min-password-length");

    @Command("changepass")
    @Description("Change your account password.")
    public void changepass(CommandSender sender, String oldPassword, String newPassword, String confirmNewPassword) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Locale.ONLY_PLAYERS_CMD);
            return;
        }

        if (!plugin.getAuthCache().isLoggedIn(player.getUniqueId())) {
            player.sendMessage(Locale.LOGIN_REQUIRED);
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            player.sendMessage(Locale.PASSWORDS_DONT_MATCH);
            return;
        }

        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            player.sendMessage(Locale.PASSWORD_TOO_SHORT.replace("<min>", String.valueOf(MIN_PASSWORD_LENGTH)));
            return;
        }

        try {
            String encOld = EncryptionUtils.encrypt(oldPassword, plugin.getSecretKey());
            String encNew = EncryptionUtils.encrypt(newPassword, plugin.getSecretKey());
            AuthRequest request = AuthRequest.of(RequestType.CHANGE_PASSWORD, player.getUniqueId(), player.getName(), encOld, encNew);

            plugin.getRequestManager().sendRequest(request, TIMEOUT_MILLIS).thenAccept(response ->
                    Bukkit.getScheduler().runTask(plugin, () -> handle(player, response)));
        } catch (Exception e) {
            player.sendMessage(Locale.CANT_PROCESS_PASS);
            plugin.getLog().error(("Exception encrypting '" + player.getName() + "' password."), e);
        }
    }

    private void handle(Player player, AuthResponse response) {
        if (!player.isOnline()) return;

        if (response.isSuccess()) {
            player.sendMessage(Locale.CHANGE_PASS_SUCCESS);
            SoundUtil.playConfirmSound(player);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOW);
            return;
        }

        switch (response.getReason()) {
            case WRONG_PASSWORD -> player.sendMessage(Locale.WRONG_PASSWORD);
            case TIMEOUT -> player.sendMessage(Locale.TIMEOUT);
            default -> player.sendMessage(Locale.CHANGE_PASS_FAIL);
        }
    }
}

