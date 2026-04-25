// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/checks/bednuke/DigPatternTracker.java
package com.yourserver.ghostguard.checks.bednuke;

public final class DigPatternTracker {

    private long lastDigAttemptMs = 0;
    private double movementDotEma = 0.0;

    public void recordDigAttempt() {
        lastDigAttemptMs = System.currentTimeMillis();
    }

    public long lastDigAttemptMs() { return lastDigAttemptMs; }

    public void updateMovementDot(double dot) {
        movementDotEma = movementDotEma + 0.10 * (dot - movementDotEma);
    }

    public double movementDotEma() { return movementDotEma; }
}