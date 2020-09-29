package net.omni.speedrun.command;

import net.omni.speedrun.SpeedRunPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class SpeedRunCommand implements CommandExecutor {
    private final SpeedRunPlugin plugin;
    private final String help;

    public SpeedRunCommand(SpeedRunPlugin plugin) {
        this.plugin = plugin;

        String[] msg = {
                "&l&eSpeedRun Help",
                "&e/speedrun start <player> &7» Starts timer of the player.",
                "&e/speedrun stop <player> &7» Stops timer of the player.",
                "&e/speedrun timer |player| &7» Shows your timer | another player's timer.",
                "&e/speedrun reset |player| &7» Resets your timer | another player's timer."
        };

        this.help = plugin.translate(String.join("\n", msg));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!sender.hasPermission("speedrun.use"))
            return noPerms(sender);

        if (args.length == 0 || args.length == 3) {
            plugin.sendMessage(sender, help);
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("timer")) {
                if (!(sender instanceof Player)) {
                    plugin.sendMessage(sender, "&cOnly players can execute this command.");
                    return true;
                }

                Player player = (Player) sender;

                if (!player.hasPermission("speedrun.timer"))
                    return noPerms(player);

                String time;

                if (plugin.getTimerHandler().hasTimer(player.getName())) { // stored in cache
                    time = plugin.getTimerHandler().convertTime(plugin.getTimerHandler().getTimer(player.getName()));

                    plugin.sendMessage(player, "&aYour current time: &e" + time);
                } else if (plugin.getTimerHandler().isInConfig(player.getName())) { // stored in config
                    time = plugin.getTimerHandler().
                            convertTime(plugin.getTimerHandler().getTimeInConfig(player.getName()));

                    plugin.sendMessage(player, "&aYour saved time: &e" + time);
                } else // timer not found anywhere
                    plugin.sendMessage(player, "&cYour timer has not been found.");
            } else if (args[0].equalsIgnoreCase("reset")) {
                if (!(sender instanceof Player)) {
                    plugin.sendMessage(sender, "&cOnly players can execute this command.");
                    return true;
                }

                Player player = (Player) sender;

                if (!player.hasPermission("speedrun.reset"))
                    return noPerms(player);

                if (!plugin.getTimerHandler().hasTimer(player.getName())) {
                    plugin.sendMessage(player, "&cYou do not have a timer on.");
                    return true;
                }

                plugin.getTimerHandler().reset(player.getName());
                plugin.sendMessage(player, "&cYour timer has been reset.");
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("speedrun.reload"))
                    return noPerms(sender);

                plugin.reloadConfig();
                plugin.sendMessage(sender, "&aSuccessfully reloaded config.");
            } else
                plugin.sendMessage(sender, help);

            return true;
        }

        if (args.length == 2) {
            if (!(args[0].equalsIgnoreCase("start")
                    || args[0].equalsIgnoreCase("stop")
                    || args[0].equalsIgnoreCase("timer")
                    || args[0].equalsIgnoreCase("reset"))) {
                plugin.sendMessage(sender, help);
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);

            if (args[0].equalsIgnoreCase("start")) {
                if (!sender.hasPermission("speedrun.start"))
                    return noPerms(sender);

                if (target == null) {
                    plugin.sendMessage(sender, "&cPlayer not found.");
                    return true;
                }

                if (plugin.getTimerHandler().hasTimer(target.getName())) {
                    plugin.sendMessage(sender, "&cPlayer has an already started timer.");
                    return true;
                }

                plugin.getTimerHandler().startTimer(target.getName());
                plugin.sendMessage(sender, "&aSuccessfully started timer of " + target.getName());
            } else if (args[0].equalsIgnoreCase("stop")) {
                if (!sender.hasPermission("speedrun.stop"))
                    return noPerms(sender);

                if (target == null) {
                    plugin.sendMessage(sender, "&cPlayer not found.");
                    return true;
                }

                if (!plugin.getTimerHandler().hasTimer(target.getName())) {
                    plugin.sendMessage(sender, "&cPlayer does not have a started timer.");
                    return true;
                }

                plugin.getTimerHandler().stopTimer(target.getName());
                plugin.sendMessage(sender, "&aSuccessfully stopped timer of " + target.getName());
            } else if (args[0].equalsIgnoreCase("timer")) {
                if (!sender.hasPermission("speedrun.timer.other"))
                    return noPerms(sender);

                String targetPlayer;
                String time;

                if (target == null) { // player not online
                    targetPlayer = args[0];

                    if (plugin.getTimerHandler().isInConfig(targetPlayer)) { // player's time is in config
                        time = plugin.getTimerHandler().
                                convertTime(plugin.getTimerHandler().getTimeInConfig(targetPlayer));

                        plugin.sendMessage(sender, "&a" + targetPlayer + "'s current time: &e" + time);
                    } else
                        plugin.sendMessage(sender, "&c" + targetPlayer + "'s time not found.");
                } else { // player online
                    targetPlayer = target.getName();

                    if (plugin.getTimerHandler().hasTimer(targetPlayer)) {
                        time = plugin.getTimerHandler().convertTime(plugin.getTimerHandler().getTimer(targetPlayer));

                        plugin.sendMessage(sender, "&a" + targetPlayer + "'s current time: &e" + time);
                    } else
                        plugin.sendMessage(sender, "&c" + targetPlayer + "'s time not found.");
                }

                return true;
            } else if (args[0].equalsIgnoreCase("reset")) {
                if (!sender.hasPermission("speedrun.reset.other"))
                    return noPerms(sender);

                if (target == null) {
                    plugin.sendMessage(sender, "&cPlayer not found.");
                    return true;
                }


                if (!plugin.getTimerHandler().hasTimer(target.getName())) {
                    plugin.sendMessage(sender, "&c" + target.getName() + " does not have a timer on.");
                    return true;
                }

                plugin.getTimerHandler().reset(target.getName());
                plugin.sendMessage(sender, "&a" + target.getName() + "'s timer has been reset.");
                plugin.sendMessage(target, "&aYour timer has been reset by " + sender.getName());
            } else
                plugin.sendMessage(sender, help);

            return true;
        }

        return true;
    }

    public void register() {
        PluginCommand pluginCommand = plugin.getCommand("speedrun");

        if (pluginCommand != null)
            pluginCommand.setExecutor(this);
    }

    private boolean noPerms(CommandSender sender) {
        plugin.sendMessage(sender, "&cYou do not have permissions to use this command.");
        return true;
    }
}
