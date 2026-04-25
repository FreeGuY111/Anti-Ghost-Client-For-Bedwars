// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/checks/scaffold/ScaffoldAnalyzer.java
package com.yourserver.ghostguard.checks.scaffold;

import com.yourserver.ghostguard.checks.Check;
import com.yourserver.ghostguard.config.GGConfig;
import com.yourserver.ghostguard.core.EvidenceSnapshot;
import com.yourserver.ghostguard.core.PlayerData;
import com.yourserver.ghostguard.platform.SolidBlockUtil;
import com.yourserver.ghostguard.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class ScaffoldAnalyzer implements Check {

    @Override
    public String name() {
        return "Scaffold";
    }

    public BridgingContext computeContext(GGConfig cfg, Player p, PlayerData pd) {
        // Only heavy checks if:
        // - repeated placements
        // - moving above threshold
        // - void/edge context
        int chain = pd.continuousPlaceChain();
        if (chain < cfg.bridgingMinRepeatedPlaces) {
            return new BridgingContext(false, "placeChain<min");
        }
        if (pd.lastHSpeedBps() < cfg.bridgingMinHorizontalSpeedBlocksPerSec) {
            return new BridgingContext(false, "speed<min");
        }

        Location loc = p.getLocation();
        boolean voidNear = isVoidOrEdgeNearby(loc, cfg.bridgingVoidCheckDepth, cfg.bridgingEdgeRadius);
        if (!voidNear) {
            // allow tiny supporting if pitch down strongly + near air below feet
            boolean pitchDown = (p.getLocation().getPitch() > cfg.bridgingPitchDownSupportDeg);
            if (!pitchDown) return new BridgingContext(false, "no-edge");
            return new BridgingContext(true, "pitchDownSupport");
        }
        return new BridgingContext(true, "edge/void");
    }

    private boolean isVoidOrEdgeNearby(Location loc, int depth, int radius) {
        World w = loc.getWorld();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        // Under-foot air column check
        for (int d = 1; d <= depth; d++) {
            Block b = w.getBlockAt(x, y - d, z);
            if (SolidBlockUtil.isAir(b)) return true;
            if (SolidBlockUtil.isSolid(b)) break;
        }

        // Edge check: nearby blocks under radius are air
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) continue;
                Block below = w.getBlockAt(x + dx, y - 1, z + dz);
                if (SolidBlockUtil.isAir(below)) return true;
            }
        }
        return false;
    }

    public ScaffoldSignals computeSignals(GGConfig cfg, Player p, PlayerData pd, BridgingContext ctx) {
        ScaffoldSignals s = new ScaffoldSignals();
        s.bridging = ctx.bridging;
        s.contextWhy = ctx.why;

        // Cadence metrics
        long[] intervals = pd.placementIntervalsMs().toArray();
        if (intervals.length >= 12) {
            s.intervalVarianceMs = MathUtil.variance(intervals);
            s.narrowBandRatio = MathUtil.narrowBandRatio(intervals, cfg.scaffoldCadenceNarrowBandMs);
        }

        // Speed metrics
        s.speedStdDev = pd.speedStats().stddev();
        s.microPauseRatio = pd.microPauseStats().meanOf01(); // mean of 0/1 pause samples

        // Offset clustering
        long[] offsets = pd.offsetCodes().toArray();
        if (offsets.length >= 10) {
            Map<Long, Integer> freq = new HashMap<>();
            for (long code : offsets) freq.put(code, freq.getOrDefault(code, 0) + 1);
            int[] top = top2(freq);
            int total = offsets.length;
            int top2 = top[0] + top[1];
            s.offsetTop2Ratio = total > 0 ? (top2 / (double) total) : 0.0;

            // Underfoot ratio: approximate by looking for dy=-1-ish & dx/dz small encoded patterns
            // We can’t perfectly decode without packing logic; but we can estimate by counting most frequent codes as "underfoot-ish".
            s.underfootRatio = estimateUnderfootRatio(freq, total);
        }

        // Sneak ratio
        long[] sneaks = pd.sneakSamples().toArray();
        if (sneaks.length >= 20) {
            long sum = 0;
            for (long v : sneaks) sum += v;
            s.sneakRatio = sum / (double) sneaks.length;
        }

        // Rotation smoothness (support)
        s.yawEma = pd.yawDeltaEma();
        s.pitchEma = pd.pitchDeltaEma();

        // Backwards movement / dot
        s.backwardsDotAvg = pd.digPatternTracker().movementDotEma(); // we reuse tracker’s EMA storage
        // Jump rhythm consistency
        s.jumpRhythmConsistency = MathUtil.jumpRhythmConsistency(pd.jumpPlaceDeltaTicks().toArray(), cfg.scaffoldJumpPlaceTickMin, cfg.scaffoldJumpPlaceTickMax);

        // Ghost place stats (attempt vs success)
        long now = System.currentTimeMillis();
        pd.ghostPlaceTracker().prune(now, cfg.scaffoldGhostWindowSeconds);
        s.attemptSuccessRatio = pd.ghostPlaceTracker().attemptSuccessRatio();
        s.invalidCancelBurst = pd.ghostPlaceTracker().invalidCancelled();

        return s;
    }

    private int[] top2(Map<Long, Integer> freq) {
        int a = 0, b = 0;
        for (int v : freq.values()) {
            if (v >= a) { b = a; a = v; }
            else if (v > b) { b = v; }
        }
        return new int[]{a, b};
    }

    private double estimateUnderfootRatio(Map<Long, Integer> freq, int total) {
        if (total <= 0 || freq.isEmpty()) return 0.0;
        // heuristic: if top code dominates, treat as underfoot-ish for evidence value
        int best = 0;
        for (int v : freq.values()) best = Math.max(best, v);
        return best / (double) total;
    }

    public double scoreFromSignals(GGConfig cfg, PlayerData pd, ScaffoldSignals s) {
        // Context filter: outside bridging context -> minimal scoring only
        double contextFactor = s.bridging ? 1.0 : 0.18;

        double score = 0;

        // A) Cadence constancy
        if (s.intervalVarianceMs > 0) {
            boolean lowVar = s.intervalVarianceMs <= cfg.scaffoldCadenceVarianceMsThreshold;
            boolean narrow = s.narrowBandRatio >= cfg.scaffoldCadenceNarrowBandRatio;
            if (lowVar && narrow) {
                score += cfg.scaffoldWeightCadence * contextFactor;
            } else if (narrow) {
                score += (cfg.scaffoldWeightCadence * 0.45) * contextFactor;
            }
        }

        // B) Speed / placement coupling
        boolean stableSpeed = s.speedStdDev <= cfg.scaffoldSpeedStddevThreshold;
        boolean lowPauses = s.microPauseRatio <= cfg.scaffoldSpeedMicroPauseRatioThreshold;
        boolean stableCadence = s.intervalVarianceMs > 0 && s.intervalVarianceMs <= cfg.scaffoldCadenceVarianceMsThreshold;

        if (stableSpeed && lowPauses && stableCadence) {
            score += cfg.scaffoldWeightSpeedCoupling * contextFactor;
        } else if (stableSpeed && stableCadence) {
            score += (cfg.scaffoldWeightSpeedCoupling * 0.55) * contextFactor;
        }

        // C) Offset clustering
        if (s.offsetTop2Ratio >= cfg.scaffoldOffsetTop2Ratio) {
            score += cfg.scaffoldWeightOffsetCluster * contextFactor;
            // bonus if underfoot style is heavy
            if (s.underfootRatio >= cfg.scaffoldOffsetUnderfootRatio) {
                score += (cfg.scaffoldWeightOffsetCluster * 0.25) * contextFactor;
            }
        }

        // D) No crouch (support only)
        if (pd.continuousPlaceChain() >= 12 && s.sneakRatio <= 0.05) {
            score += (cfg.scaffoldWeightNoSneak * 0.6) * contextFactor;
        }

        // E) Rotation smoothness (support only)
        if (pd.continuousPlaceChain() >= 10 && (s.yawEma < 0.90 && s.pitchEma < 0.85)) {
            score += (cfg.scaffoldWeightRotationSmooth) * contextFactor;
        }

        // 3) Backwards / jump scaffold detection
        if (pd.continuousPlaceChain() >= cfg.scaffoldBackwardsMinChainPlaces) {
            boolean movingBackwards = s.backwardsDotAvg <= cfg.scaffoldBackwardsDotThreshold;
            boolean jumpRhythm = s.jumpRhythmConsistency >= cfg.scaffoldJumpRhythmConsistencyRatio;
            if (movingBackwards && jumpRhythm && s.offsetTop2Ratio >= cfg.scaffoldOffsetTop2Ratio) {
                score += cfg.scaffoldWeightBackwardsJump * contextFactor;
            } else if (movingBackwards && s.offsetTop2Ratio >= cfg.scaffoldOffsetTop2Ratio) {
                score += (cfg.scaffoldWeightBackwardsJump * 0.55) * contextFactor;
            }
        }

        // 4) Ghost / invalid placement spam (requires TPS/ping gating handled outside)
        if (cfg.scaffoldGhostPlaceEnabled) {
            boolean ratioBad = s.attemptSuccessRatio >= cfg.scaffoldAttemptSuccessRatioThreshold;
            boolean burstBad = s.invalidCancelBurst >= cfg.scaffoldInvalidCancelBurstThreshold;
            if (s.bridging && (ratioBad || burstBad)) {
                score += cfg.scaffoldWeightGhostPlaceSpam;
            }
        }

        return MathUtil.clamp(score, 0, 100);
    }

    public EvidenceSnapshot toEvidence(GGConfig cfg, PlayerData pd, ScaffoldSignals s, int recentFlags) {
        EvidenceSnapshot ev = new EvidenceSnapshot();
        ev.checkName = "Scaffold";
        ev.recentFlags = recentFlags;
        ev.thresholdFlags = cfg.scaffoldFlagsToBan;

        ev.intervalVarianceMs = s.intervalVarianceMs;
        ev.narrowBandRatio = s.narrowBandRatio;
        ev.speedStdDev = s.speedStdDev;
        ev.microPauseRatio = s.microPauseRatio;

        ev.offsetTop2Ratio = s.offsetTop2Ratio;
        ev.underfootRatio = s.underfootRatio;

        ev.sneakRatio = s.sneakRatio;
        ev.yawEma = s.yawEma;
        ev.pitchEma = s.pitchEma;

        ev.backwardsDotAvg = s.backwardsDotAvg;
        ev.jumpRhythmConsistency = s.jumpRhythmConsistency;

        ev.attemptSuccessRatio = s.attemptSuccessRatio;
        ev.invalidCancelBurst = s.invalidCancelBurst;

        ev.bridgingContext = s.bridging;
        ev.contextText = s.contextWhy;
        return ev;
    }
}