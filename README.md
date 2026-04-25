# BedwarsGhostGuard

Production-ready (NMS-free) anti-cheat plugin for Bedwars:
- Detects ghost-client scaffold automation using **multi-signal scoring + decay**
- Detects bednuke via **target validity** (bed type + distance + LOS + aim angle + repetition)
- **Low false positives** by using bridging-context filtering and sustained abnormal behavior thresholds
- Optional ProtocolLib for enhanced placement attempt tracking (plugin works without it)
- Integrates with LiteBans (Scaffold tempban only)

## Compatibility
- Runtime: **1.8.9 → 1.21.x**
- Build: Java 8, Maven
- No per-version NMS usage

## Install
1. Build:
   - `mvn -q clean package`
2. Put jar into `plugins/`
3. (Optional) Install ProtocolLib for better accuracy
4. Restart server
5. Adjust `config.yml` as needed

## Punishments
### Scaffold
- Staff alerts on flags
- When recent flags >= `scaffold_flags_to_ban` (default 10):
  - Executes:
    `litebans:tempban <player> 15m "Unfair Advantage [Scaffold]"`

### BedNuke
- Staff alerts on flags
- When recent flags >= `bednuke_flags_to_kick` (default 10):
  - Kicks player with configurable reason (default: `Unfair Advantage [BedNuke]`)
  - **No ban**

## Safety gates
- If TPS < `tps_min` (default 18): **no auto punish**, only alert/hold
- If Ping > `ping_threshold_ms` (default 300): **no auto punish**, only alert/hold

## Permissions
- `anticheat.alerts` : receive alerts (default OP)

## How alerts look
Example Scaffold:
[AC] Scaffold (7/10) PlayerName
intervalVar: X, narrowRatio: X
speedStd: X, microPause: X
offsetTop2: X%, underfoot: X%
sneakRatio: X%, rotEMA(y/p): X/X
backDotAvg: X, jumpRhythm: X%
attempt:success: X, invalidCancelBurst: X
context: bridging (...)

Example BedNuke:
[AC] BedNuke (6/10) PlayerName
targetIsBed: true, dist: X
LOS: false, aimAngle: Xdeg
invalidBurst: X, fastRepeat: Xms

## Notes
- The system avoids punishing solely for speed-bridging or not sneaking.
- It relies on sustained multi-signal evidence over time.