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
 * @since 30-06-2026 12:00
 */
package me.marioogg.mlogin.velocity.repository;

import me.marioogg.mlogin.api.user.UserRepository;
import me.marioogg.mlogin.core.database.SQLManager;
import me.marioogg.mlogin.core.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class SQLUserRepository implements UserRepository {

    private final SQLManager sql;

    public SQLUserRepository(SQLManager sql) {
        this.sql = sql;
    }

    @Override
    public Optional<String> getPasswordHash(UUID uuid) {
        String query = "SELECT password FROM mlogin_users WHERE uuid = ?";

        try (Connection con = sql.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString("password"));
                }
            }
        } catch (SQLException e) {
            Log.getLogger().error("Error fetching password hash for " + uuid, e);
        }

        return Optional.empty();
    }

    @Override
    public boolean exists(UUID uuid) {
        String query = "SELECT 1 FROM mlogin_users WHERE uuid = ?";

        try (Connection con = sql.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Log.getLogger().error("Error checking if " + uuid + " exists.", e);
            return true;
        }
    }
    

    @Override
    public void save(UUID uuid, String username, String passwordHash) {
        String query = "INSERT INTO mlogin_users (uuid, username, password) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE password = VALUES(password), username = VALUES(username)";

        try (Connection con = sql.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, username);
            stmt.setString(3, passwordHash);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Log.getLogger().error("Error saving user " + uuid, e);
        }
    }

    @Override
    public void delete(UUID uuid) {
        String query = "DELETE FROM mlogin_users WHERE uuid = ?";

        try (Connection con = sql.getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            Log.getLogger().error("Error deleting user " + uuid, e);
        }
    }
}