// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/util/ColorUtil.java
package com.yourserver.ghostguard.util;

import org.bukkit.ChatColor;

public final class ColorUtil {
    private ColorUtil() {}

    public static String cc(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static String strip(String s) {
        return ChatColor.stripColor(cc(s));
    }
}