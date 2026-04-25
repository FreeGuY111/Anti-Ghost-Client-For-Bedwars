// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/core/RollingStats.java
package com.yourserver.ghostguard.core;

import java.util.ArrayDeque;
import java.util.Deque;

public final class RollingStats {
    private final int cap;
    private final Deque<Double> dq = new ArrayDeque<>();

    public RollingStats(int cap) {
        this.cap = Math.max(5, cap);
    }

    public void add(double v) {
        dq.addLast(v);
        while (dq.size() > cap) dq.removeFirst();
    }

    public int size() { return dq.size(); }

    public double mean() {
        if (dq.isEmpty()) return 0.0;
        double s = 0;
        for (double v : dq) s += v;
        return s / dq.size();
    }

    public double stddev() {
        if (dq.size() < 2) return 0.0;
        double m = mean();
        double s = 0;
        for (double v : dq) {
            double d = v - m;
            s += d * d;
        }
        return Math.sqrt(s / (dq.size() - 1));
    }

    public double ratioAbove(double threshold) {
        if (dq.isEmpty()) return 0.0;
        int c = 0;
        for (double v : dq) if (v > threshold) c++;
        return c / (double) dq.size();
    }

    public double meanOf01() {
        return mean();
    }

    public void clear() { dq.clear(); }
}