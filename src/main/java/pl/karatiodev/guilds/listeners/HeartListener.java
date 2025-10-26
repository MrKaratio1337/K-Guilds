package pl.karatiodev.guilds.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitRunnable;
import pl.karatiodev.guilds.Main;
import pl.karatiodev.guilds.manager.GuildManager;
import pl.karatiodev.guilds.objects.Guild;
import pl.karatiodev.guilds.utilities.ChatUtility;

public class HeartListener implements Listener {

    private final GuildManager guildManager;

    public HeartListener(GuildManager guildManager) {
        this.guildManager = guildManager;
    }

    @EventHandler
    public void onHeartDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof EnderCrystal)) return;
        EnderCrystal crystal = (EnderCrystal) event.getEntity();

        Guild guild = guildManager.getAllGuilds().stream()
                .filter(g -> g.getHeartLocation() != null &&
                        g.getHeartLocation().getWorld().equals(crystal.getWorld()) &&
                        g.getHeartLocation().distance(crystal.getLocation()) < 1.0)
                .findFirst().orElse(null);
        if (guild == null) return;

        event.setCancelled(true);

        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();

        Guild attackerGuild = guildManager.getGuildByMember(attacker.getUniqueId());

        if (attackerGuild != null && attackerGuild.getTag().equals(guild.getTag())) {
            attacker.sendMessage(ChatUtility.fixColor("&cNie możesz atakować własnego serca!"));
            return;
        }

        if (!guild.canBeAttacked()) {
            attacker.sendMessage(ChatUtility.fixColor("&cTo serce jest tymczasowo odporne na atak (24h)."));
            return;
        }

        double dmg = event.getDamage();
        guild.setHeartHP(guild.getHeartHP() - dmg);

        if (guild.getHeartHP() > 0) {
            attacker.sendMessage(ChatUtility.fixColor("&cSerce gildii &6" + guild.getName() +
                    " &cma jeszcze &6" + (int) guild.getHeartHP() + " HP"));
        } else {
            guild.setLives(guild.getLives() - 1);
            guild.setLastAttackTime(System.currentTimeMillis());
            guild.resetHeart();

            guild.broadcast(ChatUtility.fixColor("&cSerce gildii zostało zniszczone! Pozostało &6" +
                    guild.getLives() + " &cżyć."), Bukkit.getServer());
            attacker.sendMessage(ChatUtility.fixColor("&aUdało ci się zniszczyć serce gildii &6" + guild.getName()));

            if (guild.getLives() <= 0) {
                // Usuwamy gildie
                guildManager.removeGuildCompletely(guild);
                Bukkit.broadcastMessage(ChatUtility.fixColor("&4Gildia &6" + guild.getName() + " &czostała zniszczona!"));
                if (guild.getHeartHologram() != null) {
                    guild.getHeartHologram().delete();
                }
            } else {
                // Respawn serca po 1 minucie
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        org.bukkit.Location loc = guild.getHeartLocation();
                        if (loc == null) return;
                        EnderCrystal newCrystal = (EnderCrystal)
                                loc.getWorld().spawnEntity(loc, EntityType.ENDER_CRYSTAL);
                        newCrystal.setShowingBottom(true);
                    }
                }.runTaskLater(Main.getInstance(), 20 * 60);
            }
        }
    }

    // Zapobieganie eksplozji
    @EventHandler
    public void onHeartExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof EnderCrystal) {
            event.setCancelled(true);
        }
    }
}
