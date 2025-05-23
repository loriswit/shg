package com.loriswit.shg;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BonusChest
{
    private static final List<Material> ironMats = new ArrayList<>()
    {{
        add(Material.IRON_AXE);
        add(Material.IRON_SWORD);
        add(Material.IRON_SHOVEL);
        add(Material.IRON_CHESTPLATE);
        add(Material.IRON_HELMET);
        add(Material.IRON_LEGGINGS);
        add(Material.IRON_BOOTS);
    }};

    private static final List<Material> diamondMats = new ArrayList<>()
    {{
        add(Material.DIAMOND_AXE);
        add(Material.DIAMOND_SWORD);
        add(Material.DIAMOND_SHOVEL);
        add(Material.DIAMOND_CHESTPLATE);
        add(Material.DIAMOND_HELMET);
        add(Material.DIAMOND_LEGGINGS);
        add(Material.DIAMOND_BOOTS);
    }};

    private static final List<Material> foodMats = new ArrayList<>()
    {{
        add(Material.BREAD);
        add(Material.COOKED_BEEF);
        add(Material.COOKED_CHICKEN);
        add(Material.COOKED_MUTTON);
        add(Material.COOKED_MUTTON);
        add(Material.COOKED_COD);
        add(Material.COOKED_PORKCHOP);
    }};

    private static final List<Material> miscMats = new ArrayList<>()
    {{
        add(Material.FLINT_AND_STEEL);
        add(Material.LAVA_BUCKET);
    }};

    private static Random rand = new Random();

    private static <T> T randFromList(List<T> list)
    {
        return list.get(rand.nextInt(list.size()));
    }

    public static List<ItemStack> randomItems()
    {
        var items = new ArrayList<ItemStack>();

        if (Math.random() > 0.4)
            items.add(new ItemStack(randFromList(ironMats)));
        else
            items.add(new ItemStack(randFromList(diamondMats)));

        items.add(new ItemStack(randFromList(foodMats), 2));
        items.add(new ItemStack(randFromList(foodMats)));

        var bonus = Math.random();

        if (bonus <= 0.15)
            items.add(new ItemStack(randFromList(miscMats)));

        else if (bonus > 0.15 && bonus <= 0.3)
        {
            items.add(new ItemStack(Material.BOW));
            items.add(new ItemStack(Material.ARROW, 3));
        }

        else if (bonus > 0.3 && bonus <= 0.45)
        {
            var tracker = PlayerTracker.item();
            tracker.setAmount(3);
            items.add(tracker);
        }

        return items;
    }
}
