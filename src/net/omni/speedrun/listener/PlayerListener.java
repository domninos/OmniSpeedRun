package net.omni.speedrun.listener;

import net.omni.speedrun.SpeedRunPlugin;
import net.omni.speedrun.handlers.duos.Duo;
import org.bukkit.Bukkit;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
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

        if (plugin.getConfig().getBoolean("duoServer")) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online != null && !player.getName().equals(online.getName())) {
                    plugin.getDuoHandler().remove(online);
                    plugin.getDuoHandler().setDuo(player, online);
                    break;
                }
            }
        }

        if (plugin.getDuoHandler().isInDuo(player.getName())) {
            Duo duo = plugin.getDuoHandler().getDuo(player.getName());

            Player otherPlayer = duo.getOtherPlayer(player.getName());

            if (otherPlayer == null)
                plugin.sendMessage(player, "&cYour duo is not yet online, timer will not start");
            else {
                if (plugin.getDuoTimerHandler().isInConfig(duo)) {
                    plugin.getDuoTimerHandler().startTimer(duo);
                    plugin.sendMessage(player, "&aYour timer has been started.");
                    plugin.sendMessage(otherPlayer, "&aYour timer has been started.");
                    plugin.sendConsole("&aTimer started for " + player.getName() + " and " + otherPlayer.getName());
                }
            }
        } else {
            if (plugin.getTimerHandler().isInConfig(player.getName())) {
                plugin.getTimerHandler().startTimer(player.getName());
                plugin.sendConsole("&aTimer started for " + player.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getDuoHandler().isInDuo(player.getName())) {
            Duo duo = plugin.getDuoHandler().getDuo(player.getName());
            Player otherDuo = duo.getOtherPlayer(player.getName());

            if (plugin.getDuoTimerHandler().hasTimer(duo)) {
                plugin.getDuoTimerHandler().stopTimer(duo);
                plugin.sendConsole("&aTimer stopped for duo: " + player.getName()
                        + " and " + duo.getOther(player.getName()));

                if (otherDuo != null)
                    otherDuo.kickPlayer("Your duo timer has been stopped because " + player.getName()
                            + " has left.");
            }
        } else {
            if (plugin.getTimerHandler().hasTimer(player.getName())) {
                plugin.getTimerHandler().stopTimer(player.getName());
                plugin.sendConsole("&aTimer stopped for " + player.getName());
            }
        }

        plugin.getDuoHandler().remove(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (plugin.getDuoHandler().isInDuo(player.getName())) {
            Duo duo = plugin.getDuoHandler().getDuo(player.getName());

            if (plugin.getDuoTimerHandler().hasTimer(duo)) {
                plugin.getDuoTimerHandler().reset(duo);

                plugin.sendMessage(player, "&aYour timer has been reset.");

                Player otherDuo = duo.getOtherPlayer(player.getName());

                if (otherDuo != null)
                    plugin.sendMessage(otherDuo, "&aYour timer has been reset.");
            }
        } else {
            if (plugin.getTimerHandler().hasTimer(player.getName())) {
                plugin.getTimerHandler().reset(player.getName());
                plugin.sendMessage(player, "&aYour timer has been reset.");
            }
        }
    }

    @EventHandler
    public void onPlayerKillEnderDragon(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (!(entity instanceof EnderDragon))
            return;

        EnderDragon enderDragon = (EnderDragon) entity;
        Player killer = entity.getKiller();

        if (killer == null) {
            if (plugin.getBedKillHandler().hasBedDamage(enderDragon))
                killer = plugin.getBedKillHandler().getBedDamager(enderDragon);
        }

        if (killer == null) {
            plugin.sendConsole("&cKiller of an ender dragon not found.");
            return;
        }

        if (plugin.getDuoHandler().isInDuo(killer.getName())) {
            Duo duo = plugin.getDuoHandler().getDuo(killer.getName());
            Player otherDuo = duo.getOtherPlayer(killer.getName());

            if (plugin.getDuoTimerHandler().hasTimer(duo)) {
                for (String command : plugin.getConfig().getStringList("commandsToExecute")) {
                    if (command != null) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                command.replaceAll("%player%", killer.getName()));

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                command.replaceAll("%player%", duo.getOther(killer.getName())));
                    }
                }

                plugin.sendMessage(killer, "&aYou finished at " +
                        plugin.convertTime(plugin.getDuoTimerHandler().getTimer(duo)));

                if (otherDuo != null)
                    plugin.sendMessage(otherDuo, "&aYou finished at " +
                            plugin.convertTime(plugin.getDuoTimerHandler().getTimer(duo)));

                plugin.getDuoTimerHandler().finish(duo);
                plugin.getDuoTopHandler().update();
            }
        } else {
            if (plugin.getTimerHandler().hasTimer(killer.getName())) {
                for (String command : plugin.getConfig().getStringList("commandsToExecute")) {
                    if (command != null)
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                command.replaceAll("%player%", killer.getName()));
                }

                plugin.sendMessage(killer, "&aYou finished at " +
                        plugin.convertTime(plugin.getTimerHandler().getTimer(killer.getName())));
                plugin.getTimerHandler().finish(killer.getName());
                plugin.getTopHandler().update();
            }
        }

        plugin.getBedKillHandler().removeBedDamage(enderDragon);
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
