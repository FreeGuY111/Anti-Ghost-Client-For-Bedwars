// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/platform/PingProvider.java
package com.yourserver.ghostguard.platform;

import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class PingProvider {

    public int getPing(Player p) {
        // Prefer modern API: p.spigot().getPing()
        try {
            Method spigot = p.getClass().getMethod("spigot");
            Object spigotObj = spigot.invoke(p);
            Method getPing = spigotObj.getClass().getMethod("getPing");
            Object v = getPing.invoke(spigotObj);
            if (v instanceof Integer) return (Integer) v;
        } catch (Throwable ignored) {}

        // Fallback to handle.ping via reflection (works on many versions)
        try {
            Method getHandle = p.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(p);
            Field ping = handle.getClass().getField("ping");
            return ping.getInt(handle);
        } catch (Throwable ignored) {}

        return -1;
    }
}