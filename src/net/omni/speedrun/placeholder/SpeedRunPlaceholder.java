package net.omni.speedrun.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.omni.speedrun.SpeedRunPlugin;
import org.bukkit.entity.Player;

public class SpeedRunPlaceholder extends PlaceholderExpansion {
    private final SpeedRunPlugin plugin;

    public SpeedRunPlaceholder(SpeedRunPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "speedrun";
    }

    @Override
    public String getAuthor() {
        return "omni";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (params.startsWith("top")) {
            String top = "";

            switch (params) {
                case "top_1":
                    top = plugin.getTopHandler().getTop(1);
                    break;
                case "top_2":
                    top = plugin.getTopHandler().getTop(2);
                    break;
                case "top_3":
                    top = plugin.getTopHandler().getTop(3);
                    break;
                case "top_4":
                    top = plugin.getTopHandler().getTop(4);
                    break;
                case "top_5":
                    top = plugin.getTopHandler().getTop(5);
                    break;
            }

            if (top == null)
                top = "Unavailable";

            return top;
        } else if (params.equals("timer")) {
            if (plugin.getTimerHandler().hasTimer(player.getName()))
                return plugin.convertTime(plugin.getTimerHandler().getTimer(player.getName()));
            else if (plugin.getTimerHandler().isInConfig(player.getName()))
                return plugin.convertTime(plugin.getTimerHandler().getTimeInConfig(player.getName()));
        }

        return "Unavailable";
    }
}
