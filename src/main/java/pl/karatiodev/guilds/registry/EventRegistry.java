package pl.karatiodev.guilds.registry;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pl.karatiodev.guilds.Main;
import pl.karatiodev.guilds.listeners.BreakListener;
import pl.karatiodev.guilds.listeners.HeartListener;
import pl.karatiodev.guilds.listeners.PlaceListener;
import pl.karatiodev.guilds.listeners.TerritoryListener;

public class EventRegistry {

    public EventRegistry(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new BreakListener(Main.getInstance().getGuildManager()), plugin);
        Bukkit.getPluginManager().registerEvents(new HeartListener(Main.getInstance().getGuildManager()), plugin);
        Bukkit.getPluginManager().registerEvents(new PlaceListener(Main.getInstance().getGuildManager()), plugin);
        Bukkit.getPluginManager().registerEvents(new TerritoryListener(Main.getInstance().getGuildManager()), plugin);
    }
}
