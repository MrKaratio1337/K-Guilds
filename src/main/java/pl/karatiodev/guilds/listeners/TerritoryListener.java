package pl.karatiodev.guilds.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import pl.karatiodev.guilds.Main;
import pl.karatiodev.guilds.manager.GuildManager;
import pl.karatiodev.guilds.objects.Guild;
import pl.karatiodev.guilds.utilities.ChatUtility;
import pl.karatiodev.guilds.utilities.ChunkRef;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TerritoryListener implements Listener {

    private final GuildManager guildManager;
    private final Map<UUID, ChunkRef> lastChunk = new HashMap<>();

    public TerritoryListener(GuildManager guildManager) {
        this.guildManager = guildManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        Player player = event.getPlayer();

        Chunk from = event.getFrom().getChunk();
        Chunk to = event.getTo().getChunk();

        if (from.getX() == to.getX() && from.getZ() == to.getZ() && from.getWorld().equals(to.getWorld())) return;

        ChunkRef toRef = new ChunkRef(to.getWorld().getName(), to.getX(), to.getZ());
        Guild toGuild = guildManager.getGuildOwningChunk(to);
        Guild fromGuild = guildManager.getGuildOwningChunk(from);

        if((fromGuild == null && toGuild == null) || (fromGuild != null && toGuild != null && fromGuild.getTag().equals(toGuild.getTag()))) return;

        lastChunk.put(player.getUniqueId(), toRef);

        if(toGuild != null && (fromGuild == null || !fromGuild.getTag().equals(toGuild.getTag()))) {
            player.sendTitle(ChatUtility.fixColor("&7[" + toGuild.getTag() + "]"), ChatUtility.fixColor("&6Teren gildii &e" + toGuild.getName()), 10, 40, 10);

            if(!toGuild.getMembers().contains(player.getUniqueId())){
                sendIntruderAlert(toGuild, player);
            }
        } else if(fromGuild != null && toGuild == null){
            player.sendTitle(ChatUtility.fixColor("&7[" + toGuild.getTag() + "]"), ChatUtility.fixColor("&6Opuszczasz teren &e" + toGuild.getName()), 10, 40, 10);
        }
    }

    private void sendIntruderAlert(Guild guild, Player intruder){
        String message = ChatUtility.fixColor("&c⚠ " + intruder.getName() + " &7wkroczył na teren Twojej gildii!");
        BossBar bar = Bukkit.createBossBar(message, BarColor.RED, BarStyle.SOLID);

        for(UUID memberUUID : guild.getMembers()){
            Player member = Bukkit.getPlayer(memberUUID);
            if(member != null && member.isOnline()){
                bar.addPlayer(member);
            }
        }

        bar.setVisible(true);

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            bar.removeAll();
        }, 20L * 5);

    }
}
