// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/platform/TpsMonitor.java
package com.yourserver.ghostguard.platform;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

public final class TpsMonitor {
    private final JavaPlugin plugin;
    private volatile double tps = 20.0;

    private int taskId = -1;

    // fallback estimator
    private long lastTickNs = 0;

    public TpsMonitor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            double api = tryGetServerTps();
            if (api > 0) {
                tps = Math.min(20.0, api);
                return;
            }
            long now = System.nanoTime();
            if (lastTickNs != 0) {
                double dt = (now - lastTickNs) / 1_000_000_000.0;
                if (dt > 0) {
                    double est = 1.0 / dt;
                    tps = clamp(est, 0.0, 20.0);
                }
            }
            lastTickNs = now;
        }, 1L, 1L);
    }

    public void stop() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        taskId = -1;
    }

    public double getTps() {
        return tps;
    }

    private double tryGetServerTps() {
        try {
            Object server = Bukkit.getServer();
            // Paper has getTPS(); some have getTps()
            Method m;
            try {
                m = server.getClass().getMethod("getTPS");
            } catch (NoSuchMethodException e) {
                m = server.getClass().getMethod("getTps");
            }
            Object res = m.invoke(server);
            if (res instanceof double[]) {
                double[] arr = (double[]) res;
                if (arr.length > 0) return arr[0];
            }
        } catch (Throwable ignored) {}
        return -1;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}