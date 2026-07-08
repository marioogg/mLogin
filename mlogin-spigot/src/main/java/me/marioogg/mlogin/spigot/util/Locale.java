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
 * @since 01-07-2026 00:28
 */
package me.marioogg.mlogin.spigot.util;

import lombok.Getter;
import me.marioogg.mlogin.spigot.SpigotPlugin;
import me.marioogg.mlogin.spigot.config.SpigotConfigManager;
import org.bukkit.configuration.file.FileConfiguration;

@Getter
public class Locale {
    private static FileConfiguration config;

    private static String c(String key) {
        String raw = config.getString("locale." + key);
        return raw != null ? CC.translate(raw) : "&cMissing locale key: " + key;
    }

    public static String ONLY_PLAYERS_CMD;
    public static String ALREADY_LOGGED_IN;
    public static String ALREADY_REGISTERED;
    public static String CANT_PROCESS_PASS;
    public static String ERROR_LOGIN;

    public static String SUCCESSFUL_LOGIN;
    public static String SUCCESSFUL_REGISTER;

    public static String WRONG_PASSWORD;
    public static String NOT_REGISTERED;
    public static String TIMEOUT;
    public static String PASSWORDS_DONT_MATCH;
    public static String PASSWORD_TOO_SHORT;
    public static String PASSWORD_TOO_LONG;

    public static String REGISTER_REQUIRED;
    public static String LOGIN_REQUIRED;
    public static String AUTO_LOGIN;

    public static String CANT_COMMAND;
    public static String CANT_CHAT;
    public static String CHANGE_PASS_SUCCESS;
    public static String CHANGE_PASS_FAIL;

    public static String UNREGISTER_SUCCESS;
    public static String UNREGISTER_FAIL;

    public static String NO_PERMISSION;

    public static void reload() {
        switch (SpigotPlugin.getInstance().getConfig().getString("language", "en").toLowerCase()) {
            case "es" -> config = SpigotConfigManager.getConfig("messages_es.yml");
            case "fr" -> config = SpigotConfigManager.getConfig("messages_fr.yml");
            default -> config = SpigotConfigManager.getConfig("messages_en.yml");
        }
        ONLY_PLAYERS_CMD = c("ONLY-PLAYERS-CMD");
        ALREADY_LOGGED_IN = c("ALREADY-LOGGED-IN");
        ALREADY_REGISTERED = c("ALREADY-REGISTERED");
        CANT_PROCESS_PASS = c("CANT-PROCESS-PASS");
        ERROR_LOGIN = c("ERROR-LOGIN");
        SUCCESSFUL_LOGIN = c("SUCCESSFUL-LOGIN");
        SUCCESSFUL_REGISTER = c("SUCCESSFUL-REGISTER");
        WRONG_PASSWORD = c("WRONG-PASSWORD");
        NOT_REGISTERED = c("NOT-REGISTERED");
        TIMEOUT = c("TIMEOUT");
        PASSWORDS_DONT_MATCH = c("PASSWORDS-DONT-MATCH");
        PASSWORD_TOO_SHORT = c("PASSWORD-TOO-SHORT");
        PASSWORD_TOO_LONG = c("PASSWORD-TOO-LONG");
        REGISTER_REQUIRED = c("REGISTER-REQUIRED");
        LOGIN_REQUIRED = c("LOGIN-REQUIRED");
        AUTO_LOGIN = c("AUTO-LOGIN");
        CANT_COMMAND = c("CANT-COMMAND");
        CANT_CHAT = c("CANT-CHAT");
        CHANGE_PASS_SUCCESS = c("CHANGE-PASS-SUCCESS");
        CHANGE_PASS_FAIL = c("CHANGE-PASS-FAIL");

        UNREGISTER_SUCCESS = c("UNREGISTER-SUCCESS");
        UNREGISTER_FAIL = c("UNREGISTER-FAIL");

        NO_PERMISSION = c("NO-PERMISSION");
    }
}
