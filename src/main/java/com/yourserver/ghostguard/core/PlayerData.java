// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/core/PlayerData.java
package com.yourserver.ghostguard.core;

import com.yourserver.ghostguard.checks.bednuke.DigPatternTracker;
import com.yourserver.ghostguard.checks.scaffold.GhostPlaceTracker;
import com.yourserver.ghostguard.util.TimeUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class PlayerData {
    private final UUID uuid;

    private final ScoreEngine scoreEngine = new ScoreEngine();
    private final FlagManager flagManager = new FlagManager();

    // Movement tracking
    private Location lastLoc;
    private long lastMoveMs;
    private double lastHSpeedBps; // blocks/sec
    private boolean lastOnGround;
    private int tickCounter; // approx server ticks for rhythm checks

    // Rotation
    private float lastYaw;
    private float lastPitch;
    private float yawDeltaEma;
    private float pitchDeltaEma;

    // Scaffold trackers
    private final RollingWindow placementTimesMs = new RollingWindow(80);
    private final RollingWindow placementIntervalsMs = new RollingWindow(80);
    private final RollingStats speedStats = new RollingStats(60);
    private final RollingStats microPauseStats = new RollingStats(60);
    private final RollingWindow offsetCodes = new RollingWindow(60);
    private final RollingWindow sneakSamples = new RollingWindow(120);

    private final RollingWindow jumpStartTicks = new RollingWindow(50);
    private final RollingWindow jumpPlaceDeltaTicks = new RollingWindow(80);

    private final GhostPlaceTracker ghostPlaceTracker = new GhostPlaceTracker();
    private final DigPatternTracker digPatternTracker = new DigPatternTracker();

    // counters
    private int continuousPlaceChain; // increments on rapid placements
    private long lastPlaceMs;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID uuid() { return uuid; }

    public ScoreEngine getScoreEngine() { return scoreEngine; }
    public FlagManager getFlagManager() { return flagManager; }

    public RollingWindow placementTimesMs() { return placementTimesMs; }
    public RollingWindow placementIntervalsMs() { return placementIntervalsMs; }
    public RollingStats speedStats() { return speedStats; }
    public RollingStats microPauseStats() { return microPauseStats; }
    public RollingWindow offsetCodes() { return offsetCodes; }
    public RollingWindow sneakSamples() { return sneakSamples; }

    public RollingWindow jumpStartTicks() { return jumpStartTicks; }
    public RollingWindow jumpPlaceDeltaTicks() { return jumpPlaceDeltaTicks; }

    public GhostPlaceTracker ghostPlaceTracker() { return ghostPlaceTracker; }
    public DigPatternTracker digPatternTracker() { return digPatternTracker; }

    public Location lastLoc() { return lastLoc; }
    public void setLastLoc(Location lastLoc) { this.lastLoc = lastLoc; }

    public long lastMoveMs() { return lastMoveMs; }
    public void setLastMoveMs(long lastMoveMs) { this.lastMoveMs = lastMoveMs; }

    public double lastHSpeedBps() { return lastHSpeedBps; }
    public void setLastHSpeedBps(double v) { this.lastHSpeedBps = v; }

    public boolean lastOnGround() { return lastOnGround; }
    public void setLastOnGround(boolean lastOnGround) { this.lastOnGround = lastOnGround; }

    public int tickCounter() { return tickCounter; }
    public void tick() { tickCounter++; }

    public float lastYaw() { return lastYaw; }
    public float lastPitch() { return lastPitch; }

    public float yawDeltaEma() { return yawDeltaEma; }
    public float pitchDeltaEma() { return pitchDeltaEma; }

    public void updateRotation(Player p) {
        float yaw = p.getLocation().getYaw();
        float pitch = p.getLocation().getPitch();

        float dyaw = Math.abs(wrapAngle(yaw - lastYaw));
        float dpitch = Math.abs(pitch - lastPitch);

        // EMA smoothing to get "rotation stability"
        yawDeltaEma = ema(yawDeltaEma, dyaw, 0.12f);
        pitchDeltaEma = ema(pitchDeltaEma, dpitch, 0.12f);

        lastYaw = yaw;
        lastPitch = pitch;
    }

    private float ema(float prev, float v, float alpha) {
        return prev + alpha * (v - prev);
    }

    private float wrapAngle(float angle) {
        angle %= 360.0f;
        if (angle >= 180.0f) angle -= 360.0f;
        if (angle < -180.0f) angle += 360.0f;
        return angle;
    }

    public void recordMoveSample(double hSpeedBps) {
        speedStats.add(hSpeedBps);
        microPauseStats.add(hSpeedBps < 0.15 ? 1.0 : 0.0);
        lastHSpeedBps = hSpeedBps;
    }

    public void recordSneakSample(boolean sneaking) {
        sneakSamples.add(sneaking ? 1 : 0);
    }

    public void recordJumpStart() {
        jumpStartTicks.add(tickCounter);
    }

    public void recordPlacement(long nowMs) {
        if (lastPlaceMs > 0) {
            long dt = nowMs - lastPlaceMs;
            placementIntervalsMs.add(dt);
            if (dt <= 350) continuousPlaceChain++;
            else continuousPlaceChain = 0;
        }
        placementTimesMs.add(nowMs);
        lastPlaceMs = nowMs;
    }

    public int continuousPlaceChain() {
        return continuousPlaceChain;
    }

    public long lastPlaceMs() {
        return lastPlaceMs;
    }

    public void recordPlacementOffset(int dx, int dy, int dz) {
        // pack small offsets into a code; clamp to reasonable range
        dx = clamp(dx, -3, 3);
        dy = clamp(dy, -3, 3);
        dz = clamp(dz, -3, 3);
        int code = (dx + 3) * 49 + (dy + 3) * 7 + (dz + 3);
        offsetCodes.add(code);
    }

    public void recordJumpPlaceDeltaIfAny() {
        if (jumpStartTicks.size() == 0) return;
        int lastJumpTick = (int) jumpStartTicks.peekLast();
        int delta = tickCounter - lastJumpTick;
        if (delta >= 0 && delta <= 40) {
            jumpPlaceDeltaTicks.add(delta);
        }
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public long now() {
        return TimeUtil.nowMs();
    }
}