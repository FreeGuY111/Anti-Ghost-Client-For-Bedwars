// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/platform/RaytraceCompat.java
package com.yourserver.ghostguard.platform;

import com.yourserver.ghostguard.util.VecUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;

public final class RaytraceCompat {
    private RaytraceCompat() {}

    public static boolean hasLineOfSightToBlock(Player p, Block target, double maxDistance) {
        if (p == null || target == null) return false;
        Location eye = p.getEyeLocation();
        Location center = target.getLocation().add(0.5, 0.5, 0.5);

        // If Bukkit has rayTraceBlocks (1.13+)
        Boolean api = tryBukkitRayTrace(eye, center, maxDistance, p.getWorld());
        if (api != null) return api;

        // Fallback stepper (safe, slower but only used on bed interactions)
        return stepRay(eye, center, maxDistance);
    }

    private static Boolean tryBukkitRayTrace(Location from, Location to, double maxDist, World w) {
        try {
            // World#rayTraceBlocks(Location start, Vector direction, double maxDistance)
            Method m = w.getClass().getMethod("rayTraceBlocks", Location.class, Vector.class, double.class);
            Vector dir = to.toVector().subtract(from.toVector());
            if (dir.length() < 0.001) return true;
            dir.normalize();
            Object res = m.invoke(w, from, dir, maxDist);
            if (res == null) return true; // no hit => clear
            // RayTraceResult#getHitBlock
            Method getHitBlock = res.getClass().getMethod("getHitBlock");
            Object hit = getHitBlock.invoke(res);
            if (hit == null) return true;
            // If it hits something before the target, LOS is blocked
            Block hitBlock = (Block) hit;
            return hitBlock.getLocation().equals(to.getBlock().getLocation());
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean stepRay(Location eye, Location targetCenter, double maxDist) {
        Vector start = eye.toVector();
        Vector end = targetCenter.toVector();
        Vector dir = end.clone().subtract(start);
        double len = dir.length();
        if (len < 0.001) return true;
        if (len > maxDist) return false;

        dir.multiply(1.0 / len);

        double step = 0.2; // blocks
        int steps = (int) Math.ceil(len / step);

        World w = eye.getWorld();
        Vector pos = start.clone();
        for (int i = 0; i < steps; i++) {
            pos.add(dir.clone().multiply(step));
            Location loc = VecUtil.toLocation(w, pos);
            Block b = w.getBlockAt(loc);
            // allow target block itself as final
            if (b.getLocation().equals(targetCenter.getBlock().getLocation())) return true;
            if (SolidBlockUtil.isSolid(b) && !SolidBlockUtil.isAir(b)) {
                return false;
            }
        }
        return true;
    }
}