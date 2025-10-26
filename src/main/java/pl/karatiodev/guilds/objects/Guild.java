package pl.karatiodev.guilds.objects;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import pl.karatiodev.guilds.utilities.ChatUtility;
import pl.karatiodev.guilds.utilities.ChunkRef;

import java.util.*;

public class Guild {
    private final String tag;
    private final String name;
    private UUID owner;
    private UUID deputy;
    private long createdAt;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> invites = new HashSet<>();
    private final Set<ChunkRef> chunks = new HashSet<>();
    private ChunkRef center;
    private int lives = 3;
    private double heartHP = 300.0;
    private long lastAttackTime = 0;
    private Location heartLocation;
    private transient Hologram heartHologram;

    public Guild(String tag, String name, UUID owner){
        this.tag = tag;
        this.name = name;
        this.owner = owner;
        this.createdAt = System.currentTimeMillis();
        this.members.add(owner);
    }

    public String getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public UUID getDeputy() {
        return deputy;
    }

    public void setDeputy(UUID deputy) {
        this.deputy = deputy;
    }

    public boolean isDeputy(UUID uuid){
        return deputy != null && deputy.equals(uuid);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Set<UUID> getInvites() {
        return invites;
    }

    public Set<ChunkRef> getChunks() {
        return chunks;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public double getHeartHP() {
        return heartHP;
    }
    public void setHeartHP(double heartHP) {
        this.heartHP = heartHP;
    }

    public long getLastAttackTime() {
        return lastAttackTime;
    }

    public void setLastAttackTime(long lastAttackTime) {
        this.lastAttackTime = lastAttackTime;
    }

    public Location getHeartLocation() {
        return heartLocation;
    }

    public void setHeartLocation(Location heartLocation) {
        this.heartLocation = heartLocation;
    }

    public boolean canBeAttacked() {
        return System.currentTimeMillis() - lastAttackTime >= 86_400_000L; // 24h
    }

    public void resetHeart() {
        this.heartHP = 300.0;
    }

    public Hologram getHeartHologram() {
        return heartHologram;
    }

    public void setHeartHologram(Hologram heartHologram) {
        this.heartHologram = heartHologram;
    }

    public void updateHologram() {
        if (heartHologram == null || heartLocation == null) return;

        List<String> lines = Arrays.asList(
                "&6❤ &eSerce gildii &6" + name,
                "&7Życia: &e" + lives,
                "&7HP: &c" + (int) heartHP + "&7/300"
        );

        DHAPI.setHologramLines(heartHologram, lines);
    }

    public void addMember(UUID id){
        members.add(id);
    }

    public void removeMember(UUID id){
        members.remove(id);
    }

    public void addInvite(UUID id){
        invites.add(id);
    }

    public void removeInvite(UUID id){
        invites.remove(id);
    }

    public boolean hasInvite(UUID id){
        return invites.contains(id);
    }

    public boolean isOwner(UUID id){
        return owner.equals(id);
    }

    public boolean isOwnerOrDeputy(UUID id){
        return isOwner(id) || isDeputy(id);
    }

    public void broadcast(String message, Server server){
        for(UUID id : members){
            Player player = server.getPlayer(id);
            if(player != null) player.sendMessage(message);
        }
    }

    public boolean ownsChunk(Chunk c){
        ChunkRef ref = new ChunkRef(c.getWorld().getName(), c.getX(), c.getZ());
        return chunks.contains(ref);
    }

    public ChunkRef getCenter(){
        return center;
    }

    public void setCenter(ChunkRef center) {
        this.center = center;
    }

    public boolean ownsLocation(Location loc) {
        if (center == null) return false;
        if (!center.getWorld().equals(loc.getWorld().getName())) return false;
        double dx = Math.abs(loc.getX() - centerX());
        double dz = Math.abs(loc.getZ() - centerZ());
        return dx <= 32 && dz <= 32; // pół z 64x64
    }

    private double centerX() {
        return center.getX() * 16 + 8;
    }

    private double centerZ() {
        return center.getZ() * 16 + 8;
    }
}
