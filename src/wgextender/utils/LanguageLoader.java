package wgextender.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.io.File;

public class LanguageLoader {
    private final Plugin plugin;
    private File file;
    private FileConfiguration config;
    private String lang;

    public LanguageLoader(Plugin plugin, String lang) {
        this.plugin = plugin;
        this.lang = lang;
        this.file = new File(plugin.getDataFolder(), "messages/" + lang + ".yml");
        if (!file.exists()) {
            plugin.saveResource("messages/" + lang + ".yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    @Nullable
    public String getMessage(String key) {
        return this.config.getString(key);
    }

}
