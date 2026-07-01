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
 * @since 30-06-2026 00:09
 */
package me.marioogg.mlogin.api.session;

import me.marioogg.mlogin.core.model.AuthSession;

import java.util.Optional;
import java.util.UUID;

public interface SessionService {
    Optional<AuthSession> get(UUID uuid);

    void save(AuthSession session);

    void remove(UUID uuid);
}
