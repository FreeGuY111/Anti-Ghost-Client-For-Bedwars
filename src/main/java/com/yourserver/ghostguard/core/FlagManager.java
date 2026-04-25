// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/core/FlagManager.java
package com.yourserver.ghostguard.core;

import com.yourserver.ghostguard.config.GGConfig;

import java.util.ArrayDeque;
import java.util.Deque;

public final class FlagManager {

    private final Deque<Long> scaffoldFlags = new ArrayDeque<>();
    private final Deque<Long> bednukeFlags = new ArrayDeque<>();

    private long lastScaffoldFlagMs = 0;
    private long lastBednukeFlagMs = 0;

    private long lastPunishMs = 0;

    public Deque<Long> scaffoldFlags() { return scaffoldFlags; }
    public Deque<Long> bednukeFlags() { return bednukeFlags; }

    public long lastPunishMs() { return lastPunishMs; }
    public void setLastPunishMs(long ms) { lastPunishMs = ms; }

    public boolean tryAddScaffoldFlag(GGConfig cfg, long nowMs, double score) {
        prune(scaffoldFlags, nowMs, cfg.flagWindowMinutes);
        if (score < cfg.scoreThreshold) return false;

        long cd = cfg.flagCooldownSeconds * 1000L;
        if (nowMs - lastScaffoldFlagMs < cd) return false;

        scaffoldFlags.addLast(nowMs);
        lastScaffoldFlagMs = nowMs;
        prune(scaffoldFlags, nowMs, cfg.flagWindowMinutes);
        return true;
    }

    public boolean tryAddBednukeFlag(GGConfig cfg, long nowMs, double score) {
        prune(bednukeFlags, nowMs, cfg.flagWindowMinutes);
        if (score < cfg.scoreThreshold) return false;

        long cd = cfg.flagCooldownSeconds * 1000L;
        if (nowMs - lastBednukeFlagMs < cd) return false;

        bednukeFlags.addLast(nowMs);
        lastBednukeFlagMs = nowMs;
        prune(bednukeFlags, nowMs, cfg.flagWindowMinutes);
        return true;
    }

    public int scaffoldRecentCount(GGConfig cfg, long nowMs) {
        prune(scaffoldFlags, nowMs, cfg.flagWindowMinutes);
        return scaffoldFlags.size();
    }

    public int bednukeRecentCount(GGConfig cfg, long nowMs) {
        prune(bednukeFlags, nowMs, cfg.flagWindowMinutes);
        return bednukeFlags.size();
    }

    private void prune(Deque<Long> dq, long nowMs, int windowMinutes) {
        long cutoff = nowMs - windowMinutes * 60_000L;
        while (!dq.isEmpty() && dq.peekFirst() < cutoff) dq.removeFirst();
    }
}