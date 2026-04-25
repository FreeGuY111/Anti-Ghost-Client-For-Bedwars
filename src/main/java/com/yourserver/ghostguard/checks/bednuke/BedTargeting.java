// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/checks/bednuke/BedTargeting.java
package com.yourserver.ghostguard.checks.bednuke;

public final class BedTargeting {
    public boolean isBed;
    public double distance;
    public boolean hasLOS;
    public double aimAngleDeg;
    public boolean valid;

    public long repeatMs;
    public int invalidBurst;
}