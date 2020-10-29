package com.loriswit.shg.listeners;

import com.loriswit.shg.Game;
import com.loriswit.shg.Shg;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerPingListener implements Listener
{
    @EventHandler
    public void onServerListPing(ServerListPingEvent event)
    {
        var motd = ChatColor.BLUE + "Hunger Games " + ChatColor.YELLOW;

        if (Shg.game.getState() == Game.State.INIT)
            event.setMotd(motd + "(Lobby)");
        else if (Shg.game.getState() == Game.State.FINISHED)
            event.setMotd(motd + "(Fin de partie)");
        else
            event.setMotd(motd + "(" + Shg.game.getAlivePlayers().size() + " joueurs en vie)");
    }
}
