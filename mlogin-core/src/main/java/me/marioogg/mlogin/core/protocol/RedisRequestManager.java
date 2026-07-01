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
 * @since 30-06-2026 18:01
 */
package me.marioogg.mlogin.core.protocol;

import com.google.gson.Gson;
import me.marioogg.mlogin.core.database.RedisManager;
import me.marioogg.mlogin.core.util.Log;
import redis.clients.jedis.JedisPubSub;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class RedisRequestManager {
    private final RedisManager redis;
    private final Gson gson = new Gson();
    private final Map<UUID, CompletableFuture<AuthResponse>> pending = new ConcurrentHashMap<>();
    private final ScheduledExecutorService timeoutScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "mlogin-redis-timeout");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean responseListenerStarted = false;

    public RedisRequestManager(RedisManager redis) {
        this.redis = redis;
    }
    
    public CompletableFuture<AuthResponse> sendRequest(AuthRequest request, long timeoutMillis) {
        startResponseListenerIfNeeded();

        CompletableFuture<AuthResponse> future = new CompletableFuture<>();
        pending.put(request.getRequestId(), future);

        ScheduledFuture<?> timeoutTask = timeoutScheduler.schedule(() -> {
            CompletableFuture<AuthResponse> waiting = pending.remove(request.getRequestId());
            if (waiting != null) {
                waiting.complete(AuthResponse.fail(request.getRequestId(), ResponseReason.TIMEOUT));
            }
        }, timeoutMillis, TimeUnit.MILLISECONDS);

        future.whenComplete((response, throwable) -> timeoutTask.cancel(false));

        redis.publish(MessageChannel.AUTH_REQUEST, gson.toJson(request));
        return future;
    }
    
    private synchronized void startResponseListenerIfNeeded() {
        if (responseListenerStarted) return;
        responseListenerStarted = true;

        redis.psubscribe(new JedisPubSub() {
            @Override
            public void onPMessage(String pattern, String channel, String message) {
                try {
                    AuthResponse response = gson.fromJson(message, AuthResponse.class);
                    CompletableFuture<AuthResponse> waiting = pending.remove(response.getRequestId());
                    if (waiting != null) {
                        waiting.complete(response);
                    }
                } catch (Exception e) {
                    Log.getLogger().error("Error parsing AuthResponse from Redis.", e);
                }
            }
        }, MessageChannel.AUTH_RESPONSE_PREFIX + "*");
    }
    
    public void listenForRequests(Function<AuthRequest, AuthResponse> handler, ExecutorService executor) {
        redis.subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                executor.submit(() -> handleIncoming(message, handler));
            }
        }, MessageChannel.AUTH_REQUEST);
    }

    private void handleIncoming(String message, Function<AuthRequest, AuthResponse> handler) {
        AuthRequest request;
        try {
            request = gson.fromJson(message, AuthRequest.class);
        } catch (Exception e) {
            Log.getLogger().error("Error parsing AuthRequest from Redis.", e);
            return;
        }

        AuthResponse response;
        try {
            response = handler.apply(request);
        } catch (Exception e) {
            Log.getLogger().error("Error handling AuthRequest {}", request.getRequestId(), e);
            response = AuthResponse.fail(request.getRequestId(), ResponseReason.ERROR);
        }

        redis.publish(MessageChannel.responseChannel(request.getRequestId()), gson.toJson(response));
    }

    public void shutdown() {
        timeoutScheduler.shutdownNow();
    }
}