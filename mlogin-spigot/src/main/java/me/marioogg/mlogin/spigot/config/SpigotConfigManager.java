package me.marioogg.mlogin.spigot.config;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class SpigotConfigManager {
    private static final Map<String, FileConfiguration> CONFIGS = new HashMap<>();
    private static final Map<String, File> FILES = new HashMap<>();
    public static void loadConfigurations(JavaPlugin plugin, List<String> names)
    {
        for (String name : names) {
            loadConfig(plugin, normalize(name));
        }
    }
    private static void loadConfig(JavaPlugin plugin, String fileName)
    {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        CONFIGS.put(fileName, config);
        FILES.put(fileName, file);
    }
    public static void reloadConfig(JavaPlugin plugin, String name)
    {
        String fileName = normalize(name);
        File file = FILES.get(fileName);
        if (file == null) {
            return;
        }
        CONFIGS.put(fileName, YamlConfiguration.loadConfiguration(file));
    }
    public static void saveConfig(String name)
    {
        String fileName = normalize(name);
        FileConfiguration config = CONFIGS.get(fileName);
        File file = FILES.get(fileName);
        if (config == null || file == null) {
            return;
        }
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static FileConfiguration getConfig(String name)
    {
        return CONFIGS.get(normalize(name));
    }
    public static Map<String, FileConfiguration> getConfigs()
    {
        return Collections.unmodifiableMap(CONFIGS);
    }
    private static String normalize(String name)
    {
        return name.endsWith(".yml") ? name : name + ".yml";
    }
}