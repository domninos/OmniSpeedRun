package net.omni.speedrun.handlers.duos;

import net.omni.speedrun.SpeedRunPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuoTimerHandler {
    private final Map<Duo, Integer> timers = new HashMap<>();
    private final SpeedRunPlugin plugin;
    private final int timerID;

    public DuoTimerHandler(SpeedRunPlugin plugin) {
        this.plugin = plugin;

        this.timerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Map.Entry<Duo, Integer> entry : timers.entrySet()) {
                Duo duo = entry.getKey();

                Player player = duo.getPlayer();
                Player other = duo.getOther();

                if (player == null || other == null) {
                    stopTimer(duo);
                    continue;
                }

                entry.setValue(entry.getValue() + 1);
            }
        }, 20L, 20L);
    }

    /**
     * Starts the timer for the duo.
     *
     * @param duo - {@link Duo} to start the timer
     */
    public void startTimer(Duo duo) {
        if (!isInConfig(duo)) {
            plugin.sendConsole("&aCreating timer for " + duo.getName() + " and " + duo.getOtherName());
            timers.put(duo, 0);
            save(duo);
        } else {
            timers.put(duo, getTimeInConfig(duo));
            plugin.sendConsole("&aContinued timer of " + duo.getName() + " and " + duo.getOtherName());
        }
    }

    /**
     * Stops and saves the timer for the duo.
     *
     * @param duo - {@link Duo} to stop the timer
     */
    public void stopTimer(Duo duo) {
        save(duo);
        timers.remove(duo);
    }

    /**
     * Adds the duo to the finished list in config.
     *
     * @param duo {@link Duo} finished duo
     */
    public void finish(Duo duo) {
        if (timers.containsKey(duo)) {
            List<String> finished = plugin.getDuoConfig().getStringList("duos.finished");

            finished.add(duo.getTeamInConfig() + "|" + timers.get(duo));

            plugin.getDuoConfig().set("duos.finished", finished);

            List<String> players = plugin.getDuoConfig().getStringList("duos.players");

            players.remove(duo.getTeamInConfig() + "|" + getTimeInConfig(duo));

            plugin.getDuoConfig().set("duos.players", players);

            plugin.getDuoConfig().save();

            timers.remove(duo);
        }
    }

    /**
     * Restarts/resets a duos' timer.
     *
     * @param duo {@link Duo} player
     */
    public void reset(Duo duo) {
        if (timers.containsKey(duo)) {
            List<String> players = plugin.getDuoConfig().getStringList("duos.players");

            players.remove(duo.getTeamInConfig() + "|" + getTimeInConfig(duo));

            plugin.getDuoConfig().set("duos.players", players);
            plugin.getDuoConfig().save();

            timers.remove(duo);
        }
    }

    /**
     * Saves current time to config.
     *
     * @param duo - {@link Duo} to save
     */
    public void save(Duo duo) {
        if (timers.containsKey(duo)) {
            String foundDuo = getDuoStringInConfig(duo);

            if (foundDuo.isEmpty()) {
                plugin.sendConsole("&cCould not find duo of " + duo.getTeamInConfig());
                return;
            }

            List<String> players = plugin.getDuoConfig().getStringList("duos.players");

            players.remove(foundDuo);
            players.add(duo.getTeamInConfig() + "|" + timers.get(duo));

            plugin.getDuoConfig().set("duos.players", players);
            plugin.getDuoConfig().save();
        }
    }

    /**
     * Saves all from cache to config
     */
    public void save() {
        for (Map.Entry<Duo, Integer> entry : timers.entrySet())
            save(entry.getKey());
    }

    /**
     * Use this only if the duo is online.
     *
     * @param duo - {@link Duo} of player
     * @return true - if duo is in cache
     */
    public boolean hasTimer(Duo duo) {
        return timers.containsKey(duo);
    }

    /**
     * Use this only if the duo is in config.
     *
     * @param duo - {@link Duo} of to check
     * @return true - if uuid is in config
     */
    public boolean isInConfig(Duo duo) {
        List<String> players = plugin.getDuoConfig().getStringList("duos.players");

        for (String key : players) {
            if (key == null)
                continue;

            if (key.contains(duo.getTeamInConfig()))
                return true;
        }

        return false;
    }

    /**
     * Returns the time from config.
     *
     * @param duo - {@link Duo} to check from config
     * @return {@code Integer} the time from config
     */
    public int getTimeInConfig(Duo duo) {
        if (!isInConfig(duo))
            return 0;

        String foundDuo = getDuoStringInConfig(duo);

        if (foundDuo.isEmpty()) {
            plugin.sendConsole("&cCould not find duo from " + duo.getTeamInConfig());
            return 0;
        }

        String[] split = foundDuo.split("\\|");

        return Integer.parseInt(split[2]);
    }

    /**
     * Returns the timer of a duo in cache
     *
     * @param duo - {@link Duo} to get timer
     * @return {@code Integer} the time stored in cache
     */
    public int getTimer(Duo duo) {
        return timers.getOrDefault(duo, 0);
    }

    /**
     * Returns the duo string in config
     *
     * @param duo - {@link Duo} to get the string
     * @return {@code String} duo string
     */
    public String getDuoStringInConfig(Duo duo) {
        List<String> players = plugin.getDuoConfig().getStringList("duos.players");
        String foundDuo = "";

        for (String duoPlayers : players) {
            if (duoPlayers == null)
                continue;

            if (duoPlayers.contains(duo.getTeamInConfig())) {
                foundDuo = duoPlayers;
                break;
            }
        }

        return foundDuo;
    }


    /**
     * Stops the timer task and uses the save method.
     */
    public void flush() {
        if (Bukkit.getScheduler().isCurrentlyRunning(timerID))
            Bukkit.getScheduler().cancelTask(timerID);

        save();
        timers.clear();
    }
}
