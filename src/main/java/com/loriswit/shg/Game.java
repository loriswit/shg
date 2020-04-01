package com.loriswit.shg;

import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Game extends BukkitRunnable
{
    private State state;

    public enum State
    {
        INIT, COUNTDOWN, RUNNING, FINISHED
    }

    private World world;
    private List<Player> alivePlayers = new LinkedList<>();
    private Map<String, Stats> stats = new HashMap<>();

    private double worldRadius;
    private Location worldCenter;
    private Location initSpawn;

    private final List<Location> spawnLocations = new ArrayList<>();

    private final int blockPerPlayer = 32000;

    private final int gameTime = 900;
    private final int startCD = 10;
    private final int chestCD = 120;

    private int countdown = startCD;

    public Game()
    {
        var uuid = UUID.randomUUID();
        var creator = new WorldCreator("world-" + uuid).generateStructures(false)/*.seed(-293595622031852173L)*/;
        world = Bukkit.createWorld(creator);
        world.setDifficulty(Difficulty.HARD);
        world.setMonsterSpawnLimit(0);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);

        worldCenter = world.getSpawnLocation().clone();
        Bukkit.getLogger().info("Seed: " + world.getSeed());

        // avoid having the center on water
        while (world.getHighestBlockAt(worldCenter).getType() == Material.WATER)
        {
            Bukkit.getLogger().info("water not cool");
            worldCenter.add(100, 0, 100);
            worldCenter.setY(world.getHighestBlockYAt(worldCenter));
        }

        world.getWorldBorder().setCenter(worldCenter);

        initSpawn = new Location(world, 100000, 250, 0);
        initSpawn.add(-0.5, 0, -0.5);
        initSpawn.setPitch(90);

        // fill some planks
        initPlanks(true);

        for (var player : Bukkit.getOnlinePlayers())
            initPlayer(player);

        world.loadChunk(worldCenter.getChunk());

        state = State.INIT;
        alivePlayers.clear();
        stats.clear();
    }

    public State getState()
    {
        return state;
    }

    public List<Player> getAlivePlayers()
    {
        return alivePlayers;
    }

    public Location getCenter()
    {
        return worldCenter;
    }

    public void initPlayer(Player player)
    {
        player.teleport(initSpawn);

        player.setGameMode(GameMode.SPECTATOR);
        player.setLevel(0);
        player.setExp(0);

        var console = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(console, "title " + player.getName() + " times 0 2000000000 20");
        Bukkit.dispatchCommand(console, "title " + player.getName() + " subtitle {\"text\":\"La partie va bientôt commencer\"}");
        Bukkit.dispatchCommand(console, "title " + player.getName() + " title {\"text\":\"Hunger Games\"}");
    }

    public void startCountdown()
    {
        if (state != State.INIT)
            return;

        if (Bukkit.getOnlinePlayers().size() < 2)
        {
            Bukkit.getLogger().warning("Pas assez de joueurs");
            return;
        }

        state = State.COUNTDOWN;

        alivePlayers.addAll(Bukkit.getOnlinePlayers());
        worldRadius = Math.sqrt(alivePlayers.size() * blockPerPlayer) / 2;

        for (var player : alivePlayers)
            stats.put(player.getName(), new Stats());

        Thread thread = new Thread(this::computeSpawnLocations);
        thread.start();

        runTaskTimer(Shg.getInstance(), 0, 20);
    }

    public void kill(Player player, Player killer)
    {
        if(!alivePlayers.contains(player))
            return;

        stats.get(player.getName()).rank = alivePlayers.size();
        alivePlayers.remove(player);

        if (killer != null)
            stats.get(killer.getName()).kills++;

        if (alivePlayers.size() > 1)
        {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 100000, 1);
            Bukkit.broadcastMessage(ChatColor.YELLOW + "Il reste " + alivePlayers.size() + " joueurs");
        }
        else if (state == State.RUNNING)
            finish();
    }

    public void deleteWorld()
    {
        for (var player : Bukkit.getOnlinePlayers())
            if (player.getWorld() == world)
                player.kickPlayer("This world no longer exists");

        Bukkit.unloadWorld(world, false);
        Util.deleteDirectory(new File(world.getName()));
    }

    @Override
    public void run()
    {
        if (state == State.COUNTDOWN && countdown > 0)
            countdownStep();

        else if (state == State.COUNTDOWN && countdown == 0)
            startGame();

        else if (state == State.RUNNING && countdown == 0)
            spawnChest();

        --countdown;
    }

    private void countdownStep()
    {
        var console = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(console, "title @a title {\"text\":\"" + countdown + "\"}");

        for (var player : Bukkit.getOnlinePlayers())
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 10, 1);
    }

    private void startGame()
    {
        try
        {
            synchronized (spawnLocations)
            {
                if (spawnLocations.size() < alivePlayers.size())
                    spawnLocations.wait();
            }
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        initPlanks(false);

        var console = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(console, "title @a clear");

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "La partie a débuté !");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Il y a " + alivePlayers.size() + " joueurs");

        world.getWorldBorder().setSize(worldRadius * 2);
        world.getWorldBorder().setSize(1, gameTime);

        world.setStorm(false);
        world.setThundering(false);
        world.setTime(1000);

        int index = 0;
        for (var player : alivePlayers)
        {
            player.teleport(spawnLocations.get(index));

            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(5);

            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 1);

            ++index;
        }

        countdown = chestCD;
        state = State.RUNNING;

        // in case all players left during countdown
        if (alivePlayers.size() < 2)
            finish();
    }

    private void computeSpawnLocations()
    {
        synchronized (spawnLocations)
        {
            Bukkit.getLogger().info("Computing spawn locations...");

            var safetyDist = 80;
            var spawnRadius = worldRadius - safetyDist;

            Bukkit.getLogger().info("player count: " + alivePlayers.size());
            Bukkit.getLogger().info("safety dist: " + safetyDist);
            Bukkit.getLogger().info("spawn dist: " + spawnRadius);

            for (var i = 0; i < alivePlayers.size(); ++i)
            {
                Location loc = null;
                var ok = false;
                var dist = safetyDist;

                while (!ok)
                {
                    var x = ThreadLocalRandom.current().nextDouble(-spawnRadius, spawnRadius);
                    var z = ThreadLocalRandom.current().nextDouble(-spawnRadius, spawnRadius);

                    loc = worldCenter.clone().add(x, 0, z);
                    loc.setY(world.getHighestBlockYAt(loc));

                    if (loc.getBlock().getType().isSolid())
                    {
                        ok = true;
                        for (var l : spawnLocations)
                            if (dist > 0 && loc.distanceSquared(l) < dist * dist)
                            {
                                ok = false;
                                break;
                            }
                    }

                    if (!ok)
                        dist -= 5;
                }

                // center on block
                loc.add(-0.5, 1, -0.5);
                loc.setYaw(i * 90);
                spawnLocations.add(loc);
            }

            Bukkit.getLogger().info("Spawn locations computed.");
            spawnLocations.notify();
        }
    }

    private void initPlanks(boolean fill)
    {
        var radius = 10;
        var location = initSpawn.clone().add(-radius, -1, -radius);
        for (var x = 0; x <= radius * 2; ++x)
            for (var z = 0; z <= radius * 2; ++z)
                location.clone().add(x, 0, z).getBlock().setType(fill ? Material.SPRUCE_PLANKS : Material.AIR);
    }

    private void spawnChest()
    {
        var radius = world.getWorldBorder().getSize() / 2 - 50;
        if (radius < 0)
            return;

        Bukkit.getLogger().info("Chest radius: " + radius);

        var x = ThreadLocalRandom.current().nextDouble(-radius, radius);
        var z = ThreadLocalRandom.current().nextDouble(-radius, radius);
        var location = worldCenter.clone().add(x, 0, z);
        location.setY(world.getHighestBlockYAt(location) + 1);

        Bukkit.getLogger().info("X, Z : " + x + ", " + z);
        Bukkit.getLogger().info("Chest location: " + location.getBlockX() + ", " + location.getBlockZ());
        Bukkit.getLogger().info("World center: " + worldCenter.getBlockX() + ", " + worldCenter.getBlockZ());
        Bukkit.getLogger().info("Border center: " + world.getWorldBorder().getCenter().getBlockX() + ", " + world.getWorldBorder().getCenter().getBlockZ());

        location.getBlock().setType(Material.BEACON);
        for (x = 0; x < 3; ++x)
            for (z = 0; z < 3; ++z)
                location.clone().add(x - 1, -1, z - 1).getBlock().setType(Material.EMERALD_BLOCK);

        var block = location.add(0, 0, -1).getBlock();
        block.setType(Material.CHEST);

        var chest = (Chest) block.getState();
        for (var item : BonusChest.randomItems())
            chest.getInventory().addItem(item);

        Bukkit.broadcastMessage(ChatColor.YELLOW + "Un coffre est apparu !");
        world.playSound(location, Sound.EVENT_RAID_HORN, 100000, 1);

        countdown = chestCD;
    }

    private void finish()
    {
        state = State.FINISHED;

        if (alivePlayers.size() > 0)
        {
            var winner = alivePlayers.get(0);
            var s = stats.get(winner.getName());
            s.rank = 1;

            var console = Bukkit.getConsoleSender();
            Bukkit.dispatchCommand(console, "title " + winner.getName() + " times 0 100 20");
            Bukkit.dispatchCommand(console, "title " + winner.getName() + " title {\"text\":\"Tu as gagné !\"}");
            Bukkit.broadcastMessage(ChatColor.RED + winner.getName() + " a gagné ! (" + s.kills + " kills)");

            Bukkit.getLogger().info("Fin de la partie");

            // play victory music
            world.playSound(winner.getLocation(), Sound.ITEM_TOTEM_USE, 100000, 1);
            world.playSound(winner.getLocation(), Sound.MUSIC_DISC_CHIRP, 100000, 1);
        }

//        try
//        {
//            var writer = new PrintWriter("results-" + new java.util.Date().getTime() + ".csv");
//            for (var stat : stats.entrySet())
//                writer.println(stat.getKey() + "," + stat.getValue().rank + "," + stat.getValue().kills);
//            writer.close();
//        } catch (FileNotFoundException e)
//        {
//            e.printStackTrace();
//        }

        cancel();

        // start game 5 seconds later
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                Shg.getInstance().newGame();
                cancel();
            }
        }.runTaskTimer(Shg.getInstance(), 100, 0);
    }
}
