package net.omni.speedrun;

import net.omni.speedrun.command.DuoSpeedRunCommand;
import net.omni.speedrun.command.SpeedRunCommand;
import net.omni.speedrun.handlers.BedKillHandler;
import net.omni.speedrun.handlers.duos.DuoTopHandler;
import net.omni.speedrun.handlers.TimerHandler;
import net.omni.speedrun.handlers.TopHandler;
import net.omni.speedrun.handlers.duos.DuoConfig;
import net.omni.speedrun.handlers.duos.DuoHandler;
import net.omni.speedrun.handlers.duos.DuoTimerHandler;
import net.omni.speedrun.listener.PlayerListener;
import net.omni.speedrun.listener.PlayerUseBedListener;
import net.omni.speedrun.placeholder.DuoSpeedRunPlaceholder;
import net.omni.speedrun.placeholder.SpeedRunPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public class SpeedRunPlugin extends JavaPlugin {

    private TimerHandler timerHandler;
    private TopHandler topHandler;

    private BedKillHandler bedKillHandler;

    private DuoTimerHandler duoTimerHandler;
    private DuoConfig duoConfig;
    private DuoHandler duoHandler;
    private DuoTopHandler duoTopHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.duoConfig = new DuoConfig(this, "duos.yml");

        this.timerHandler = new TimerHandler(this);
        this.topHandler = new TopHandler(this);

        topHandler.update();

        this.duoTimerHandler = new DuoTimerHandler(this);
        this.duoHandler = new DuoHandler(this);

        duoHandler.loadDuos();

        this.duoTopHandler = new DuoTopHandler(this);

        duoTopHandler.update();

        this.bedKillHandler = new BedKillHandler();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new SpeedRunPlaceholder(this).register();
            new DuoSpeedRunPlaceholder(this).register();
            sendConsole("&aSuccessfully registered placeholders.");
        }

        new PlayerListener(this).register();
        new PlayerUseBedListener(this).register();
        new SpeedRunCommand(this).register();
        new DuoSpeedRunCommand(this).register();

        sendConsole("&aDuo server: " + getConfig().getBoolean("duoServer"));
        sendConsole("&aSuccessfully enabled OmniSpeedRun v" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        timerHandler.flush();
        duoTimerHandler.flush();
        duoHandler.flush();
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

    public String convertTime(int time) {
        int day = (int) TimeUnit.SECONDS.toDays(time);
        long hours = TimeUnit.SECONDS.toHours(time) - (day * 24L);
        long minute = TimeUnit.SECONDS.toMinutes(time) - (TimeUnit.SECONDS.toHours(time) * 60);
        long second = TimeUnit.SECONDS.toSeconds(time) - (TimeUnit.SECONDS.toMinutes(time) * 60);

        String converted = "";

        if (day > 0)
            converted += day + "d ";

        if (hours > 0)
            converted += hours + "h ";

        if (minute > 0)
            converted += minute + "m ";

        if (second > 0)
            converted += second + "s ";

        if (converted.isEmpty())
            converted += "0";

        return converted;
    }

    public TimerHandler getTimerHandler() {
        return timerHandler;
    }

    public DuoTimerHandler getDuoTimerHandler() {
        return duoTimerHandler;
    }

    public DuoHandler getDuoHandler() {
        return duoHandler;
    }

    public TopHandler getTopHandler() {
        return topHandler;
    }

    public BedKillHandler getBedKillHandler() {
        return bedKillHandler;
    }

    public DuoConfig getDuoConfig() {
        return duoConfig;
    }

    public DuoTopHandler getDuoTopHandler() {
        return duoTopHandler;
    }
}
