package net.omni.speedrun;

import net.omni.speedrun.command.SpeedRunCommand;
import net.omni.speedrun.handlers.BedKillHandler;
import net.omni.speedrun.handlers.TimerHandler;
import net.omni.speedrun.handlers.TopHandler;
import net.omni.speedrun.listener.PlayerListener;
import net.omni.speedrun.listener.PlayerUseBedListener;
import net.omni.speedrun.placeholder.SpeedRunPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class SpeedRunPlugin extends JavaPlugin {

    private TimerHandler timerHandler;
    private TopHandler topHandler;
    private BedKillHandler bedKillHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.timerHandler = new TimerHandler(this);
        this.topHandler = new TopHandler(this);

        topHandler.update();

        this.bedKillHandler = new BedKillHandler();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new SpeedRunPlaceholder(this).register();
            sendConsole("&aSuccessfully registered placeholder.");
        }

        new PlayerListener(this).register();
        new PlayerUseBedListener(this).register();
        new SpeedRunCommand(this).register();

        sendConsole("&aSuccessfully enabled OmniSpeedRun v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        timerHandler.flush();
        bedKillHandler.flush();
        sendConsole("&aSuccessfully disabled OmniSpeedRun.");
    }

    public void sendConsole(String message) {
        sendMessage(Bukkit.getConsoleSender(), message);
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(translate("&7[&9SpeedRun&7] &r" + message));
    }

    public String translate(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public TimerHandler getTimerHandler() {
        return timerHandler;
    }

    public TopHandler getTopHandler() {
        return topHandler;
    }

    public BedKillHandler getBedKillHandler() {
        return bedKillHandler;
    }
}
