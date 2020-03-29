package com.loriswit.shg.listeners;

import com.loriswit.shg.PlayerTracker;
import com.loriswit.shg.Shg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;

public class InteractListener implements Listener
{
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (!event.hasItem() || !event.getItem().getItemMeta().getDisplayName().equals(PlayerTracker.name))
            return;

        var player = event.getPlayer();

        Player nearestPlayer = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for (var p : Shg.getInstance().getAlivePlayers())
        {
            if (p == player)
                continue;

            var distance = p.getLocation().distanceSquared(player.getLocation());
            if (distance < shortestDistance)
            {
                shortestDistance = distance;
                nearestPlayer = p;
            }
        }

        if (nearestPlayer != null)
        {
//            Bukkit.getLogger().info(player.getName() + " is targeting " + nearestPlayer.getName());

            var target = nearestPlayer.getLocation().clone();
            target.setY(target.getY() + 1);
            var direction = target.subtract(player.getEyeLocation()).toVector();
            var location = player.getLocation().setDirection(direction);
            player.teleport(location);

            if(event.getHand() == EquipmentSlot.HAND)
                player.getInventory().getItemInMainHand().setAmount(0);
            else
                player.getInventory().getItemInOffHand().setAmount(0);

            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    player.getInventory().addItem(PlayerTracker.item());
                    this.cancel();
                }
                // 10 sec countdown
            }.runTaskTimer(Shg.getInstance(), 200, 0);
        }
    }
}
