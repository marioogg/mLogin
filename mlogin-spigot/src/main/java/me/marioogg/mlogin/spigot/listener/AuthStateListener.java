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
 * @since 30-06-2026 17:42
 */
package me.marioogg.mlogin.spigot.listener;

import com.google.gson.Gson;
import me.marioogg.mlogin.core.database.RedisManager;
import me.marioogg.mlogin.core.model.AuthMessage;
import me.marioogg.mlogin.core.protocol.MessageChannel;
import me.marioogg.mlogin.core.util.Log;
import me.marioogg.mlogin.spigot.cache.AuthStateCache;
import redis.clients.jedis.JedisPubSub;

public class AuthStateListener {

    private final RedisManager redis;
    private final AuthStateCache cache;
    private final Gson gson = new Gson();

    public AuthStateListener(RedisManager redis, AuthStateCache cache) {
        this.redis = redis;
        this.cache = cache;
    }

    public void start() {
        redis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                try {
                    AuthMessage authMessage = gson.fromJson(message, AuthMessage.class);
                    cache.set(authMessage.getUuid(), authMessage.getState());
                } catch (Exception e) {
                    Log.getLogger().error("Error parsing AuthMessage from Redis.", e);
                }
            }
        }, MessageChannel.AUTH_STATE);
    }
}
