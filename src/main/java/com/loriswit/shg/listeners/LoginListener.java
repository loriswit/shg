package com.loriswit.shg.listeners;

import com.loriswit.shg.Game;
import com.loriswit.shg.Shg;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Team;

public class LoginListener implements Listener
{
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        var player = event.getPlayer();
        player.setGameMode(GameMode.SPECTATOR);

        if (!player.hasPlayedBefore())
        {
            var board = Bukkit.getScoreboardManager().getMainScoreboard();

            Team team;
            if (board.getTeams().isEmpty())
            {
                team = board.registerNewTeam("dummy");
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            }
            else
                team = board.getTeam("dummy");

            team.addEntry(player.getName());
        }

        if (Shg.game.getState() != Game.State.INIT)
        {
            player.sendMessage(ChatColor.YELLOW + "Bienvenue " + player.getName() + ", la partie a déjà commencé !");
            player.teleport(Shg.game.getCenter());
            return;
        }

        Shg.game.initPlayer(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        var player = event.getPlayer();

        if (Shg.game.getState() == Game.State.COUNTDOWN || Shg.game.getState() == Game.State.RUNNING)
            Shg.game.kill(player, null);
    }
}
