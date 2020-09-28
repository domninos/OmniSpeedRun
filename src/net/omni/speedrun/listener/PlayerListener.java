package net.omni.speedrun.listener;

import net.omni.speedrun.SpeedRunPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final SpeedRunPlugin plugin;

    public PlayerListener(SpeedRunPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.getTimerHandler().isInConfig(player.getName())) {
            plugin.getTimerHandler().startTimer(player.getName());
            plugin.sendConsole("&aTimer started for " + player.getName());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getTimerHandler().hasTimer(player.getName())) {
            plugin.getTimerHandler().stopTimer(player.getName());
            plugin.sendConsole("&aTimer stopped for " + player.getName());
        }
    }

    @EventHandler
    public void onPlayerKillEnderDragon(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof EnderDragon) {
            Player killer = entity.getKiller();

            if (killer == null) {
                plugin.sendConsole("&cKiller of an ender dragon not found.");
                return;
            }

            if (plugin.getTimerHandler().hasTimer(killer.getName())) {
                for (String command : plugin.getConfig().getStringList("commandsToExecute")) {
                    if (command != null)
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                command.replaceAll("%player%", killer.getName()));
                }

                plugin.sendMessage(killer, "&aYou finished at " +
                        plugin.getTimerHandler().convertTime(plugin.getTimerHandler().getTimer(killer.getName())));
                plugin.getTimerHandler().finish(killer.getName());
                plugin.getTopHandler().update();
            }
        }
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
