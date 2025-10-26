package pl.karatiodev.guilds.utilities;

import java.util.Objects;

public class ChunkRef {
    private final String world;
    private final int x, z;

    public ChunkRef(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public static ChunkRef fromString(String s){
        String[] parts = s.split(":");
        return new ChunkRef(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    @Override
    public String toString() {
        return world + ":" + x + ":" + z;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ChunkRef chunkRef = (ChunkRef) o;
        return x == chunkRef.x && z == chunkRef.z && Objects.equals(world, chunkRef.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
}
