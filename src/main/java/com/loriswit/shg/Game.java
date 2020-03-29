package com.loriswit.shg;

import com.loriswit.shg.listeners.PlayerMoveListener;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class Game extends BukkitRunnable
{
    private final int startCountdown = 10;
    private final int pvpCountdown = 300;
    private final int chestCountdown = 120;

    private int countdown = startCountdown;

    public Game()
    {
        Bukkit.broadcastMessage(ChatColor.YELLOW + "La partie va commencer dans " + countdown + "...");
        for (var player : Bukkit.getOnlinePlayers())
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 10, 1);
    }

    @Override
    public void run()
    {
        World world = Shg.getInstance().getWorld();

        if (Shg.getInstance().isFinished())
        {
            Shg.getInstance().restartGame();
            countdown = -1;
            cancel();
        }

        var players = Shg.getInstance().getAlivePlayers();

        if (countdown > 0)
            countdown--;

        if (countdown > 0 && !Shg.getInstance().hasStarted())
        {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("" + ChatColor.YELLOW + countdown + "...");
            for (var player : players)
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 10, 1);
        }
        else if (countdown == 0 && !Shg.getInstance().hasStarted())
        {
            // GAME START

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Pouf !");

            countdown = pvpCountdown;
            Bukkit.broadcastMessage(ChatColor.RED + "Vous avez " + countdown + " seconds pour vous cacher !");

            Bukkit.broadcastMessage(ChatColor.YELLOW + "Il reste " + Shg.getInstance().getAlivePlayers().size() + " joueurs");

            world.setStorm(false);
            world.setThundering(false);
            world.setTime(1000);

            int side = (int) Math.sqrt(players.size());

            int index = 0;
            for (var player : players)
            {
                if (world == null)
                    world = player.getWorld();

                player.removePotionEffect(PotionEffectType.BLINDNESS);
                PlayerMoveListener.allowMoves(true);

                var x = (index % side) * 2;
                var z = (index / side) * 2;
                var y = world.getHighestBlockYAt(x, z) + 1;
                player.teleport(new Location(world, x, y, z, index * 90, 0));

                player.setHealth(20);
                player.setFoodLevel(20);
                player.setSaturation(5);

                player.getInventory().clear();
                player.setGameMode(GameMode.SURVIVAL);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 1);

//                for (var p : Bukkit.getOnlinePlayers())
//                {
//                    player.showPlayer(Shg.getInstance(), p);
//                    p.showPlayer(Shg.getInstance(), player);
//                }

                Shg.getInstance().addStats(player);

                ++index;
            }

            Shg.getInstance().startGame();
        }
        else if (countdown == 0 && Shg.getInstance().isPvpEnabled())
        {
            // SPAWN CHEST

            var x = ThreadLocalRandom.current().nextInt(-15, 15);
            var z = ThreadLocalRandom.current().nextInt(-15, 15);
            var location = new Location(world, x, world.getHighestBlockYAt(x, z) + 1, z);
            var block = location.getBlock();
            block.setType(Material.CHEST);

            var chest = (Chest) block.getState();
            for (var item : BonusChest.randomItems())
                chest.getInventory().addItem(item);

            Bukkit.broadcastMessage(ChatColor.YELLOW + "Un coffre est apparu au centre de la map !");

            countdown = chestCountdown;
        }
        else if (countdown == 10 && !Shg.getInstance().isPvpEnabled() && Shg.getInstance().hasStarted())
        {
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Le PVP va s'activer dans " + countdown-- + "...");
            for (var player : players)
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 10, 1);
        }
        else if (countdown == 0 && !Shg.getInstance().isPvpEnabled() && Shg.getInstance().hasStarted())
        {
            // PVP START

            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Pouf !");

            Shg.getInstance().enablePvp();
            Bukkit.broadcastMessage(ChatColor.RED + "Le PVP est activé !");
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Tu as reçu un \"player tracker\"");

            for (var player : players)
            {
                player.getInventory().addItem(PlayerTracker.item());
                player.playSound(player.getLocation(), Sound.EVENT_RAID_HORN, 1000, 1);
            }

            countdown = chestCountdown;
        }
        else if (countdown < 10 && !Shg.getInstance().isPvpEnabled() && Shg.getInstance().hasStarted())
        {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("" + ChatColor.YELLOW + countdown + "...");
            for (var player : players)
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 10, 1);
        }
    }
}
