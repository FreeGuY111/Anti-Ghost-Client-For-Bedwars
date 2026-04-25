// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/util/LocationUtil.java
package com.yourserver.ghostguard.util;

import org.bukkit.Location;

public final class LocationUtil {
    private LocationUtil() {}

    public static int blockX(Location l) { return l.getBlockX(); }
    public static int blockY(Location l) { return l.getBlockY(); }
    public static int blockZ(Location l) { return l.getBlockZ(); }
}