package com.loriswit.shg.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockListener implements Listener
{
    @EventHandler
    public void onEntityDamage(BlockBreakEvent event)
    {
        if(event.getBlock().getType() == Material.EMERALD_BLOCK || event.getBlock().getType() == Material.BEACON)
            event.setDropItems(false);
    }
}
