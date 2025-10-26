package pl.karatiodev.guilds.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtility {

    public static String serialize(Location loc) {
        if (loc == null) return null;
        return loc.getWorld().getName() + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch();
    }

    public static Location deserialize(String s) {
        if (s == null) return null;
        String[] parts = s.split(";");
        return new Location(
                Bukkit.getWorld(parts[0]),
                Double.parseDouble(parts[1]),
                Double.parseDouble(parts[2]),
                Double.parseDouble(parts[3]),
                Float.parseFloat(parts[4]),
                Float.parseFloat(parts[5])
        );
    }
}
