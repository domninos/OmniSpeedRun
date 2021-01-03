package net.omni.speedrun.handlers.duos;

import net.omni.speedrun.SpeedRunPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class DuoHandler {
    private final SpeedRunPlugin plugin;
    private final Set<Duo> duos = new HashSet<>();
    private final Map<Player, Player> invites = new HashMap<>();

    public DuoHandler(SpeedRunPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadDuos() {
        duos.clear();

        List<String> players = plugin.getDuoConfig().getStringList("duos.players");

        for (String duoString : players) {
            if (duoString == null)
                continue;

            String[] split = duoString.split("\\|");
            String name = split[0];
            String otherName = split[1];

            duos.add(new Duo(name, otherName));
            plugin.sendConsole("&aLoaded duo of " + name + " and " + otherName);
        }
    }

    public void setDuo(Player player, Player otherPlayer) {
        if (isInDuo(player.getName()) || isInDuo(otherPlayer.getName())) {
            plugin.sendConsole("&cCould not create duo of " + player.getName()
                    + " and " + otherPlayer.getName() + " because one of them is in a duo already.");
            return;
        }

        duos.add(new Duo(player.getName(), otherPlayer.getName()));

        List<String> players = plugin.getDuoConfig().getStringList("duos.players");

        players.add(player.getName() + "|" + otherPlayer.getName() + "|0");

        plugin.getDuoConfig().set("duos.players", players);

        plugin.sendConsole("&cDuo of " + player.getName() + " is now " + otherPlayer.getName());
    }

    public void invite(Player player, Player otherPlayer) {
        if (!isInvited(player, otherPlayer)) {
            invites.put(player, otherPlayer);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                remove(player);
                plugin.sendMessage(player, "&cInvitation expired for " + otherPlayer.getName());
                plugin.sendMessage(otherPlayer, "&c" + player.getName() + "'s invitation expired.");
            }, 20 * 15);
        }
    }

    public void remove(Player player) {
        invites.remove(player);

        // removes invites if their invited player is null
        invites.entrySet().stream().filter(set -> set.getValue() == null).
                forEach(value -> invites.remove(value.getKey()));
    }

    public void accept(Player player, Player invited) {
        if (!isInvited(player, invited))
            return;

        remove(player);
        plugin.getDuoHandler().setDuo(player, invited);
    }

    public void leave(Player player) {
        if (isInDuo(player.getName())) {
            Duo duo = getDuo(player.getName());
            Player otherPlayer = duo.getOtherPlayer(player.getName());

            plugin.getDuoTimerHandler().reset(duo);

            plugin.sendMessage(player, "&aDuo has been disbanded.");

            if (otherPlayer != null)
                plugin.sendMessage(otherPlayer, "&aDuo has been disbanded.");

            duos.remove(duo);

            List<String> players = plugin.getDuoConfig().getStringList("duos.players");

            String duoString = plugin.getDuoTimerHandler().getDuoStringInConfig(duo);

            players.remove(duoString);

            plugin.getDuoConfig().set("duos.players", players);
        }
    }

    public boolean isInvited(Player player, Player otherPlayer) {
        return invites.containsKey(player) && invites.get(player) != null
                && invites.get(player).equals(otherPlayer);
    }

    public boolean isInvited(Player player) {
        return invites.containsValue(player);
    }

    public boolean isInDuo(String player) {
        return duos.stream().anyMatch(duo ->
                duo.getName().equalsIgnoreCase(player) || duo.getOtherName().equalsIgnoreCase(player));
    }

    public Duo getDuo(String player) {
        return duos.stream().filter(duo -> duo.getName().equalsIgnoreCase(player) ||
                duo.getOtherName().equalsIgnoreCase(player)).findFirst().orElse(null);
    }

    public void flush() {
        duos.clear();
        invites.clear();
    }

}