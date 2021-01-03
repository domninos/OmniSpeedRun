package net.omni.speedrun.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.omni.speedrun.SpeedRunPlugin;
import net.omni.speedrun.handlers.duos.Duo;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class DuoSpeedRunCommand implements CommandExecutor {
    private final SpeedRunPlugin plugin;
    private final String help;

    public DuoSpeedRunCommand(SpeedRunPlugin plugin) {
        this.plugin = plugin;

        String[] msg = {
                "&l&eDuoSpeedRun Help",
                "&e/duospeedrun start <player> &7» Starts timer of the duo.",
                "&e/duospeedrun stop <player> &7» Stops timer of the duo.",
                "&e/duospeedrun invite <player> &7» Invites the player to become your duo.",
                "&e/duospeedrun accept <player> &7» Accepts invitation from a player.",
                "&e/duospeedrun leave &7» Leaves your current duo.",
                "&e/duospeedrun timer |player| &7» Shows your duo timer | another duo's timer.",
                "&e/duospeedrun reset |player| &7» Resets your duo timer | another duo's timer."
        };

        this.help = plugin.translate(String.join("\n", msg));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!sender.hasPermission("duospeedrun.use"))
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

                if (!player.hasPermission("duospeedrun.timer"))
                    return noPerms(player);

                if (!plugin.getDuoHandler().isInDuo(player.getName()))
                    return noDuo(player);

                Duo duo = plugin.getDuoHandler().getDuo(player.getName());

                String time;

                if (plugin.getDuoTimerHandler().hasTimer(duo)) { // stored in cache
                    time = plugin.convertTime(plugin.getDuoTimerHandler().getTimer(duo));

                    plugin.sendMessage(player, "&aYour duo's current time: &e" + time);
                } else if (plugin.getDuoTimerHandler().isInConfig(duo)) { // stored in config
                    time = plugin.convertTime(plugin.getDuoTimerHandler().getTimeInConfig(duo));

                    plugin.sendMessage(player, "&aYour duo's saved time: &e" + time);
                } else // timer not found anywhere
                    plugin.sendMessage(player, "&cYour timer has not been found.");
            } else if (args[0].equalsIgnoreCase("reset")) {
                if (!(sender instanceof Player)) {
                    plugin.sendMessage(sender, "&cOnly players can execute this command.");
                    return true;
                }

                Player player = (Player) sender;

                if (!player.hasPermission("duospeedrun.reset"))
                    return noPerms(player);

                if (!plugin.getDuoHandler().isInDuo(player.getName()))
                    return noDuo(player);

                Duo duo = plugin.getDuoHandler().getDuo(player.getName());

                if (!plugin.getDuoTimerHandler().hasTimer(duo)) {
                    plugin.sendMessage(player, "&cYour duo does not have a timer on.");
                    return true;
                }

                plugin.getDuoTimerHandler().reset(duo);
                plugin.sendMessage(player, "&cYour duo's timer has been reset.");

                Player otherPlayer = duo.getOtherPlayer(player.getName());

                if (otherPlayer != null)
                    plugin.sendMessage(otherPlayer, "&aYour timer has been reset by "
                            + sender.getName());

                for (String command : plugin.getConfig().getStringList("commandsToExecuteOnReset")) {
                    if (command != null) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                command.replaceAll("%player%", player.getName()));

                        if (otherPlayer != null)
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                    command.replaceAll("%player", otherPlayer.getName()));
                    }
                }

            } else if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("duospeedrun.reload"))
                    return noPerms(sender);

                plugin.getDuoConfig().load();
                plugin.getDuoHandler().loadDuos();
                plugin.sendMessage(sender, "&aSuccessfully reloaded config.");
            } else if (args[0].equalsIgnoreCase("leave")) {
                if (!sender.hasPermission("duospeedrun.accept"))
                    return noPerms(sender);

                if (!(sender instanceof Player)) {
                    plugin.sendMessage(sender, "&cOnly players can execute this command.");
                    return true;
                }

                Player player = (Player) sender;

                if (!plugin.getDuoHandler().isInDuo(player.getName())) {
                    plugin.sendMessage(player, "&cYou are not in a duo.");
                    return true;
                }

                plugin.getDuoHandler().leave(player);
            } else
            plugin.sendMessage(sender, help);

            return true;
        }

        if (args.length == 2) {
            if (!(args[0].equalsIgnoreCase("invite")
                    || args[0].equalsIgnoreCase("accept")
                    || args[0].equalsIgnoreCase("start")
                    || args[0].equalsIgnoreCase("stop")
                    || args[0].equalsIgnoreCase("timer")
                    || args[0].equalsIgnoreCase("reset"))) {
                plugin.sendMessage(sender, help);
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);

            if (args[0].equalsIgnoreCase("invite")) {
                if (!sender.hasPermission("duospeedrun.invite"))
                    return noPerms(sender);

                if (!(sender instanceof Player)) {
                    plugin.sendMessage(sender, "&cOnly players can execute this command.");
                    return true;
                }

                Player player = (Player) sender;

                if (plugin.getDuoHandler().isInDuo(player.getName())) {
                    plugin.sendMessage(sender, "&cYou're already in a duo.");
                    return true;
                }

                if (target == null) {
                    plugin.sendMessage(sender, "&cPlayer not found.");
                    return true;
                }

                if (player.getName().equals(target.getName())) {
                    plugin.sendMessage(sender, "&cYou cannot invite yourself.");
                    return true;
                }

                if (plugin.getDuoHandler().isInDuo(target.getName())) {
                    plugin.sendMessage(sender, "&aPlayer is already in a duo.");
                    return true;
                }

                plugin.getDuoHandler().invite(player, target);
                plugin.sendMessage(player, "&aYou have invited " + target.getName());

                TextComponent textComponent = new TextComponent(player.getName()
                        + " has invited you to become his duo. Click this to accept invitation.");
                textComponent.setFont("minecraft:uniform");
                textComponent.setColor(ChatColor.RED);

                textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new Text("Click here to accept invitation")));
                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/duospeedrun accept " + player.getName()));

                target.spigot().sendMessage(textComponent);
            } else if (args[0].equalsIgnoreCase("accept")) {
                if (!sender.hasPermission("duospeedrun.accept"))
                    return noPerms(sender);

                if (!(sender instanceof Player)) {
                    plugin.sendMessage(sender, "&cOnly players can execute this command.");
                    return true;
                }

                Player player = (Player) sender;

                if (target == null) {
                    plugin.sendMessage(sender, "&cPlayer not found.");
                    return true;
                }

                if (!plugin.getDuoHandler().isInvited(player)) {
                    plugin.sendMessage(sender, "&cYou don't have any invitations.");
                    return true;
                }

                if (plugin.getDuoHandler().isInDuo(target.getName())) {
                    plugin.sendMessage(sender, "&c" + target.getName() + " is already in a duo.");
                    return true;
                }

                plugin.getDuoHandler().accept(target, player);
                plugin.sendMessage(sender, "&aYou are now duos with " + target.getName());
                plugin.sendMessage(target, "&aYou are now duos with " + player.getName());
            } else if (args[0].equalsIgnoreCase("start")) {
                if (!sender.hasPermission("duospeedrun.start"))
                    return noPerms(sender);

                if (target == null) {
                    plugin.sendMessage(sender, "&cPlayer not found.");
                    return true;
                }

                if (!plugin.getDuoHandler().isInDuo(target.getName()))
                    return noDuoTarget(sender);

                Duo duo = plugin.getDuoHandler().getDuo(target.getName());

                if (plugin.getDuoTimerHandler().hasTimer(duo)) {
                    plugin.sendMessage(sender, "&cDuo has an already started timer.");
                    return true;
                }

                plugin.getDuoTimerHandler().startTimer(duo);
                plugin.sendMessage(sender, "&aSuccessfully started timer of "
                        + target.getName() + " and " + duo.getOther(target.getName()));
            } else if (args[0].equalsIgnoreCase("stop")) {
                if (!sender.hasPermission("speedrun.stop"))
                    return noPerms(sender);

                if (target == null) {
                    plugin.sendMessage(sender, "&cPlayer not found.");
                    return true;
                }

                if (!plugin.getDuoHandler().isInDuo(target.getName()))
                    return noDuoTarget(sender);

                Duo duo = plugin.getDuoHandler().getDuo(target.getName());

                if (!plugin.getDuoTimerHandler().hasTimer(duo)) {
                    plugin.sendMessage(sender, "&cDuo does not have a started timer.");
                    return true;
                }

                plugin.getDuoTimerHandler().stopTimer(duo);
                plugin.sendMessage(sender, "&aSuccessfully stopped timer of " + target.getName()
                        + duo.getOther(target.getName()));
            } else if (args[0].equalsIgnoreCase("timer")) {
                if (!sender.hasPermission("speedrun.timer.other"))
                    return noPerms(sender);

                String targetPlayer;
                String time;

                if (target == null) { // player not online
                    targetPlayer = args[0];

                    Duo duo = plugin.getDuoHandler().getDuo(targetPlayer);

                    if (duo == null)
                        return noDuoTarget(sender);

                    if (plugin.getDuoTimerHandler().isInConfig(duo)) { // duo's time is in config
                        time = plugin.convertTime(plugin.getDuoTimerHandler().getTimeInConfig(duo));

                        plugin.sendMessage(sender, "&a" + targetPlayer + " and "
                                + duo.getOther(targetPlayer) + "'s current time: &e" + time);
                    } else
                        plugin.sendMessage(sender, "&c" + targetPlayer + "'s duo time not found.");
                } else { // player online
                    targetPlayer = target.getName();

                    if (!plugin.getDuoHandler().isInDuo(targetPlayer))
                        return noDuoTarget(sender);

                    Duo duo = plugin.getDuoHandler().getDuo(targetPlayer);

                    if (plugin.getDuoTimerHandler().hasTimer(duo)) {
                        time = plugin.convertTime(plugin.getDuoTimerHandler().getTimer(duo));

                        plugin.sendMessage(sender, "&a" + targetPlayer +
                                " and " + duo.getOther(targetPlayer) + "'s current time: &e" + time);
                    } else
                        plugin.sendMessage(sender, "&c" + targetPlayer + "'s duo time not found.");
                }

                return true;
            } else if (args[0].equalsIgnoreCase("reset")) {
                if (!sender.hasPermission("duospeedrun.reset.other"))
                    return noPerms(sender);

                if (target == null) {
                    plugin.sendMessage(sender, "&cPlayer not found.");
                    return true;
                }

                if (!plugin.getDuoHandler().isInDuo(target.getName()))
                    return noDuoTarget(sender);

                Duo duo = plugin.getDuoHandler().getDuo(target.getName());

                if (!plugin.getDuoTimerHandler().hasTimer(duo)) {
                    plugin.sendMessage(sender, "&c" + target.getName()
                            + "'s duo does not have a timer on.");
                    return true;
                }

                plugin.getDuoTimerHandler().reset(duo);
                plugin.sendMessage(sender, "&a" + target.getName() +
                        " and " + duo.getOther(target.getName()) + "'s timer has been reset.");
                plugin.sendMessage(target, "&aYour timer has been reset by " + sender.getName());

                Player otherPlayer = duo.getOtherPlayer(target.getName());

                if (otherPlayer != null)
                    plugin.sendMessage(otherPlayer, "&aYour timer has been reset by "
                            + sender.getName());

                for (String command : plugin.getConfig().getStringList("commandsToExecuteOnReset")) {
                    if (command != null) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                command.replaceAll("%player%", target.getName()));

                        if (otherPlayer != null)
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                    command.replaceAll("%player", otherPlayer.getName()));
                    }
                }
            } else
                plugin.sendMessage(sender, help);

            return true;
        }

        return true;
    }

    public void register() {
        PluginCommand pluginCommand = plugin.getCommand("duospeedrun");

        if (pluginCommand != null)
            pluginCommand.setExecutor(this);
    }

    private boolean noPerms(CommandSender sender) {
        plugin.sendMessage(sender, "&cYou do not have permissions to use this command.");
        return true;
    }

    private boolean noDuo(CommandSender sender) {
        plugin.sendMessage(sender, "&cYou are not in a duo.");
        return true;
    }

    private boolean noDuoTarget(CommandSender sender) {
        plugin.sendMessage(sender, "&cPlayer is not in a duo");
        return true;
    }
}
