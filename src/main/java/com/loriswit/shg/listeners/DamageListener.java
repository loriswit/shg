package com.loriswit.shg.listeners;

import com.loriswit.shg.PlayerTracker;
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
        if (event.getEntity().getType() != EntityType.PLAYER)
            return;

        if (!Shg.getInstance().hasStarted())
        {
            event.setCancelled(true);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent && !Shg.getInstance().isPvpEnabled())
            if (((EntityDamageByEntityEvent) event).getDamager().getType() == EntityType.PLAYER)
            {
                event.setCancelled(true);
                return;
            }

        var player = (Player) event.getEntity();
        if (player.getHealth() - event.getDamage() > 0)
            return;

        // if player DED
        event.setCancelled(true);
        var console = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(console, "title " + player.getName() + " times 0 100 20");

        if (event instanceof EntityDamageByEntityEvent)
        {
            var damager = ((EntityDamageByEntityEvent) event).getDamager();
            if (damager.getType() == EntityType.PLAYER || (damager.getType() == EntityType.ARROW && ((Arrow) damager).getShooter() instanceof Player))
            {
                if (damager.getType() == EntityType.ARROW)
                    damager = (Player) ((Arrow) damager).getShooter();

                var killer = damager.getName();
                Bukkit.dispatchCommand(console, "title " + player.getName() + " subtitle {\"text\":\"tué par " + killer + "\"}");
                Bukkit.broadcastMessage(ChatColor.YELLOW + killer + " a tué " + player.getName());
                Shg.getInstance().getStats(damager).kills++;
            }
            else
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Un animal a tué " + player.getName());
        }
        else
        {
            switch (event.getCause())
            {
                case SUFFOCATION:
                    Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " s'est étouffé");
                    break;
                case FALL:
                    Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " a fait une chute mortelle");
                    break;
                case FIRE:
                case FIRE_TICK:
                case LAVA:
                case HOT_FLOOR:
                    Bukkit.broadcastMessage(ChatColor.YELLOW + player.getName() + " est mort de ses brûlures");
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
            if (item != null && !item.getItemMeta().getDisplayName().equals(PlayerTracker.name))
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

        var alive = Shg.getInstance().getAlivePlayers();
        Shg.getInstance().getStats(player).rank = alive.size();
        alive.remove(player);

        if (alive.size() == 1)
        {
            var winner = alive.get(0);
            var stats = Shg.getInstance().getStats(winner);
            stats.rank = 1;
            Bukkit.dispatchCommand(console, "title " + winner.getName() + " times 0 100 20");
            Bukkit.dispatchCommand(console, "title " + winner.getName() + " title {\"text\":\"Tu as gagné !\"}");
            Bukkit.broadcastMessage(ChatColor.RED + winner.getName() + " a gagné ! (" + stats.kills + " kills)");

            Bukkit.getLogger().info("Fin de la partie");

            Shg.getInstance().finish();

            // play victory music
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1000, 1);
            player.getWorld().playSound(player.getLocation(), Sound.MUSIC_DISC_CHIRP, 1000, 1);
        }
        else
        {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1000, 1);
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Il reste " + alive.size() + " joueurs");
        }
    }
}
