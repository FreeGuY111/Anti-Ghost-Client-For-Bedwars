// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/GhostGuardPlugin.java
package com.yourserver.ghostguard;

import com.yourserver.ghostguard.config.GGConfig;
import com.yourserver.ghostguard.core.PlayerDataManager;
import com.yourserver.ghostguard.integration.LiteBansPunisher;
import com.yourserver.ghostguard.integration.ProtocolLibAdapter;
import com.yourserver.ghostguard.integration.StaffAlertService;
import com.yourserver.ghostguard.listeners.*;
import com.yourserver.ghostguard.platform.PingProvider;
import com.yourserver.ghostguard.platform.TpsMonitor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class GhostGuardPlugin extends JavaPlugin {

    private GGConfig ggConfig;
    private PlayerDataManager dataManager;

    private TpsMonitor tpsMonitor;
    private PingProvider pingProvider;

    private StaffAlertService alerts;
    private LiteBansPunisher punisher;

    private ProtocolLibAdapter protocolLibAdapter;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.ggConfig = new GGConfig(this);
        this.dataManager = new PlayerDataManager();

        this.tpsMonitor = new TpsMonitor(this);
        this.tpsMonitor.start();

        this.pingProvider = new PingProvider();

        this.alerts = new StaffAlertService(this, ggConfig);
        this.punisher = new LiteBansPunisher(this, ggConfig);

        // Optional ProtocolLib hook
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            try {
                this.protocolLibAdapter = new ProtocolLibAdapter(this, ggConfig, dataManager, alerts, tpsMonitor, pingProvider);
                this.protocolLibAdapter.start();
                getLogger().info("ProtocolLib detected: enhanced placement attempt tracking enabled.");
            } catch (Throwable t) {
                this.protocolLibAdapter = null;
                getLogger().warning("ProtocolLib hook failed, continuing without it: " + t.getMessage());
            }
        } else {
            getLogger().info("ProtocolLib not found: running in Bukkit-events-only mode.");
        }

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new JoinQuitListener(dataManager), this);
        Bukkit.getPluginManager().registerEvents(new MovementListener(this, ggConfig, dataManager, alerts, tpsMonitor, pingProvider, punisher), this);
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(this, ggConfig, dataManager, alerts, tpsMonitor, pingProvider, punisher), this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(this, ggConfig, dataManager, alerts, tpsMonitor, pingProvider, punisher), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this, ggConfig, dataManager, alerts, tpsMonitor, pingProvider, punisher), this);

        // Score decay task
        Bukkit.getScheduler().runTaskTimer(this, () -> dataManager.forEachOnline(pd -> pd.getScoreEngine().decay(ggConfig)), 20L, 20L);

        getLogger().info("BedwarsGhostGuard enabled.");
    }

    @Override
    public void onDisable() {
        if (protocolLibAdapter != null) {
            try { protocolLibAdapter.stop(); } catch (Throwable ignored) {}
        }
        if (tpsMonitor != null) {
            try { tpsMonitor.stop(); } catch (Throwable ignored) {}
        }
        getLogger().info("BedwarsGhostGuard disabled.");
    }

    public GGConfig cfg() { return ggConfig; }
}