// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/checks/bednuke/BedNukeAnalyzer.java
package com.yourserver.ghostguard.checks.bednuke;

import com.yourserver.ghostguard.checks.Check;
import com.yourserver.ghostguard.config.GGConfig;
import com.yourserver.ghostguard.core.EvidenceSnapshot;
import com.yourserver.ghostguard.core.PlayerData;
import com.yourserver.ghostguard.platform.MaterialCompat;
import com.yourserver.ghostguard.platform.RaytraceCompat;
import com.yourserver.ghostguard.util.MathUtil;
import com.yourserver.ghostguard.util.VecUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.Deque;

public final class BedNukeAnalyzer implements Check {

    private final Deque<Long> invalidAttemptsMs = new ArrayDeque<>();

    @Override
    public String name() {
        return "BedNuke";
    }

    public BedTargeting validateAttempt(GGConfig cfg, Player p, PlayerData pd, Block b) {
        BedTargeting t = new BedTargeting();
        t.isBed = MaterialCompat.isBed(b);

        Location eye = p.getEyeLocation();
        Location center = b.getLocation().add(0.5, 0.5, 0.5);

        t.distance = eye.distance(center);
        t.hasLOS = RaytraceCompat.hasLineOfSightToBlock(p, b, cfg.bednukeMaxDistance);

        // Aim alignment
        Vector look = eye.getDirection();
        Vector to = center.toVector().subtract(eye.toVector()).normalize();
        double dot = look.dot(to);
        double angle = Math.toDegrees(Math.acos(MathUtil.clamp(dot, -1, 1)));
        t.aimAngleDeg = angle;

        boolean within = t.distance <= cfg.bednukeMaxDistance;
        boolean angleOk = angle <= cfg.bednukeAimAngleDeg;

        t.valid = t.isBed && within && t.hasLOS && angleOk;

        // repeat timing
        long now = System.currentTimeMillis();
        long last = pd.digPatternTracker().lastDigAttemptMs();
        t.repeatMs = (last > 0) ? (now - last) : 9999L;

        if (!t.valid) recordInvalid(cfg, now);
        t.invalidBurst = invalidBurst(cfg, now);

        return t;
    }

    private void recordInvalid(GGConfig cfg, long nowMs) {
        invalidAttemptsMs.addLast(nowMs);
        prune(cfg, nowMs);
    }

    private int invalidBurst(GGConfig cfg, long nowMs) {
        prune(cfg, nowMs);
        return invalidAttemptsMs.size();
    }

    private void prune(GGConfig cfg, long nowMs) {
        long cutoff = nowMs - cfg.bednukeInvalidWindowSeconds * 1000L;
        while (!invalidAttemptsMs.isEmpty() && invalidAttemptsMs.peekFirst() < cutoff) invalidAttemptsMs.removeFirst();
    }

    public double scoreFromTargeting(GGConfig cfg, BedTargeting t) {
        double score = 0;

        // Strong evidence: invalid bed targeting patterns
        if (!t.isBed) return 0; // only consider for bed blocks

        if (!t.hasLOS) score += 40;
        if (t.aimAngleDeg > cfg.bednukeAimAngleDeg) score += 35;
        if (t.distance > cfg.bednukeMaxDistance) score += 25;

        // burst repetition
        if (t.invalidBurst >= cfg.bednukeInvalidBurstThreshold) score += 35;

        // fast repeat (automation attempts)
        if (t.repeatMs <= cfg.bednukeFastRepeatMsThreshold) score += 15;

        return MathUtil.clamp(score, 0, 100);
    }

    public EvidenceSnapshot toEvidence(GGConfig cfg, BedTargeting t, int recentFlags, double score) {
        EvidenceSnapshot ev = new EvidenceSnapshot();
        ev.checkName = "BedNuke";
        ev.recentFlags = recentFlags;
        ev.thresholdFlags = cfg.bednukeFlagsToKick;

        ev.score = score;
        ev.scoreThreshold = cfg.scoreThreshold;

        ev.targetIsBed = t.isBed;
        ev.distance = t.distance;
        ev.hasLOS = t.hasLOS;
        ev.aimAngleDeg = t.aimAngleDeg;
        ev.invalidBurstCount = t.invalidBurst;
        ev.fastRepeatMs = t.repeatMs;

        return ev;
    }
}