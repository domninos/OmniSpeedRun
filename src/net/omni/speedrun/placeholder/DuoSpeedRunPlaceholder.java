package net.omni.speedrun.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.omni.speedrun.SpeedRunPlugin;
import net.omni.speedrun.handlers.duos.Duo;
import org.bukkit.entity.Player;

public class DuoSpeedRunPlaceholder extends PlaceholderExpansion {
    private final SpeedRunPlugin plugin;

    public DuoSpeedRunPlaceholder(SpeedRunPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "duospeedrun";
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
                    top = plugin.getDuoTopHandler().getTop(1);
                    break;
                case "top_2":
                    top = plugin.getDuoTopHandler().getTop(2);
                    break;
                case "top_3":
                    top = plugin.getDuoTopHandler().getTop(3);
                    break;
                case "top_4":
                    top = plugin.getDuoTopHandler().getTop(4);
                    break;
                case "top_5":
                    top = plugin.getDuoTopHandler().getTop(5);
                    break;
            }

            if (top == null)
                top = "Unavailable";

            return top;
        } else if (params.equals("timer")) {
            if (!plugin.getDuoHandler().isInDuo(player.getName()))
                return "Could not find duo";

            Duo duo = plugin.getDuoHandler().getDuo(player.getName());

            if (plugin.getDuoTimerHandler().hasTimer(duo))
                return plugin.convertTime(plugin.getDuoTimerHandler().getTimer(duo));
            else if (plugin.getDuoTimerHandler().isInConfig(duo))
                return plugin.convertTime(plugin.getDuoTimerHandler().getTimeInConfig(duo));
        } else if (params.equals("duo")) {
            if (!plugin.getDuoHandler().isInDuo(player.getName()))
                return "Could not find duo";

            Duo duo = plugin.getDuoHandler().getDuo(player.getName());

            return duo.getOther(player.getName());
        }

        return "Unavailable";
    }
}
