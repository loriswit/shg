package com.loriswit.shg.listeners;

import com.loriswit.shg.PlayerTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class DropItemListener implements Listener
{
    @EventHandler
    public void onItemDrop (PlayerDropItemEvent event)
    {
        if(event.getItemDrop().getItemStack().getItemMeta().getDisplayName().equals(PlayerTracker.name))
            event.setCancelled(true);
    }
}
