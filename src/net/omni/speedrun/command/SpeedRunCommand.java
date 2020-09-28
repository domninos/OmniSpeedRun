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
                "&e/speedrun stop <player> &7» Stops timer of the player."
        };

        this.help = plugin.translate(String.join("\n", msg));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!sender.hasPermission("speedrun.use"))
            return noPerms(sender);

        if (args.length == 0 || args.length == 1 || args.length == 3) {
            plugin.sendMessage(sender, help);
            return true;
        }

        if (args.length == 2) {
            if (!(args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("stop"))) {
                plugin.sendMessage(sender, help);
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                plugin.sendMessage(sender, "&cPlayer not found.");
                return true;
            }

            if (args[0].equalsIgnoreCase("start")) {
                if (!sender.hasPermission("speedrun.start"))
                    return noPerms(sender);

                if (plugin.getTimerHandler().hasTimer(target.getName())) {
                    plugin.sendMessage(sender, "&cPlayer has an already started timer.");
                    return true;
                }

                plugin.getTimerHandler().startTimer(target.getName());
                plugin.sendMessage(sender, "&aSuccessfully started timer of " + target.getName());
            } else if (args[0].equalsIgnoreCase("stop")) {
                if (!sender.hasPermission("speedrun.stop"))
                    return noPerms(sender);

                if (!plugin.getTimerHandler().hasTimer(target.getName())) {
                    plugin.sendMessage(sender, "&cPlayer does not have a started timer.");
                    return true;
                }

                plugin.getTimerHandler().stopTimer(target.getName());
                plugin.sendMessage(sender, "&aSuccessfully stopped timer of " + target.getName());
            }

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
