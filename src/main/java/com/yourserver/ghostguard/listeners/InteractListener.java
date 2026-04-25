// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/listeners/InteractListener.java
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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class InteractListener implements Listener {

    private final GhostGuardPlugin plugin;
    private final GGConfig cfg;
    private final PlayerDataManager data;
    private final StaffAlertService alerts;
    private final TpsMonitor tps;
    private final PingProvider pingProvider;
    private final LiteBansPunisher punisher;

    private final BedNukeAnalyzer bednuke = new BedNukeAnalyzer();

    public InteractListener(GhostGuardPlugin plugin, GGConfig cfg, PlayerDataManager data,
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
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        PlayerData pd = data.get(p);

        // Extra attempt tracking without ProtocolLib:
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            pd.ghostPlaceTracker().recordAttempt();
        }

        if (!cfg.enableBednuke) return;

        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block clicked = e.getClickedBlock();
            if (clicked == null) return;
            if (!MaterialCompat.isBed(clicked)) return;

            // treat as bed damage attempt; validate
            long now = System.currentTimeMillis();
            pd.digPatternTracker().recordDigAttempt();

            BedTargeting t = bednuke.validateAttempt(cfg, p, pd, clicked);

            double add = bednuke.scoreFromTargeting(cfg, t);
            pd.getScoreEngine().addBednuke(add * 0.55);

            boolean flagged = pd.getFlagManager().tryAddBednukeFlag(cfg, now, pd.getScoreEngine().bednuke());
            if (flagged) {
                int recent = pd.getFlagManager().bednukeRecentCount(cfg, now);
                EvidenceSnapshot ev = bednuke.toEvidence(cfg, t, recent, pd.getScoreEngine().bednuke());
                alerts.alertEvidence(p, ev);

                // Mitigation: cancel bed attempts when clearly invalid & score high (optional)
                if (cfg.bednukeMitigationEnabled && pd.getScoreEngine().bednuke() >= cfg.bednukeCancelWhenScoreGe) {
                    if (!t.valid) e.setCancelled(true);
                }

                MitigationEngine.bednukeKickIfNeeded(cfg, p, pd, alerts, tps, pingProvider);
            }
        }
    }
}