package pl.karatiodev.guilds.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import pl.karatiodev.guilds.manager.GuildManager;
import pl.karatiodev.guilds.objects.Guild;
import pl.karatiodev.guilds.utilities.ChatUtility;

public class PlaceListener implements Listener {

    private GuildManager guildManager;

    public PlaceListener(GuildManager guildManager) {
        this.guildManager = guildManager;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        for (Guild guild : guildManager.getAllGuilds()) {
            if (guild.ownsLocation(event.getBlock().getLocation())) {
                Guild playerGuild = guildManager.getGuildByMember(player.getUniqueId());
                if (playerGuild == null || !playerGuild.getTag().equals(guild.getTag())) {
                    event.setCancelled(true);
                    player.sendMessage(ChatUtility.fixColor("&cTen teren należy do gildii &6" + guild.getName()));
                }
                return;
            }
        }
    }
}
