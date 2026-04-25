// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/listeners/JoinQuitListener.java
package com.yourserver.ghostguard.listeners;

import com.yourserver.ghostguard.core.PlayerDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public final class JoinQuitListener implements Listener {

    private final PlayerDataManager data;

    public JoinQuitListener(PlayerDataManager data) {
        this.data = data;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        data.remove(e.getPlayer().getUniqueId());
    }
}