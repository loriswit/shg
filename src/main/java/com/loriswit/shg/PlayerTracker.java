package com.loriswit.shg;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PlayerTracker
{
    public static final String name = "Player tracker";

    public static ItemStack item()
    {
        var compass = new ItemStack(Material.BLAZE_ROD);
        var meta = compass.getItemMeta();
        meta.setDisplayName(name);
        compass.setItemMeta(meta);
        return compass;
    }
}
