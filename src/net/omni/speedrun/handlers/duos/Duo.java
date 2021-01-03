package net.omni.speedrun.handlers.duos;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Duo {
    private final String name;
    private final String other;

    public Duo(String name, String other) {
        this.name = name;
        this.other = other;
    }

    public String getName() {
        return name;
    }

    public String getOtherName() {
        return other;
    }

    public String getOther(String name) {
        return name.equalsIgnoreCase(this.name) ? getOtherName() : getName();
    }

    public Player getOtherPlayer(String name) {
        return Bukkit.getPlayer(getOther(name));
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

    public Player getOther() {
        return Bukkit.getPlayer(other);
    }

    public String getTeamInConfig() {
        return name + "|" + other;
    }
}
