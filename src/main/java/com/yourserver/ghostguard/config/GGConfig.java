// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/config/GGConfig.java
package com.yourserver.ghostguard.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class GGConfig {

    private final JavaPlugin plugin;

    public boolean enableScaffold;
    public boolean enableBednuke;

    public double scoreThreshold;

    public double decayScaffoldPerSec;
    public double decayBednukePerSec;

    public int flagCooldownSeconds;
    public int flagWindowMinutes;

    public int scaffoldFlagsToBan;
    public int bednukeFlagsToKick;

    public String scaffoldBanDuration;
    public String scaffoldBanReason;
    public String bednukeKickReason;

    public boolean tpsProtectionEnabled;
    public double tpsMin;

    public boolean pingProtectionEnabled;
    public int pingThresholdMs;

    public String staffPermission;

    public String alertPrefix;
    public String alertScaffoldTitle;
    public String alertBednukeTitle;

    public boolean debugMode;
    public boolean evidenceLogs;
    public boolean logToConsole;

    // Scaffold tuning
    public double scaffoldWeightCadence;
    public double scaffoldWeightSpeedCoupling;
    public double scaffoldWeightOffsetCluster;
    public double scaffoldWeightNoSneak;
    public double scaffoldWeightRotationSmooth;
    public double scaffoldWeightBackwardsJump;
    public double scaffoldWeightGhostPlaceSpam;

    public int scaffoldCadenceSampleSize;
    public int scaffoldCadenceNarrowBandMs;
    public double scaffoldCadenceNarrowBandRatio;
    public double scaffoldCadenceVarianceMsThreshold;

    public int scaffoldSpeedSampleSize;
    public double scaffoldSpeedStddevThreshold;
    public double scaffoldSpeedMicroPauseRatioThreshold;

    public int scaffoldOffsetSampleSize;
    public double scaffoldOffsetTop2Ratio;
    public double scaffoldOffsetUnderfootRatio;

    public double scaffoldBackwardsDotThreshold;
    public int scaffoldBackwardsMinChainPlaces;
    public int scaffoldJumpRhythmWindow;
    public int scaffoldJumpPlaceTickMin;
    public int scaffoldJumpPlaceTickMax;
    public double scaffoldJumpRhythmConsistencyRatio;

    public boolean scaffoldGhostPlaceEnabled;
    public int scaffoldGhostWindowSeconds;
    public double scaffoldAttemptSuccessRatioThreshold;
    public int scaffoldInvalidCancelBurstThreshold;

    // Bridging context
    public double bridgingMinHorizontalSpeedBlocksPerSec;
    public int bridgingMinRepeatedPlaces;
    public int bridgingVoidCheckDepth;
    public int bridgingEdgeRadius;
    public double bridgingPitchDownSupportDeg;

    // Bednuke tuning
    public double bednukeMaxDistance;
    public double bednukeAimAngleDeg;
    public int bednukeInvalidWindowSeconds;
    public int bednukeInvalidBurstThreshold;
    public int bednukeFastRepeatMsThreshold;

    public boolean bednukeMitigationEnabled;
    public double bednukeCancelWhenScoreGe;

    public GGConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();

        enableScaffold = c.getBoolean("enable_scaffold", true);
        enableBednuke = c.getBoolean("enable_bednuke", true);

        scoreThreshold = c.getDouble("score_threshold", 75.0);

        decayScaffoldPerSec = c.getDouble("score_decay_per_second.scaffold", 2.0);
        decayBednukePerSec = c.getDouble("score_decay_per_second.bednuke", 2.0);

        flagCooldownSeconds = c.getInt("flag_cooldown_seconds", 10);
        flagWindowMinutes = c.getInt("flag_window_minutes", 8);

        scaffoldFlagsToBan = c.getInt("scaffold_flags_to_ban", 10);
        bednukeFlagsToKick = c.getInt("bednuke_flags_to_kick", 10);

        scaffoldBanDuration = c.getString("scaffold_ban_duration", "15m");
        scaffoldBanReason = c.getString("scaffold_ban_reason", "Unfair Advantage [Scaffold]");
        bednukeKickReason = c.getString("bednuke_kick_reason", "Unfair Advantage [BedNuke]");

        tpsProtectionEnabled = c.getBoolean("tps_protection_enabled", true);
        tpsMin = c.getDouble("tps_min", 18.0);

        pingProtectionEnabled = c.getBoolean("ping_protection_enabled", true);
        pingThresholdMs = c.getInt("ping_threshold_ms", 300);

        staffPermission = c.getString("staff_permission", "anticheat.alerts");

        alertPrefix = c.getString("alerts.prefix", "&7[&cAC&7] ");
        alertScaffoldTitle = c.getString("alerts.scaffold_title", "&cScaffold");
        alertBednukeTitle = c.getString("alerts.bednuke_title", "&cBedNuke");

        debugMode = c.getBoolean("logging.debug_mode", false);
        evidenceLogs = c.getBoolean("logging.evidence_logs", true);
        logToConsole = c.getBoolean("logging.log_to_console", true);

        // scaffold weights
        scaffoldWeightCadence = c.getDouble("scaffold.weights.cadence_constancy", 28.0);
        scaffoldWeightSpeedCoupling = c.getDouble("scaffold.weights.speed_coupling", 28.0);
        scaffoldWeightOffsetCluster = c.getDouble("scaffold.weights.offset_clustering", 22.0);
        scaffoldWeightNoSneak = c.getDouble("scaffold.weights.no_sneak_support", 8.0);
        scaffoldWeightRotationSmooth = c.getDouble("scaffold.weights.rotation_smooth_support", 6.0);
        scaffoldWeightBackwardsJump = c.getDouble("scaffold.weights.backwards_jump_scaffold", 24.0);
        scaffoldWeightGhostPlaceSpam = c.getDouble("scaffold.weights.ghost_place_spam", 35.0);

        scaffoldCadenceSampleSize = c.getInt("scaffold.cadence.sample_size", 40);
        scaffoldCadenceNarrowBandMs = c.getInt("scaffold.cadence.narrow_band_ms", 35);
        scaffoldCadenceNarrowBandRatio = c.getDouble("scaffold.cadence.narrow_band_ratio_threshold", 0.75);
        scaffoldCadenceVarianceMsThreshold = c.getDouble("scaffold.cadence.variance_ms_threshold", 900.0);

        scaffoldSpeedSampleSize = c.getInt("scaffold.speed.sample_size", 30);
        scaffoldSpeedStddevThreshold = c.getDouble("scaffold.speed.stability_stddev_threshold", 0.35);
        scaffoldSpeedMicroPauseRatioThreshold = c.getDouble("scaffold.speed.micro_pause_ratio_threshold", 0.08);

        scaffoldOffsetSampleSize = c.getInt("scaffold.offsets.sample_size", 25);
        scaffoldOffsetTop2Ratio = c.getDouble("scaffold.offsets.top2_ratio_threshold", 0.86);
        scaffoldOffsetUnderfootRatio = c.getDouble("scaffold.offsets.underfoot_ratio_threshold", 0.65);

        scaffoldBackwardsDotThreshold = c.getDouble("scaffold.backwards.dot_threshold", -0.30);
        scaffoldBackwardsMinChainPlaces = c.getInt("scaffold.backwards.min_chain_places", 10);
        scaffoldJumpRhythmWindow = c.getInt("scaffold.backwards.jump_rhythm_window", 18);
        scaffoldJumpPlaceTickMin = c.getInt("scaffold.backwards.jump_place_tick_min", 1);
        scaffoldJumpPlaceTickMax = c.getInt("scaffold.backwards.jump_place_tick_max", 4);
        scaffoldJumpRhythmConsistencyRatio = c.getDouble("scaffold.backwards.jump_rhythm_consistency_ratio", 0.70);

        scaffoldGhostPlaceEnabled = c.getBoolean("scaffold.ghost_place.enabled", true);
        scaffoldGhostWindowSeconds = c.getInt("scaffold.ghost_place.window_seconds", 12);
        scaffoldAttemptSuccessRatioThreshold = c.getDouble("scaffold.ghost_place.attempt_success_ratio_threshold", 2.8);
        scaffoldInvalidCancelBurstThreshold = c.getInt("scaffold.ghost_place.invalid_cancel_burst_threshold", 8);

        bridgingMinHorizontalSpeedBlocksPerSec = convertMinSpeedToBlocksPerSec(c.getDouble("scaffold.bridging_context.min_horizontal_speed", 0.08));
        bridgingMinRepeatedPlaces = c.getInt("scaffold.bridging_context.min_repeated_places", 6);
        bridgingVoidCheckDepth = c.getInt("scaffold.bridging_context.void_check_depth", 2);
        bridgingEdgeRadius = c.getInt("scaffold.bridging_context.edge_check_radius", 1);
        bridgingPitchDownSupportDeg = c.getDouble("scaffold.bridging_context.pitch_down_support_deg", 65);

        bednukeMaxDistance = c.getDouble("bednuke.max_distance", 4.6);
        bednukeAimAngleDeg = c.getDouble("bednuke.aim_angle_deg", 18.0);
        bednukeInvalidWindowSeconds = c.getInt("bednuke.invalid_window_seconds", 8);
        bednukeInvalidBurstThreshold = c.getInt("bednuke.invalid_burst_threshold", 6);
        bednukeFastRepeatMsThreshold = c.getInt("bednuke.fast_repeat_ms_threshold", 85);

        bednukeMitigationEnabled = c.getBoolean("bednuke.mitigation.enabled", true);
        bednukeCancelWhenScoreGe = c.getDouble("bednuke.mitigation.cancel_when_score_ge", 80);
    }

    private double convertMinSpeedToBlocksPerSec(double cfgValue) {
        // config is "small threshold"; many people think in per-tick; our engine uses blocks/sec
        // if user used ~0.08, approximate *20 -> 1.6 blocks/sec
        return cfgValue * 20.0;
    }
}