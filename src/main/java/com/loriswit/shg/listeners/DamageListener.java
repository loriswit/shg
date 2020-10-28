package com.loriswit.shg.listeners;

import com.loriswit.shg.Game;
import com.loriswit.shg.Shg;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener
{
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (Shg.game.getState() != Game.State.RUNNING)
        {
            if (Shg.game.getState() == Game.State.FINISHED)
                event.setCancelled(true);

            return;
        }

        if (event.getEntity().getType() != EntityType.PLAYER)
            return;

        var player = (Player) event.getEntity();

        // avoid players suffocating when spawning
        if(Shg.game.hasJustStarted() && event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION)
        {
            var location = player.getLocation();
            location.setY(player.getWorld().getHighestBlockYAt(location) + 1);
            player.teleport(location);
            event.setCancelled(true);
            return;
        }

        // skip if player still alive
        if (player.getHealth() - event.getDamage() > 0)
            return;

        event.setCancelled(true);
        var console = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(console, "title " + player.getName() + " times 0 100 20");

        Player killer = null;

        if (event instanceof EntityDamageByEntityEvent)
        {
            var damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager.getType() == EntityType.PLAYER || (damager.getType() == EntityType.ARROW && ((Arrow) damager).getShooter() instanceof Player))
            {
                if (damager.getType() == EntityType.ARROW)
                    killer = (Player) ((Arrow) damager).getShooter();
                else
                    killer = (Player) damager;

                Bukkit.dispatchCommand(console, "title " + player.getName() + " subtitle {\"text\":\"tué par " + killer.getName() + "\"}");
                Bukkit.broadcastMessage(ChatColor.YELLOW + killer.getName() + " a tué " + player.getName());
            }
            else
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Un mob a tué " + player.getName());
        }
        else
        {
            Bukkit.getLogger().info("Damage: " + event.getCause());
            switch (event.getCause())
            {
                case SUFFOCATION:
                    var border = player.getWorld().getWorldBorder();
                    if (player.getLocation().distanceSquared(border.getCenter()) > border.getSize() * border.getSize() / 4)
                        Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " est mort dans la tempête");
                    else
                        Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " est mort étouffé");
                    break;
                case FALL:
                    Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " a fait une chute mortelle");
                    break;
                case FIRE:
                case FIRE_TICK:
                case LAVA:
                case HOT_FLOOR:
                    Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " est mort de ses brulures");
                    break;
                case DROWNING:
                    Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " s'est noyé");
                    break;
                case BLOCK_EXPLOSION:
                    Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " a explosé");
                case ENTITY_EXPLOSION:
                    break;
                case STARVATION:
                    Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " est mort de faim");
                    break;
                case POISON:
                    Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " s'est fait empoisonné");
                    break;
                default:
                    Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " est mort tout seul");
            }
        }

        // drop all items
        for (var item : player.getInventory().getContents())
            if (item != null)
            {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                player.getInventory().remove(item);
            }

        // play death sound
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);

        // spawn smoke effect
        for (int i = 0; i < 8; i++)
            player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, i);

        Bukkit.dispatchCommand(console, "title " + player.getName() + " title {\"text\":\"Tu es mort !\"}");
        player.setGameMode(GameMode.SPECTATOR);

        Shg.game.kill(player, killer);
    }
}
