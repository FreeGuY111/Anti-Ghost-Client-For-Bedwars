// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/core/EvidenceSnapshot.java
package com.yourserver.ghostguard.core;

public final class EvidenceSnapshot {
    public String checkName;

    public int recentFlags;
    public int thresholdFlags;

    public double score;
    public double scoreThreshold;

    public boolean bridgingContext;

    // Scaffold evidence
    public double intervalVarianceMs;
    public double narrowBandRatio;
    public double speedStdDev;
    public double microPauseRatio;
    public double offsetTop2Ratio;
    public double underfootRatio;
    public double sneakRatio;
    public double yawEma;
    public double pitchEma;

    public double backwardsDotAvg;
    public double jumpRhythmConsistency;
    public double attemptSuccessRatio;
    public int invalidCancelBurst;

    // Bednuke evidence
    public boolean targetIsBed;
    public double distance;
    public boolean hasLOS;
    public double aimAngleDeg;
    public int invalidBurstCount;
    public long fastRepeatMs;

    public String contextText;
}