package com.loriswit.shg.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryListener implements Listener
{
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event)
    {
        if(event.getInventory().getType() == InventoryType.BEACON)
            event.setCancelled(true);
    }
}
