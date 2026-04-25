// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/integration/LiteBansPunisher.java
package com.yourserver.ghostguard.integration;

import com.yourserver.ghostguard.config.GGConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class LiteBansPunisher {

    private final JavaPlugin plugin;
    private final GGConfig cfg;

    public LiteBansPunisher(JavaPlugin plugin, GGConfig cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
    }

    public void tempBanScaffold(Player p) {
        String cmd = "litebans:tempban " + p.getName() + " " + cfg.scaffoldBanDuration + " \"" + cfg.scaffoldBanReason + "\"";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        if (cfg.logToConsole) plugin.getLogger().info("[GhostGuard] Executed: " + cmd);
    }
}