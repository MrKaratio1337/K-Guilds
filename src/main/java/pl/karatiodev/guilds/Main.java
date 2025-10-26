package pl.karatiodev.guilds;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pl.karatiodev.guilds.manager.GuildManager;
import pl.karatiodev.guilds.registry.CommandRegistry;
import pl.karatiodev.guilds.registry.EventRegistry;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    private static Main instance;
    private GuildManager guildManager;
    private File guildFile;
    private FileConfiguration guildConfig;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            this.guildFile = new File(getDataFolder(), "guilds.yml");
            if (!guildFile.exists()) {
                guildFile.getParentFile().mkdirs();
                saveResource("guilds.yml", false);
            }
            this.guildConfig = YamlConfiguration.loadConfiguration(guildFile);

            this.guildManager = new GuildManager(this, guildConfig);
            try {
                this.guildManager.loadAll();
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to load guilds", e);
            }

            new CommandRegistry(this);
            new EventRegistry(this);
            getLogger().info("K-Guilds enabled!");
        }, 20L);
    }

    @Override
    public void onDisable() {
        try {
            this.guildManager.saveAll();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to save guilds", e);
        }
        getLogger().info("K-Guilds disabled!");
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    public static Main getInstance() {
        return instance;
    }
}
