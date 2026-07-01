/**
 * Copyright © 2026 marioogg <https://github.com/marioogg>
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
 * @since 26/06/2026 20:01 (CET)
 */
package me.marioogg.mlogin.core.database;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

public class RedisManager {
    private final JedisPool pool;

    private static final String channel = "mlogin:auth";

    public RedisManager(String host, int port, String password) {
        if (password.isEmpty() || password.trim().isEmpty()) {
            this.pool = new JedisPool(host, port);
        } else {
            this.pool = new JedisPool(new JedisPoolConfig(), host, port, 2000, password);
        }
    }

    public void publish(String channel, String message) {
        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, message);
        }
    }

    public void subscribe(JedisPubSub subscriber, String channel) {
        new Thread(() -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.subscribe(subscriber, channel);
            }
        }, "mlogin-redis-subscribe").start();
    }

    public void psubscribe(JedisPubSub subscriber, String pattern) {
        new Thread(() -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.psubscribe(subscriber, pattern);
            }
        }, "mlogin-redis-psubscribe").start();
    }

    public void close() {
        pool.close();
    }
}