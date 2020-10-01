package net.omni.speedrun.handlers;

import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BedKillHandler {
    private final Map<EnderDragon, Player> bedKills = new HashMap<>();

    public void setBedDamage(EnderDragon enderDragon, Player player) {
        bedKills.put(enderDragon, player);
    }

    public void removeBedDamage(EnderDragon enderDragon) {
        bedKills.remove(enderDragon);
    }

    public Player getBedDamager(EnderDragon enderDragon) {
        return bedKills.getOrDefault(enderDragon, null);
    }

    public boolean hasBedDamage(EnderDragon enderDragon) {
        return bedKills.containsKey(enderDragon);
    }

    public void flush() {
        bedKills.clear();
    }
}
