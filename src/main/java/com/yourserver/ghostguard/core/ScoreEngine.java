// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/core/ScoreEngine.java
package com.yourserver.ghostguard.core;

import com.yourserver.ghostguard.config.GGConfig;

public final class ScoreEngine {
    private double scaffoldScore;
    private double bednukeScore;

    public double scaffold() { return scaffoldScore; }
    public double bednuke() { return bednukeScore; }

    public void addScaffold(double add) {
        scaffoldScore = clamp(scaffoldScore + add, 0, 100);
    }

    public void addBednuke(double add) {
        bednukeScore = clamp(bednukeScore + add, 0, 100);
    }

    public void decay(GGConfig cfg) {
        scaffoldScore = clamp(scaffoldScore - cfg.decayScaffoldPerSec, 0, 100);
        bednukeScore = clamp(bednukeScore - cfg.decayBednukePerSec, 0, 100);
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}