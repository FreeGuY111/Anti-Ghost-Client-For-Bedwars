// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/platform/SolidBlockUtil.java
package com.yourserver.ghostguard.platform;

import org.bukkit.Material;
import org.bukkit.block.Block;

public final class SolidBlockUtil {
    private SolidBlockUtil() {}

    public static boolean isAir(Block b) {
        if (b == null) return true;
        Material m = b.getType();
        if (m == null) return true;
        String n = m.name();
        return n.equals("AIR") || n.equals("CAVE_AIR") || n.equals("VOID_AIR");
    }

    public static boolean isSolid(Block b) {
        if (b == null) return false;
        Material m = b.getType();
        if (m == null) return false;
        try {
            return m.isSolid();
        } catch (Throwable ignored) {
            // 1.8: isSolid exists; but just in case:
            String n = m.name();
            return !isAir(b) && !n.contains("WATER") && !n.contains("LAVA");
        }
    }
}