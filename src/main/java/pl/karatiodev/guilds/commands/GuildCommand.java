package pl.karatiodev.guilds.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.karatiodev.guilds.manager.GuildManager;
import pl.karatiodev.guilds.utilities.ChatUtility;

import java.util.Locale;

public class GuildCommand implements CommandExecutor {

    private GuildManager guildManager;

    public GuildCommand(GuildManager guildManager) {
        this.guildManager = guildManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is for players only.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage(ChatUtility.fixColor("&6Gildie &7- &fDostępne: create, invite, accept, leave, info, disband, deputy, home, border"));
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "create":
                if (!player.hasPermission("guilds.create")) {
                    player.sendMessage(ChatUtility.fixColor("&cBrak permisji"));
                    return true;
                }

                if (args.length < 3) {
                    player.sendMessage(ChatUtility.fixColor("&cUżycie: /g create <TAG> <Nazwa>"));
                    return true;
                }

                String tag = args[1].toUpperCase(Locale.ROOT);
                String name = joinArgs(args, 2);
                guildManager.createGuildCommand(player, tag, name);
                break;
            case "invite":
                if (!player.hasPermission("guilds.invite")) {
                    player.sendMessage(ChatUtility.fixColor("&cBrak permisji"));
                    return true;
                }

                if (args.length != 2) {
                    player.sendMessage(ChatUtility.fixColor("&cUżycie: /g invite <gracz>"));
                    return true;
                }

                guildManager.inviteCommand(player, args[1]);
                break;
            case "deputy":
                if (!player.hasPermission("guilds.deputy")) {
                    player.sendMessage(ChatUtility.fixColor("&cBrak permisji"));
                    return true;
                }
                if (args.length != 2) {
                    player.sendMessage(ChatUtility.fixColor("&cUżycie: /g deputy <gracz>"));
                    return true;
                }
                guildManager.setDeputyCommand(player, args[1]);
                break;
            case "accept":
                guildManager.acceptCommand(player);
                break;
            case "leave":
                guildManager.leaveCommand(player);
                break;
            case "info":
                if (args.length != 2) {
                    player.sendMessage(ChatUtility.fixColor("&cUżycie: /g info <tag>"));
                    return true;
                }

                guildManager.infoCommand(player, args[1].toUpperCase(Locale.ROOT));
                break;
            case "disband":
                guildManager.disbandCommand(player);
                break;
            case "home":
                guildManager.homeCommand(player);
                break;
            case "border":
                guildManager.toggleBorderCommand(player);
                break;
            default:
                player.sendMessage(ChatUtility.fixColor("&cNieznana podkomenda"));
                break;
        }
        return true;
    }

    private String joinArgs(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) sb.append(' ');
            sb.append(args[i]);
        }
        return sb.toString();
    }
}
