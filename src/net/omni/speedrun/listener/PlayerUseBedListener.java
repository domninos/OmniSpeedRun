package net.omni.speedrun.listener;

import net.omni.speedrun.SpeedRunPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class PlayerUseBedListener implements Listener {

    private final SpeedRunPlugin plugin;

    public PlayerUseBedListener(SpeedRunPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUseBed(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.NOT_POSSIBLE_HERE) {
            Player player = event.getPlayer();

            if (!plugin.getTimerHandler().hasTimer(player.getName()))
                return;

            for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
                if (entity == null)
                    continue;

                if (!(entity instanceof LivingEntity))
                    continue;

                if (!(entity instanceof EnderDragon))
                    continue;

                EnderDragon enderDragon = (EnderDragon) entity;

                plugin.getBedKillHandler().setBedDamage(enderDragon, player);
                plugin.sendConsole("&aLast bed hit for an ender dragon is " + player.getName());
                break;
            }
        }
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
