// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/listeners/BlockPlaceListener.java
package com.yourserver.ghostguard.listeners;

import com.yourserver.ghostguard.GhostGuardPlugin;
import com.yourserver.ghostguard.checks.scaffold.BridgingContext;
import com.yourserver.ghostguard.checks.scaffold.ScaffoldAnalyzer;
import com.yourserver.ghostguard.checks.scaffold.ScaffoldSignals;
import com.yourserver.ghostguard.config.GGConfig;
import com.yourserver.ghostguard.core.EvidenceSnapshot;
import com.yourserver.ghostguard.core.MitigationEngine;
import com.yourserver.ghostguard.core.PlayerData;
import com.yourserver.ghostguard.core.PlayerDataManager;
import com.yourserver.ghostguard.integration.LiteBansPunisher;
import com.yourserver.ghostguard.integration.StaffAlertService;
import com.yourserver.ghostguard.platform.PingProvider;
import com.yourserver.ghostguard.platform.TpsMonitor;
import com.yourserver.ghostguard.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public final class BlockPlaceListener implements Listener {

    private final GhostGuardPlugin plugin;
    private final GGConfig cfg;
    private final PlayerDataManager data;
    private final StaffAlertService alerts;
    private final TpsMonitor tps;
    private final PingProvider pingProvider;
    private final LiteBansPunisher punisher;

    private final ScaffoldAnalyzer scaffold = new ScaffoldAnalyzer();

    public BlockPlaceListener(GhostGuardPlugin plugin, GGConfig cfg, PlayerDataManager data,
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        PlayerData pd = data.get(p);
        long now = System.currentTimeMillis();

        // Track attempt (events-only mode)
        pd.ghostPlaceTracker().recordAttempt();

        if (e.isCancelled()) {
            // invalid/cancelled attempt — but only count if TPS/ping ok to avoid false positives
            if (!MitigationEngine.shouldOnlyAlertDueToServer(cfg, tps, pingProvider, p)) {
                pd.ghostPlaceTracker().recordInvalidCancelled();
            }
            return;
        }

        // Success place
        pd.ghostPlaceTracker().recordSuccess();
        pd.recordPlacement(now);

        // Compute relative offset from feet to placed block
        Block b = e.getBlockPlaced();
        Location feet = p.getLocation();
        int dx = b.getX() - feet.getBlockX();
        int dy = b.getY() - feet.getBlockY();
        int dz = b.getZ() - feet.getBlockZ();
        pd.recordPlacementOffset(dx, dy, dz);

        // Jump rhythm: if placement occurs soon after jump start
        pd.recordJumpPlaceDeltaIfAny();

        if (!cfg.enableScaffold) return;

        BridgingContext ctx = scaffold.computeContext(cfg, p, pd);

        ScaffoldSignals signals = scaffold.computeSignals(cfg, p, pd, ctx);

        // Ghost place scoring should be gated by TPS/ping
        if (MitigationEngine.shouldOnlyAlertDueToServer(cfg, tps, pingProvider, p)) {
            signals.attemptSuccessRatio = 0;
            signals.invalidCancelBurst = 0;
        }

        double add = scaffold.scoreFromSignals(cfg, pd, signals);

        // outside context, add is already factor-reduced
        pd.getScoreEngine().addScaffold(add * 0.18);

        // If inside bridging context, apply stronger (still multi-signal)
        if (signals.bridging) {
            pd.getScoreEngine().addScaffold(add * 0.40);
        }

        boolean flagged = pd.getFlagManager().tryAddScaffoldFlag(cfg, now, pd.getScoreEngine().scaffold());
        if (flagged) {
            int recent = pd.getFlagManager().scaffoldRecentCount(cfg, now);
            EvidenceSnapshot ev = scaffold.toEvidence(cfg, pd, signals, recent);
            ev.score = pd.getScoreEngine().scaffold();
            ev.scoreThreshold = cfg.scoreThreshold;

            alerts.alertEvidence(p, ev);
            MitigationEngine.scaffoldPunishIfNeeded(cfg, p, pd, alerts, punisher, tps, pingProvider);
        }
    }
}