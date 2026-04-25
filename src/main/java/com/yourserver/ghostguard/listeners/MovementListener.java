// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/listeners/MovementListener.java
package com.yourserver.ghostguard.listeners;

import com.yourserver.ghostguard.GhostGuardPlugin;
import com.yourserver.ghostguard.checks.bednuke.DigPatternTracker;
import com.yourserver.ghostguard.config.GGConfig;
import com.yourserver.ghostguard.core.PlayerData;
import com.yourserver.ghostguard.core.PlayerDataManager;
import com.yourserver.ghostguard.integration.LiteBansPunisher;
import com.yourserver.ghostguard.integration.StaffAlertService;
import com.yourserver.ghostguard.platform.PingProvider;
import com.yourserver.ghostguard.platform.TpsMonitor;
import com.yourserver.ghostguard.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public final class MovementListener implements Listener {

    private final GhostGuardPlugin plugin;
    private final GGConfig cfg;
    private final PlayerDataManager data;
    private final StaffAlertService alerts;
    private final TpsMonitor tps;
    private final PingProvider pingProvider;
    private final LiteBansPunisher punisher;

    public MovementListener(GhostGuardPlugin plugin, GGConfig cfg, PlayerDataManager data,
                            StaffAlertService alerts, TpsMonitor tps, PingProvider pingProvider,
                            LiteBansPunisher punisher) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.data = data;
        this.alerts = alerts;
        this.tps = tps;
        this.pingProvider = pingProvider;
        this.punisher = punisher;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        PlayerData pd = data.get(p);

        pd.tick();

        Location to = e.getTo();
        if (to == null) return;

        long now = System.currentTimeMillis();

        Location last = pd.lastLoc();
        long lastMs = pd.lastMoveMs();
        if (last != null && last.getWorld().equals(to.getWorld())) {
            double dx = to.getX() - last.getX();
            double dz = to.getZ() - last.getZ();
            double dist = Math.sqrt(dx*dx + dz*dz);

            long dtMs = Math.max(1, now - lastMs);
            double speedBps = (dist / dtMs) * 1000.0;

            pd.recordMoveSample(speedBps);

            // Movement direction vs look direction (for backwards scaffold)
            Vector move = new Vector(dx, 0, dz);
            if (move.lengthSquared() > 1e-6) move.normalize();
            Vector look = to.getDirection().setY(0);
            if (look.lengthSquared() > 1e-6) look.normalize();
            double dot = move.dot(look);
            pd.digPatternTracker().updateMovementDot(dot);

            // Sneak samples (support)
            pd.recordSneakSample(p.isSneaking());

            // Jump detection by ground transitions
            boolean onGround = p.isOnGround();
            if (pd.lastOnGround() && !onGround) {
                pd.recordJumpStart();
            }
            pd.setLastOnGround(onGround);
        }

        pd.updateRotation(p);

        pd.setLastLoc(to.clone());
        pd.setLastMoveMs(now);
    }
}