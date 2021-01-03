package net.omni.speedrun.handlers.duos;

import net.omni.speedrun.SpeedRunPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DuoConfig {
    private final File file;
    private final SpeedRunPlugin plugin;
    private FileConfiguration config;

    public DuoConfig(SpeedRunPlugin plugin, String fileName) {
        this.plugin = plugin;

        if (!fileName.endsWith(".yml"))
            fileName = fileName + ".yml";

        this.file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists())
            plugin.saveResource(fileName, false);

        load();
    }

    public void load() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void set(String path, Object object) {
        config.set(path, object);
        save();
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
