// bedwars-ghostguard/src/main/java/com/yourserver/ghostguard/core/PlayerDataManager.java
package com.yourserver.ghostguard.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class PlayerDataManager {
    private final Map<UUID, PlayerData> map = new ConcurrentHashMap<>();

    public PlayerData get(Player p) {
        return map.computeIfAbsent(p.getUniqueId(), PlayerData::new);
    }

    public void remove(UUID uuid) {
        map.remove(uuid);
    }

    public void forEachOnline(Consumer<PlayerData> consumer) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            consumer.accept(get(p));
        }
    }
}