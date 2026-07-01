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
 * @since 30-06-2026 00:10
 */
package me.marioogg.mlogin.api.user;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<String> getPasswordHash(UUID uuid);

    boolean exists(UUID uuid);

    void save(UUID uuid, String username, String passwordHash);
}
