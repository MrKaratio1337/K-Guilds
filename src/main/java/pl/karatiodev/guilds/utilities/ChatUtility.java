package pl.karatiodev.guilds.utilities;

import net.md_5.bungee.api.ChatColor;

public class ChatUtility {

    public static String fixColor(String message){
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
