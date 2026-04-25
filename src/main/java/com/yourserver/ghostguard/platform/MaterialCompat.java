// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/platform/MaterialCompat.java
package com.yourserver.ghostguard.platform;

import org.bukkit.Material;
import org.bukkit.block.Block;

public final class MaterialCompat {
    private MaterialCompat() {}

    public static boolean isBed(Block b) {
        if (b == null) return false;
        Material t = b.getType();
        if (t == null) return false;
        String name = t.name();
        // Legacy: BED_BLOCK (1.8)
        if ("BED_BLOCK".equals(name) || "BED".equals(name)) return true;
        // Modern: RED_BED, BLUE_BED, etc.
        return name.endsWith("_BED");
    }
}