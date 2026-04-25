// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/util/VecUtil.java
package com.yourserver.ghostguard.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public final class VecUtil {
    private VecUtil() {}

    public static Location toLocation(World w, Vector v) {
        return new Location(w, v.getX(), v.getY(), v.getZ());
    }
}