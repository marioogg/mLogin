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
 * @since 30-06-2026 17:55
 */
package me.marioogg.mlogin.spigot;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.common.collect.ImmutableList;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import me.marioogg.mlogin.core.database.RedisManager;
import me.marioogg.mlogin.core.protocol.RedisRequestManager;
import me.marioogg.mlogin.core.util.Log;
import me.marioogg.mlogin.spigot.cache.AuthStateCache;
import me.marioogg.mlogin.spigot.command.LoginCommand;
import me.marioogg.mlogin.spigot.command.RegisterCommand;
import me.marioogg.mlogin.spigot.listener.AuthStateListener;
import me.marioogg.mlogin.spigot.listener.JoinListener;
import me.marioogg.mlogin.spigot.listener.ProtectionListener;
import me.marioogg.mlogin.spigot.listener.QuitListener;
import me.marioogg.mlogin.spigot.util.Locale;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.List;

public class SpigotPlugin extends JavaPlugin {

    @Getter
    public static SpigotPlugin instance;
    @Getter
    private RedisManager redis;
    @Getter
    private RedisRequestManager requestManager;
    @Getter
    private AuthStateCache authCache;
    @Getter
    private String secretKey;
    @Getter
    private final Logger log = Log.getLogger();

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        Locale.reload();
        PacketEvents.getAPI().load();
        if (!loadSecretKey()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        loadRedis();

        authCache = new AuthStateCache();
        requestManager = new RedisRequestManager(redis);
        new AuthStateListener(redis, authCache).start();

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new QuitListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);

        initCmds();
    }

    @Override
    public void onDisable() {
        if (requestManager != null) {
            requestManager.shutdown();
        }
        if (redis != null) {
            redis.close();
        }
    }

    private boolean loadSecretKey() {
        secretKey = getConfig().getString("security.secret-key", "");
        if (secretKey.isEmpty()) {
            List<String> msg = ImmutableList.of(
                    "Your secret key in config.yml is empty.",
                    "Copy your secret key from the first line of config.conf",
                    "in plugins/mLogin in your proxy server."
            );
            msg.forEach(log::error);
            return false;
        }
        return true;
    }

    private void loadRedis() {
        redis = new RedisManager(
                getConfig().getString("redis.host", "localhost"),
                getConfig().getInt("redis.port", 6379),
                getConfig().getString("redis.password", "")
        );
    }

    private void initCmds(){
        Lamp<BukkitCommandActor> lamp = BukkitLamp.builder(this).build();
        lamp.register(new LoginCommand());
        lamp.register(new RegisterCommand());
        lamp.register(new me.marioogg.mlogin.spigot.command.ChangePasswordCommand());
        lamp.register(new me.marioogg.mlogin.spigot.command.UnregisterCommand());
    }
}
