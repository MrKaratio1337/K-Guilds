package pl.karatiodev.guilds.registry;

import org.bukkit.plugin.java.JavaPlugin;
import pl.karatiodev.guilds.Main;
import pl.karatiodev.guilds.commands.GuildCommand;

public class CommandRegistry {

    public CommandRegistry(JavaPlugin plugin){
        plugin.getCommand("g").setExecutor(new GuildCommand(Main.getInstance().getGuildManager()));
    }
}
