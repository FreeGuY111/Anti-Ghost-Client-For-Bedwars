// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/checks/scaffold/GhostPlaceTracker.java
package com.yourserver.ghostguard.checks.scaffold;

import java.util.ArrayDeque;
import java.util.Deque;

public final class GhostPlaceTracker {
    private final Deque<Long> attemptsMs = new ArrayDeque<>();
    private final Deque<Long> successesMs = new ArrayDeque<>();
    private final Deque<Long> invalidCancelledMs = new ArrayDeque<>();

    public void recordAttempt() {
        attemptsMs.addLast(System.currentTimeMillis());
    }

    public void recordSuccess() {
        successesMs.addLast(System.currentTimeMillis());
    }

    public void recordInvalidCancelled() {
        invalidCancelledMs.addLast(System.currentTimeMillis());
    }

    public void prune(long nowMs, int windowSeconds) {
        long cutoff = nowMs - windowSeconds * 1000L;
        pruneDeque(attemptsMs, cutoff);
        pruneDeque(successesMs, cutoff);
        pruneDeque(invalidCancelledMs, cutoff);
    }

    private void pruneDeque(Deque<Long> dq, long cutoff) {
        while (!dq.isEmpty() && dq.peekFirst() < cutoff) dq.removeFirst();
    }

    public int attempts() { return attemptsMs.size(); }
    public int successes() { return successesMs.size(); }
    public int invalidCancelled() { return invalidCancelledMs.size(); }

    public double attemptSuccessRatio() {
        int s = successes();
        if (s <= 0) return attempts() > 0 ? 999.0 : 1.0;
        return attempts() / (double) s;
    }
}