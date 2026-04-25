// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/integration/StaffAlertService.java
package com.yourserver.ghostguard.integration;

import com.yourserver.ghostguard.config.GGConfig;
import com.yourserver.ghostguard.core.EvidenceSnapshot;
import com.yourserver.ghostguard.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class StaffAlertService {

    private final JavaPlugin plugin;
    private final GGConfig cfg;

    public StaffAlertService(JavaPlugin plugin, GGConfig cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
    }

    public void alertEvidence(Player suspect, EvidenceSnapshot ev) {
        String title = ev.checkName.equals("Scaffold") ? cfg.alertScaffoldTitle : cfg.alertBednukeTitle;
        String header = cfg.alertPrefix + title + " &7(" + ev.recentFlags + "/" + ev.thresholdFlags + ") &f" + suspect.getName();
        sendToStaff(ColorUtil.cc(header));

        if (ev.checkName.equals("Scaffold")) {
            sendToStaff(ColorUtil.cc("&7intervalVar: &f" + fmt(ev.intervalVarianceMs) + "ms  &7narrowRatio: &f" + pct(ev.narrowBandRatio)));
            sendToStaff(ColorUtil.cc("&7speedStd: &f" + fmt(ev.speedStdDev) + "  &7microPause: &f" + pct(ev.microPauseRatio)));
            sendToStaff(ColorUtil.cc("&7offsetTop2: &f" + pct(ev.offsetTop2Ratio) + "  &7underfoot: &f" + pct(ev.underfootRatio)));
            sendToStaff(ColorUtil.cc("&7sneakRatio: &f" + pct(ev.sneakRatio) + "  &7rotEMA(y/p): &f" + fmt(ev.yawEma) + "/" + fmt(ev.pitchEma)));
            sendToStaff(ColorUtil.cc("&7backDotAvg: &f" + fmt(ev.backwardsDotAvg) + "  &7jumpRhythm: &f" + pct(ev.jumpRhythmConsistency)));
            if (ev.attemptSuccessRatio > 0) {
                sendToStaff(ColorUtil.cc("&7attempt:success: &f" + fmt(ev.attemptSuccessRatio) + "  &7invalidCancelBurst: &f" + ev.invalidCancelBurst));
            }
            sendToStaff(ColorUtil.cc("&7context: &f" + (ev.bridgingContext ? "bridging" : "not-bridging") + " &8(" + ev.contextText + ")"));
        } else {
            sendToStaff(ColorUtil.cc("&7targetIsBed: &f" + ev.targetIsBed + "  &7dist: &f" + fmt(ev.distance)));
            sendToStaff(ColorUtil.cc("&7LOS: &f" + ev.hasLOS + "  &7aimAngle: &f" + fmt(ev.aimAngleDeg) + "deg"));
            sendToStaff(ColorUtil.cc("&7invalidBurst: &f" + ev.invalidBurstCount + "  &7fastRepeat: &f" + ev.fastRepeatMs + "ms"));
        }
    }

    public void alertPunished(Player p, String check, String action) {
        sendToStaff(ColorUtil.cc(cfg.alertPrefix + "&c" + check + " &7=> &f" + p.getName() + " &7(" + action + ")"));
    }

    public void alertSafetyHold(Player p, String check, int recent, int threshold, double tps, int ping) {
        sendToStaff(ColorUtil.cc(cfg.alertPrefix + "&e" + check + " &7(" + recent + "/" + threshold + ") &f" + p.getName()
                + " &8[holding: tps=" + fmt(tps) + ", ping=" + ping + "ms]"));
    }

    private void sendToStaff(String msg) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.hasPermission(cfg.staffPermission)) {
                viewer.sendMessage(msg);
            }
        }
        if (cfg.logToConsole) plugin.getLogger().info(ColorUtil.strip(msg));
    }

    private String fmt(double v) { return String.format(java.util.Locale.US, "%.2f", v); }
    private String pct(double v) { return String.format(java.util.Locale.US, "%.0f%%", v * 100.0); }
}