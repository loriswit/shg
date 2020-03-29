package com.loriswit.shg.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener
{
    private static boolean movingAllowed = false;

    public static void allowMoves(boolean allow)
    {
        movingAllowed = allow;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (!movingAllowed)
        {
            event.setTo(event.getFrom());
            return;
        }

//        for (var player : Bukkit.getOnlinePlayers())
//        {
//            Player nearestPlayer = null;
//            double shortestDistance = Double.POSITIVE_INFINITY;
//            for (var p : Bukkit.getOnlinePlayers())
//            {
//                if (p == player)
//                    continue;
//
//                var distance = p.getLocation().distanceSquared(player.getLocation());
//                if (distance < shortestDistance)
//                {
//                    shortestDistance = distance;
//                    nearestPlayer = p;
//                }
//            }
//
//            if (nearestPlayer != null)
//            {
//                Bukkit.getLogger().info(player.getName() + " -> " + nearestPlayer.getName() + "(" + nearestPlayer.getLocation() + ")");
//                player.setCompassTarget(nearestPlayer.getLocation());
//            }
//        }
    }
}
