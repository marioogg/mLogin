/**
 * Copyright © 2026 marioogg <https://github.com/marioogg>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * @author marioogg
 * @since 26/06/2026 16:33 (CET)
 */

package me.marioogg.mlogin.core.config;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private ConfigurationNode root;
    private HoconConfigurationLoader loader;

    public void load(Path dataDirectory) throws IOException {
        File folder = dataDirectory.toFile();
        if (!folder.exists()) folder.mkdirs();

        File file = new File(folder, "config.conf");
        if (!file.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/config.conf")) {
                if (in != null) {
                    Files.copy(in, file.toPath());
                } else {
                    file.createNewFile();
                }
            }
        }

        loader = HoconConfigurationLoader.builder().file(file).build();
        root = loader.load();
    }

    public void set(String path, Object value) throws SerializationException {
        root.node((Object[]) path.split("\\.")).set(value);
    }

    public void save() throws IOException {
        if (loader != null && root != null) {
            loader.save(root);
        }
    }

    public String getString(String path, String def) {
        return root.node((Object[]) path.split("\\.")).getString(def);
    }

    public int getInt(String path, int def) {
        return root.node((Object[]) path.split("\\.")).getInt(def);
    }
}
