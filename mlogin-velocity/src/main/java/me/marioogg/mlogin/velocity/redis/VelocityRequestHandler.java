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
 * @since 30-06-2026 17:20
 */
package me.marioogg.mlogin.velocity.redis;

import me.marioogg.mlogin.api.auth.AuthService;
import me.marioogg.mlogin.api.user.UserRepository;
import me.marioogg.mlogin.core.encryption.EncryptionUtils;
import me.marioogg.mlogin.core.model.AuthState;
import me.marioogg.mlogin.core.protocol.*;
import me.marioogg.mlogin.core.util.Log;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class VelocityRequestHandler {

    private final RedisRequestManager requestManager;
    private final UserRepository users;
    private final AuthService authService;
    private final String secretKey;
    private final LoginRateLimiter rateLimiter;

    public VelocityRequestHandler(RedisRequestManager requestManager, UserRepository users,
                                  AuthService authService, String secretKey, LoginRateLimiter rateLimiter) {
        this.requestManager = requestManager;
        this.users = users;
        this.authService = authService;
        this.secretKey = secretKey;
        this.rateLimiter = rateLimiter;
    }

    public void start(ExecutorService executor) {
        requestManager.listenForRequests(this::handle, executor);
    }

    private AuthResponse handle(AuthRequest request) {
        UUID uuid = request.getPlayerUuid();
        try {
            return switch (request.getType()) {
                case CHECK_STATE -> checkState(request, uuid);
                case LOGIN -> login(request, uuid);
                case REGISTER -> register(request, uuid);
                case CHANGE_PASSWORD -> changePassword(request, uuid);
                case UNREGISTER -> unregister(request, uuid);
            };

        } catch (Exception e) {
            Log.getLogger().error("Error processing the request " + request.getRequestId() + " (" + request.getType() + ")", e);
            return AuthResponse.fail(request.getRequestId(), ResponseReason.ERROR);
        }
    }

    private AuthResponse checkState(AuthRequest request, UUID uuid) {
        AuthState state = authService.getState(uuid);
        ResponseReason reason = switch (state) {
            case LOGGED_IN -> ResponseReason.OK;
            case LOGIN_REQUIRED -> ResponseReason.LOGIN_REQUIRED;
            case REGISTER_REQUIRED -> ResponseReason.REGISTER_REQUIRED;
        };
        return AuthResponse.of(request.getRequestId(), state == AuthState.LOGGED_IN, reason);
    }

    private AuthResponse login(AuthRequest request, UUID uuid) throws Exception {
        if (authService.isAuthenticated(uuid)) {
            return AuthResponse.fail(request.getRequestId(), ResponseReason.ALREADY_LOGGED_IN);
        }

        String ip = request.getIp();
        long blockedSeconds = Math.max(
                rateLimiter.getBlockedSecondsRemaining(uuid),
                rateLimiter.getBlockedSecondsRemaining(ip)
        );
        if (blockedSeconds > 0) {
            return AuthResponse.rateLimited(request.getRequestId(), blockedSeconds);
        }

        Optional<String> hash = users.getPasswordHash(uuid);
        if (hash.isEmpty()) {
            return AuthResponse.fail(request.getRequestId(), ResponseReason.NOT_REGISTERED);
        }

        String plainPassword = EncryptionUtils.decrypt(request.getEncryptedPassword(), secretKey);
        if (!EncryptionUtils.verifyPassword(plainPassword, hash.get())) {
            rateLimiter.registerFailure(uuid);
            rateLimiter.registerFailure(ip);
            return AuthResponse.fail(request.getRequestId(), ResponseReason.WRONG_PASSWORD);
        }

        rateLimiter.registerSuccess(uuid);
        rateLimiter.registerSuccess(ip);
        authService.setState(uuid, AuthState.LOGGED_IN);
        return AuthResponse.ok(request.getRequestId());
    }

    private AuthResponse register(AuthRequest request, UUID uuid) throws Exception {
        if (authService.isAuthenticated(uuid)) {
            return AuthResponse.fail(request.getRequestId(), ResponseReason.ALREADY_LOGGED_IN);
        }

        if (users.exists(uuid)) {
            return AuthResponse.fail(request.getRequestId(), ResponseReason.ALREADY_REGISTERED);
        }

        String plainPassword = EncryptionUtils.decrypt(request.getEncryptedPassword(), secretKey);
        String hash = EncryptionUtils.hashPassword(plainPassword);
        users.save(uuid, request.getUsername(), hash);
        authService.setState(uuid, AuthState.LOGGED_IN);
        return AuthResponse.ok(request.getRequestId());
    }

    private AuthResponse changePassword(AuthRequest request, UUID uuid) throws Exception {
        if (!users.exists(uuid)) {
            return AuthResponse.fail(request.getRequestId(), ResponseReason.NOT_REGISTERED);
        }

        // encryptedPassword = old password encrypted, extra = new password encrypted
        String oldPlain = EncryptionUtils.decrypt(request.getEncryptedPassword(), secretKey);
        String newPlain = request.getExtra() != null ? EncryptionUtils.decrypt(request.getExtra(), secretKey) : null;

        if (newPlain == null || newPlain.isEmpty()) {
            return AuthResponse.fail(request.getRequestId(), ResponseReason.ERROR);
        }

        Optional<String> hash = users.getPasswordHash(uuid);
        if (hash.isEmpty()) {
            return AuthResponse.fail(request.getRequestId(), ResponseReason.NOT_REGISTERED);
        }

        if (!EncryptionUtils.verifyPassword(oldPlain, hash.get())) {
            rateLimiter.registerFailure(uuid);
            return AuthResponse.fail(request.getRequestId(), ResponseReason.WRONG_PASSWORD);
        }

        String newHash = EncryptionUtils.hashPassword(newPlain);
        users.save(uuid, request.getUsername(), newHash);
        rateLimiter.registerSuccess(uuid);
        return AuthResponse.ok(request.getRequestId());
    }

    private AuthResponse unregister(AuthRequest request, UUID uuid) throws Exception {
        if (!users.exists(uuid)) {
            return AuthResponse.fail(request.getRequestId(), ResponseReason.NOT_REGISTERED);
        }

        users.delete(uuid);
        authService.setState(uuid, AuthState.REGISTER_REQUIRED);
        return AuthResponse.ok(request.getRequestId());
    }
}