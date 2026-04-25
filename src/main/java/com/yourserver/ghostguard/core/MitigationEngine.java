// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/core/MitigationEngine.java
package com.yourserver.ghostguard.core;

import com.yourserver.ghostguard.config.GGConfig;
import com.yourserver.ghostguard.integration.LiteBansPunisher;
import com.yourserver.ghostguard.integration.StaffAlertService;
import com.yourserver.ghostguard.platform.PingProvider;
import com.yourserver.ghostguard.platform.TpsMonitor;
import org.bukkit.entity.Player;

public final class MitigationEngine {

    private MitigationEngine() {}

    public static boolean canAutoPunish(GGConfig cfg, TpsMonitor tps, PingProvider pingProvider, Player p) {
        if (cfg.tpsProtectionEnabled && tps.getTps() < cfg.tpsMin) return false;
        if (cfg.pingProtectionEnabled && pingProvider.getPing(p) > cfg.pingThresholdMs) return false;
        return true;
    }

    public static boolean shouldOnlyAlertDueToServer(GGConfig cfg, TpsMonitor tps, PingProvider pingProvider, Player p) {
        if (cfg.tpsProtectionEnabled && tps.getTps() < cfg.tpsMin) return true;
        if (cfg.pingProtectionEnabled && pingProvider.getPing(p) > cfg.pingThresholdMs) return true;
        return false;
    }

    public static void scaffoldPunishIfNeeded(GGConfig cfg, Player p, PlayerData pd,
                                              StaffAlertService alerts, LiteBansPunisher punisher,
                                              TpsMonitor tps, PingProvider pingProvider) {
        long now = System.currentTimeMillis();
        int recent = pd.getFlagManager().scaffoldRecentCount(cfg, now);

        if (recent >= cfg.scaffoldFlagsToBan) {
            // safety guard: avoid spam punish
            if (now - pd.getFlagManager().lastPunishMs() < 60_000L) return;

            if (!canAutoPunish(cfg, tps, pingProvider, p)) {
                alerts.alertSafetyHold(p, "Scaffold", recent, cfg.scaffoldFlagsToBan, tps.getTps(), pingProvider.getPing(p));
                return;
            }

            pd.getFlagManager().setLastPunishMs(now);
            punisher.tempBanScaffold(p);
            alerts.alertPunished(p, "Scaffold", "Tempbanned " + cfg.scaffoldBanDuration);
        }
    }

    public static void bednukeKickIfNeeded(GGConfig cfg, Player p, PlayerData pd,
                                          StaffAlertService alerts,
                                          TpsMonitor tps, PingProvider pingProvider) {
        long now = System.currentTimeMillis();
        int recent = pd.getFlagManager().bednukeRecentCount(cfg, now);

        if (recent >= cfg.bednukeFlagsToKick) {
            if (now - pd.getFlagManager().lastPunishMs() < 45_000L) return;

            if (!canAutoPunish(cfg, tps, pingProvider, p)) {
                alerts.alertSafetyHold(p, "BedNuke", recent, cfg.bednukeFlagsToKick, tps.getTps(), pingProvider.getPing(p));
                return;
            }

            pd.getFlagManager().setLastPunishMs(now);
            p.kickPlayer(cfg.bednukeKickReason);
            alerts.alertPunished(p, "BedNuke", "Kicked");
        }
    }
}