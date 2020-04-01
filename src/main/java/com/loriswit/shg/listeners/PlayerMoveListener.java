package com.loriswit.shg.listeners;

import com.loriswit.shg.Game;
import com.loriswit.shg.Shg;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener
{
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (Shg.game.getState() == Game.State.INIT
                || Shg.game.getState() == Game.State.COUNTDOWN && Shg.game.getAlivePlayers().contains(event.getPlayer()))
            event.setTo(event.getFrom());
    }
}
