package com.loriswit.shg.listeners;

import com.loriswit.shg.Shg;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

public class LoginListener implements Listener
{
    private static int index = 0;

//    @EventHandler
//    public void onPlayerLogin(PlayerLoginEvent event)
//    {
//        if (Shg.getInstance().isAboutToStart())
//            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "La partie a déjà commencé !");
//    }

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

        if (Shg.getInstance().isAboutToStart())
        {
            player.sendMessage(ChatColor.YELLOW + "Bienvenue " + player.getName() + ", la partie a déjà commencé !");
            return;
        }

        resetPlayer(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        var player = event.getPlayer();
        player.damage(1000);
    }

    public static void resetPlayer(Player player)
    {
//        var y = player.getWorld().getHighestBlockYAt(0, 0) + 1;
        player.teleport(new Location(Shg.getInstance().getWorld(), (index++) * 20, 220, 0, 0, -90));

        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, false, false, false));
        player.setGameMode(GameMode.SPECTATOR);
        player.setLevel(0);
        player.setExp(0);

        player.sendMessage(ChatColor.YELLOW + "Bienvenue " + player.getName() + ", la partie va bientôt commencer.");

//        for (var p : Bukkit.getOnlinePlayers())
//        {
//            p.hidePlayer(Shg.getInstance(), player);
//            player.hidePlayer(Shg.getInstance(), p);
//        }
    }
}
