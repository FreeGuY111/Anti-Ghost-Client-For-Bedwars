// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/listeners/BlockBreakListener.java
package com.yourserver.ghostguard.listeners;

import com.yourserver.ghostguard.GhostGuardPlugin;
import com.yourserver.ghostguard.checks.bednuke.BedNukeAnalyzer;
import com.yourserver.ghostguard.checks.bednuke.BedTargeting;
import com.yourserver.ghostguard.config.GGConfig;
import com.yourserver.ghostguard.core.EvidenceSnapshot;
import com.yourserver.ghostguard.core.MitigationEngine;
import com.yourserver.ghostguard.core.PlayerData;
import com.yourserver.ghostguard.core.PlayerDataManager;
import com.yourserver.ghostguard.integration.LiteBansPunisher;
import com.yourserver.ghostguard.integration.StaffAlertService;
import com.yourserver.ghostguard.platform.MaterialCompat;
import com.yourserver.ghostguard.platform.PingProvider;
import com.yourserver.ghostguard.platform.TpsMonitor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public final class BlockBreakListener implements Listener {

    private final GhostGuardPlugin plugin;
    private final GGConfig cfg;
    private final PlayerDataManager data;
    private final StaffAlertService alerts;
    private final TpsMonitor tps;
    private final PingProvider pingProvider;
    private final LiteBansPunisher punisher;

    private final BedNukeAnalyzer bednuke = new BedNukeAnalyzer();

    public BlockBreakListener(GhostGuardPlugin plugin, GGConfig cfg, PlayerDataManager data,
                              StaffAlertService alerts, TpsMonitor tps, PingProvider pingProvider,
                              LiteBansPunisher punisher) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.data = data;
        this.alerts = alerts;
        this.tps = tps;
        this.pingProvider = pingProvider;
        this.punisher = punisher;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBreak(BlockBreakEvent e) {
        if (!cfg.enableBednuke) return;
        Player p = e.getPlayer();
        Block b = e.getBlock();
        if (!MaterialCompat.isBed(b)) return;

        PlayerData pd = data.get(p);
        long now = System.currentTimeMillis();
        pd.digPatternTracker().recordDigAttempt();

        BedTargeting t = bednuke.validateAttempt(cfg, p, pd, b);
        double add = bednuke.scoreFromTargeting(cfg, t);
        pd.getScoreEngine().addBednuke(add * 0.70);

        boolean flagged = pd.getFlagManager().tryAddBednukeFlag(cfg, now, pd.getScoreEngine().bednuke());
        if (flagged) {
            int recent = pd.getFlagManager().bednukeRecentCount(cfg, now);
            EvidenceSnapshot ev = bednuke.toEvidence(cfg, t, recent, pd.getScoreEngine().bednuke());
            alerts.alertEvidence(p, ev);

            if (cfg.bednukeMitigationEnabled && pd.getScoreEngine().bednuke() >= cfg.bednukeCancelWhenScoreGe) {
                if (!t.valid) e.setCancelled(true);
            }

            MitigationEngine.bednukeKickIfNeeded(cfg, p, pd, alerts, tps, pingProvider);
        }
    }
}