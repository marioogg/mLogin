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
package me.marioogg.mlogin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import me.marioogg.mlogin.api.auth.AuthService;
import me.marioogg.mlogin.api.session.SessionService;
import me.marioogg.mlogin.api.user.UserRepository;
import me.marioogg.mlogin.core.config.ConfigManager;
import me.marioogg.mlogin.core.database.RedisManager;
import me.marioogg.mlogin.core.database.SQLManager;
import me.marioogg.mlogin.core.encryption.EncryptionUtils;
import me.marioogg.mlogin.core.protocol.LoginRateLimiter;
import me.marioogg.mlogin.core.protocol.RedisRequestManager;
import me.marioogg.mlogin.core.util.Log;
import me.marioogg.mlogin.velocity.listener.LoginListener;
import me.marioogg.mlogin.velocity.redis.VelocityRequestHandler;
import me.marioogg.mlogin.velocity.repository.SQLUserRepository;
import me.marioogg.mlogin.velocity.service.InMemorySessionService;
import me.marioogg.mlogin.velocity.service.VelocityAuthService;
import me.marioogg.mlogin.velocity.command.BackupCommand;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Plugin(
        id = "mlogin",
        name = "mLogin",
        version = "1.0-SNAPSHOT",
        authors = {"marioogg"},
        description = "A simple authentication plugin for Velocity."
)
public class VelocityPlugin {
    @Getter
    private static VelocityPlugin instance;
    @Getter
    private final ProxyServer server;
    @Getter
    private final Path dataDirectory;
    @Getter
    private ConfigManager config;
    @Getter
    private RedisManager redis;
    @Getter
    private SQLManager sql;
    @Getter
    private String secretKey;
    @Getter
    private SessionService sessionService;
    @Getter
    private UserRepository userRepository;
    @Getter
    private AuthService authService;

    private RedisRequestManager requestManager;
    private VelocityRequestHandler requestHandler;
    private ExecutorService requestExecutor;
    private LoginRateLimiter rateLimiter;

    private final Logger logger = Log.getLogger();

    @Inject
    public VelocityPlugin(ProxyServer server, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        loadConfig();
        loadRedis();
        loadSql();
        loadServices();
        loadRequestHandler();
        loadListeners();
        loadCommands();
        logger.info("mLogin enabled successfully.");
    }

    private void loadCommands() {
        try {
            server.getCommandManager().register(
                    server.getCommandManager().metaBuilder("mloginbackup").build(),
                    new BackupCommand(this)
            );
        } catch (Exception e) {
            logger.error("Failed to register commands.", e);
        }
    }

    private void loadConfig(){
        try {
            config = new ConfigManager();
            config.load(dataDirectory);

            secretKey = config.getString("security.secret-key", "");
            if (secretKey == null || secretKey.isEmpty()) {
                secretKey = EncryptionUtils.generateSecureKey(32);
                config.set("security.secret-key", secretKey);
                config.save();
                logger.info("There's not a valid secret key in config.conf. mLogin has generated one for you."
                        + "Copy it into every backend configuration or follow https://mlogin.marioogg.dev");
            }
        } catch (Exception e) {
            logger.error("Error loading or saving the config.", e);
            server.shutdown(Component.text("Your mLogin configuration is broken. Server will shutdown for security."));
        }
    }

    private void loadRedis(){
        try {
            redis = new RedisManager(
                    config.getString("redis.host", "localhost"),
                    config.getInt("redis.port", 6379),
                    config.getString("redis.password", "")
            );
        } catch (Exception e){
            logger.error("Error connecting to Redis database.", e);
            server.shutdown(Component.text("Your mLogin configuration is broken. Server will shutdown for security."));
        }
    }

    private void loadSql() {
        try {
            sql = new SQLManager();
            sql.init(
                    config.getString("sql.host", "localhost"),
                    config.getInt("sql.port", 3306),
                    config.getString("sql.database", "mlogin"),
                    config.getString("sql.username", "root"),
                    config.getString("sql.password", "")
            );
        } catch (Exception e) {
            logger.error("Error connecting to SQL database: ", e);
            server.shutdown(Component.text("Your mLogin configuration is broken. Server will shutdown for security."));
        }
    }

    private void loadServices() {
        userRepository = new SQLUserRepository(sql);
        sessionService = new InMemorySessionService();
        authService = new VelocityAuthService(sessionService, userRepository, redis);
    }

    private void loadRequestHandler() {
        requestExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "mlogin-velocity-requests");
            t.setDaemon(true);
            return t;
        });

        rateLimiter = new LoginRateLimiter(
                redis,
                config.getInt("rate-limit.max-attempts", 5),
                config.getInt("rate-limit.attempt-window-seconds", 60),
                config.getInt("rate-limit.base-block-seconds", 30),
                config.getInt("rate-limit.max-block-seconds", 900),
                config.getInt("rate-limit.strike-window-seconds", 3600)
        );

        requestManager = new RedisRequestManager(redis);
        requestHandler = new VelocityRequestHandler(requestManager, userRepository, authService, secretKey, rateLimiter);
        requestHandler.start(requestExecutor);
    }

    private void loadListeners() {
        server.getEventManager().register(this, new LoginListener(authService, sessionService, userRepository));
    }
}