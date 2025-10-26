package pl.karatiodev.guilds.manager;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.karatiodev.guilds.Main;
import pl.karatiodev.guilds.objects.Guild;
import pl.karatiodev.guilds.utilities.ChatUtility;
import pl.karatiodev.guilds.utilities.ChunkRef;
import pl.karatiodev.guilds.utilities.LocationUtility;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class GuildManager {
    private final Main instance;
    private final FileConfiguration config;
    private final Map<String, Guild> byTag = new HashMap<>();
    private final Map<UUID, String> ownerIndex = new HashMap<>();
    private final Set<UUID> showingBorders = new HashSet<>();

    public GuildManager(Main instance, FileConfiguration config) {
        this.instance = instance;
        this.config = config;
    }

    public Guild createGuild(String tag, String name, UUID owner){
        Guild guild = new Guild(tag, name, owner);
        byTag.put(tag, guild);
        ownerIndex.put(owner, tag);
        return guild;
    }

    public void disbandGuild(String tag){
        Guild guild = byTag.get(tag);
        if(guild != null){
            ownerIndex.remove(guild.getOwner());
            if (guild.getHeartHologram() != null) {
                guild.getHeartHologram().delete();
            }
        }
    }

    public Guild getGuildByTag(String tag){
        return byTag.get(tag);
    }

    public Guild getGuildByOwner(UUID owner){
        String tag = ownerIndex.get(owner);
        return tag == null ? null : byTag.get(tag);
    }

    public Guild getGuildByMember(UUID id){
        for(Guild guild : byTag.values()){
            if(guild.getOwner().equals(id)){
                return guild;
            }
        }
        return null;
    }

    public Optional<Guild> getGuildWithInvite(UUID id){
        return byTag.values().stream().filter(guild -> guild.getOwner().equals(id)).findFirst();
    }

    public Guild getGuildOwningChunk(Chunk chunk){
        for(Guild guild : byTag.values()){
            if(guild.ownsChunk(chunk)){
                return guild;
            }
        }
        return null;
    }

    public void loadAll() {
        if (!config.contains("guilds")) {
            instance.getLogger().info("Brak zapisanych gildii do załadowania.");
            return;
        }

        int loaded = 0;

        for (String key : config.getConfigurationSection("guilds").getKeys(false)) {
            try {
                String path = "guilds." + key;
                String tag = config.getString(path + ".tag");
                String name = config.getString(path + ".name");
                String ownerStr = config.getString(path + ".owner");

                if (tag == null || name == null || ownerStr == null) {
                    instance.getLogger().warning("Pominięto gildie '" + key + "' — brak wymaganych danych (tag/name/owner).");
                    continue;
                }

                UUID owner = UUID.fromString(ownerStr);
                Guild g = new Guild(tag, name, owner);

                g.setCreatedAt(config.getLong(path + ".created", System.currentTimeMillis()));

                List<String> members = config.getStringList(path + ".members");
                for (String m : members) g.getMembers().add(UUID.fromString(m));

                List<String> invites = config.getStringList(path + ".invites");
                for (String i : invites) g.getInvites().add(UUID.fromString(i));

                List<String> chunks = config.getStringList(path + ".chunks");
                for (String s : chunks) g.getChunks().add(ChunkRef.fromString(s));

                if (config.contains(path + ".center.world")) {
                    String world = config.getString(path + ".center.world");
                    int x = config.getInt(path + ".center.x");
                    int z = config.getInt(path + ".center.z");
                    if (world != null) {
                        g.setCenter(new ChunkRef(world, x, z));
                    }
                }

                if (config.contains(path + ".heartLocation")) {
                    try {
                        String serialized = config.getString(path + ".heartLocation");
                        Location heartLoc = LocationUtility.deserialize(serialized);

                        if (heartLoc != null && heartLoc.getWorld() != null) {
                            g.setHeartLocation(heartLoc);

                            g.setHeartHP(config.getDouble(path + ".heartHP", 300.0));
                            g.setLives(config.getInt(path + ".lives", 3));

                            try {
                                org.bukkit.Location holoLoc = heartLoc.clone().add(0, 2, 0);
                                Hologram holo = DHAPI.createHologram(
                                        "guild_" + g.getTag().toLowerCase(),
                                        holoLoc,
                                        Arrays.asList(
                                                "&6❤ &eSerce gildii &6" + g.getName(),
                                                "&7Życia: &e" + g.getLives(),
                                                "&7HP: &c" + (int) g.getHeartHP() + "&7/300"
                                        )
                                );
                                g.setHeartHologram(holo);
                            } catch (Throwable t) {
                                instance.getLogger().warning("Nie udało się odtworzyć hologramu dla gildii: " + g.getTag());
                            }
                        } else {
                            instance.getLogger().warning("Nie można odtworzyć lokacji serca gildii: " + g.getTag());
                        }
                    } catch (Exception e) {
                        instance.getLogger().warning("Błąd odtwarzania serca gildii: " + g.getTag());
                        e.printStackTrace();
                    }
                }

                byTag.put(tag, g);
                ownerIndex.put(owner, tag);
                loaded++;

            } catch (Exception e) {
                instance.getLogger().severe("Błąd podczas ładowania gildii: " + key);
                e.printStackTrace();
            }
        }

        instance.getLogger().info("Załadowano gildii: " + loaded);
    }

    public void saveAll() throws IOException {
        config.set("guilds", null);
        for (Guild g : byTag.values()) {
            String path = "guilds." + g.getTag();
            config.set(path + ".tag", g.getTag());
            config.set(path + ".name", g.getName());
            config.set(path + ".owner", g.getOwner().toString());
            config.set(path + ".deputy", g.getDeputy() == null ? null : g.getDeputy().toString());
            config.set(path + ".created", g.getCreatedAt());
            config.set(path + ".members", g.getMembers().stream().map(UUID::toString).collect(Collectors.toList()));
            config.set(path + ".invites", g.getInvites().stream().map(UUID::toString).collect(Collectors.toList()));
            config.set(path + ".chunks", g.getChunks().stream().map(ChunkRef::toString).collect(Collectors.toList()));
            config.set(path + ".lives", g.getLives());
            config.set(path + ".heartHP", g.getHeartHP());
            config.set(path + ".lastAttack", g.getLastAttackTime());
            config.set(path + ".heartLocation", LocationUtility.serialize(g.getHeartLocation()));
            if (g.getCenter() != null) {
                config.set(path + ".center.world", g.getCenter().getWorld());
                config.set(path + ".center.x", g.getCenter().getX());
                config.set(path + ".center.z", g.getCenter().getZ());
            }
        }
        if (config instanceof FileConfiguration) {
            try {
                config.save(instance.getDataFolder().toPath().resolve("guilds.yml").toFile());
            } catch (IOException e) {
                throw e;
            }
        }
    }

    public void removeGuildCompletely(Guild guild) {
        if (guild == null) return;

        if (guild.getHeartLocation() != null) {
            try {
                Location loc = guild.getHeartLocation();
                loc.getWorld().getNearbyEntities(loc, 2, 2, 2).stream()
                        .filter(e -> e instanceof EnderCrystal)
                        .forEach(Entity::remove);

                loc.getBlock().setType(Material.AIR);
            } catch (Exception e) {
                instance.getLogger().warning("Nie udało się usunąć serca gildii: " + guild.getTag());
                e.printStackTrace();
            }
        }

        try {
            if (guild.getHeartHologram() != null) {
                guild.getHeartHologram().delete();
            }
        } catch (Exception e) {
            instance.getLogger().warning("Nie udało się usunąć hologramu gildii: " + guild.getTag());
            e.printStackTrace();
        }

        byTag.remove(guild.getTag());
        ownerIndex.remove(guild.getOwner());

        try {
            config.set("guilds." + guild.getTag(), null);
            saveAll();
        } catch (Exception e) {
            instance.getLogger().severe("Błąd przy zapisie po usunięciu gildii: " + guild.getTag());
            e.printStackTrace();
        }

        instance.getLogger().info("Usunięto gildie: " + guild.getName() + " [" + guild.getTag() + "]");
        Bukkit.broadcastMessage(ChatUtility.fixColor(
                "&cGildia &6" + guild.getName() + " &czostała rozwiązana!"
        ));
    }

    public void createGuildCommand(Player player, String tag, String name){
        if(getGuildByTag(tag) != null){
            player.sendMessage(ChatUtility.fixColor("&cGildia z tym tagiem już istnieje!"));
            return;
        }

        if(getGuildByOwner(player.getUniqueId()) != null){
            player.sendMessage(ChatUtility.fixColor("&cJuż jesteś właścicielem gildii"));
            return;
        }

        Guild guild = createGuild(tag, name, player.getUniqueId());
        org.bukkit.Location base = player.getLocation().clone();
        base.setY(base.getY() - 10);
        guild.setCenter(new ChunkRef(base.getWorld().getName(), base.getChunk().getX(), base.getChunk().getZ()));

        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Location loc = base.clone().add(x, y, z);
                    if (Math.abs(x) == 2 || Math.abs(y) == 2 || Math.abs(z) == 2)
                        loc.getBlock().setType(Material.STONE_BRICKS);
                    else
                        loc.getBlock().setType(Material.AIR);
                }
            }
        }

        Location crystalLoc = base.clone().add(0, -1, 0);
        crystalLoc.getBlock().setType(Material.BEDROCK);
        crystalLoc.add(0.5, 1, 0.5);
        EnderCrystal crystal = (EnderCrystal)
                base.getWorld().spawnEntity(crystalLoc, EntityType.ENDER_CRYSTAL);
        crystal.setShowingBottom(true);

        Location holoLoc = crystalLoc.clone().add(0, 2.0, 0);
        Hologram holo = DHAPI.createHologram("guild_" + guild.getTag().toLowerCase(), holoLoc,
                Arrays.asList(
                        "&6❤ &eSerce gildii &6" + guild.getName(),
                        "&7Życia: &e3",
                        "&7HP: &c300/300"
                ));
        guild.setHeartHologram(holo);

        Location tp = base.clone().add(0, 11, 0);
        guild.setLives(3);
        guild.setHeartHP(300.0);
        guild.setHeartLocation(crystalLoc);
        guild.updateHologram();
        player.teleport(tp);
        player.sendMessage(ChatUtility.fixColor("&aUtworzono gildie &6" + guild.getName() + " &7[&6" + guild.getTag() + " &7]"));
    }

    public void inviteCommand(Player player, String targetName) {
        Guild guild = getGuildByMember(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatUtility.fixColor("&cNie jesteś w gildii"));
            return;
        }

        if (!guild.isOwnerOrDeputy(player.getUniqueId())) {
            player.sendMessage(ChatUtility.fixColor("&cTylko lider/zastępca może zapraszać"));
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            player.sendMessage(ChatUtility.fixColor("&cGracz offline"));
            return;
        }

        if (getGuildByMember(target.getUniqueId()) != null) {
            player.sendMessage(ChatUtility.fixColor("&cGracz już jest w gildii"));
            return;
        }

        guild.addInvite(target.getUniqueId());
        player.sendMessage(ChatUtility.fixColor("&aZaproszono &6" + target.getName()));
        target.sendMessage(ChatUtility.fixColor("&6Zaproszenie do gildii &e" + guild.getName() + " &7[&6" + guild.getTag() + "&7]&f. Użyj &a/g accept&f aby dołączyć."));
    }


    public void acceptCommand(Player player) {
        Optional<Guild> maybe = getGuildWithInvite(player.getUniqueId());
        if (!maybe.isPresent()) {
            player.sendMessage(ChatUtility.fixColor("&cBrak zaproszeń"));
            return;
        }

        Guild guild = maybe.get();
        guild.addMember(player.getUniqueId());
        guild.removeInvite(player.getUniqueId());
        player.sendMessage(ChatUtility.fixColor("&aDołączyłeś do gildii &6" + guild.getName()));
        guild.broadcast(ChatUtility.fixColor("&6" + player.getName() + " &adołączył do gildii"), Bukkit.getServer());
    }


    public void leaveCommand(Player player) {
        Guild guild = getGuildByMember(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatUtility.fixColor("&cNie jesteś w gildii"));
            return;
        }

        if (guild.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatUtility.fixColor("&cWłaściciel nie może opuścić gildii. Użyj /g disband lub przekaż własność."));
            return;
        }

        guild.removeMember(player.getUniqueId());
        player.sendMessage(ChatUtility.fixColor("&aOpuściłeś gildie &6" + guild.getName()));
    }


    public void infoCommand(Player player, String tag) {
        Guild guild = getGuildByTag(tag);
        if (guild == null) {
            player.sendMessage(ChatUtility.fixColor("&cBrak takiej gildii"));
            return;
        }

        player.sendMessage(ChatUtility.fixColor("&6Gildia: &f" + guild.getName() + " &7[&6" + guild.getTag() + "&7]"));
        player.sendMessage(ChatUtility.fixColor("&6Właściciel: &f" + Bukkit.getOfflinePlayer(guild.getOwner()).getName()));
        player.sendMessage(ChatUtility.fixColor("&6Członków: &f" + guild.getMembers().size()));
        player.sendMessage(ChatUtility.fixColor("&6Utworzona: &f" + Date.from(Instant.ofEpochMilli(guild.getCreatedAt()))));
    }


    public void disbandCommand(Player player) {
        Guild guild = getGuildByOwner(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatUtility.fixColor("&cNie jesteś właścicielem żadnej gildii"));
            return;
        }

        disbandGuild(guild.getTag());
        player.sendMessage(ChatUtility.fixColor("&aRozwiązano gildie &6" + guild.getName()));
    }

    public void setDeputyCommand(Player player, String targetName) {
        Guild guild = getGuildByOwner(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatUtility.fixColor("&cNie jesteś właścicielem gildii"));
            return;
        }

        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            player.sendMessage(ChatUtility.fixColor("&cGracz offline"));
            return;
        }

        if (!guild.getMembers().contains(target.getUniqueId())) {
            player.sendMessage(ChatUtility.fixColor("&cTen gracz nie jest w twojej gildii"));
            return;
        }

        guild.setDeputy(target.getUniqueId());
        player.sendMessage(ChatUtility.fixColor("&aUstawiono &6" + target.getName() + " &ajako zastępcę gildii."));
        target.sendMessage(ChatUtility.fixColor("&aZostałeś zastępcą gildii &6" + guild.getName()));
    }

    public void homeCommand(Player player) {
        Guild guild = getGuildByMember(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatUtility.fixColor("&cNie jesteś w gildii"));
            return;
        }

        if (guild.getCenter() == null) {
            player.sendMessage(ChatUtility.fixColor("&cTwoja gildia nie ma jeszcze ustawionego serca"));
            return;
        }

        World world = Bukkit.getWorld(guild.getCenter().getWorld());
        if (world == null) {
            player.sendMessage(ChatUtility.fixColor("&cŚwiat gildii nie istnieje"));
            return;
        }

        Location tp = new Location(
                world,
                guild.getCenter().getX() * 16 + 8,
                world.getHighestBlockYAt(guild.getCenter().getX() * 16 + 8, guild.getCenter().getZ() * 16 + 8) + 1,
                guild.getCenter().getZ() * 16 + 8
        );

        player.teleport(tp);
        player.sendMessage(ChatUtility.fixColor("&aTeleportowano do gildii &6" + guild.getName()));
    }

    public void toggleBorderCommand(Player player) {
        Guild guild = getGuildByOwner(player.getUniqueId());
        if (guild == null) {
            player.sendMessage(ChatUtility.fixColor("&cTylko lider gildii może użyć tej komendy."));
            return;
        }

        if (guild.getCenter() == null) {
            player.sendMessage(ChatUtility.fixColor("&cTwoja gildia nie ma ustawionego centrum."));
            return;
        }

        UUID id = player.getUniqueId();

        if (showingBorders.contains(id)) {
            showingBorders.remove(id);
            player.sendMessage(ChatUtility.fixColor("&aGranice gildii ukryte."));
            return;
        }

        showingBorders.add(id);
        player.sendMessage(ChatUtility.fixColor("&aPokazuję granice gildii (64x64). Wpisz ponownie &e/g granica &aaby wyłączyć."));

        org.bukkit.World world = Bukkit.getWorld(guild.getCenter().getWorld());
        double cx = guild.getCenter().getX() * 16 + 8;
        double cz = guild.getCenter().getZ() * 16 + 8;
        double half = 32; // pół szerokości 64x64

        new BukkitRunnable() {
            double step = 0;
            @Override
            public void run() {
                if (!showingBorders.contains(id) || !player.isOnline()) {
                    cancel();
                    return;
                }

                double y = player.getLocation().getY() + 1.5;
                for (double x = cx - half; x <= cx + half; x += 1.5) {
                    player.spawnParticle(org.bukkit.Particle.REDSTONE, new org.bukkit.Location(world, x, y, cz - half),
                            1, new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1));
                    player.spawnParticle(org.bukkit.Particle.REDSTONE, new org.bukkit.Location(world, x, y, cz + half),
                            1, new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1));
                }
                for (double z = cz - half; z <= cz + half; z += 1.5) {
                    player.spawnParticle(org.bukkit.Particle.REDSTONE, new org.bukkit.Location(world, cx - half, y, z),
                            1, new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1));
                    player.spawnParticle(org.bukkit.Particle.REDSTONE, new org.bukkit.Location(world, cx + half, y, z),
                            1, new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1));
                }

                step += 1;
            }
        }.runTaskTimer(instance, 0L, 20L); // co sekundę
    }

    public Collection<Guild> getAllGuilds() {
        return byTag.values();
    }
}
