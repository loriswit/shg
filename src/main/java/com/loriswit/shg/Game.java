package com.loriswit.shg;

import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Game
{
    private State state;

    public enum State
    {
        INIT, COUNTDOWN, RUNNING, FINISHED
    }

    private World world;
    private List<Player> alivePlayers = new LinkedList<>();
    private Map<String, Stats> stats = new HashMap<>();
    private final List<Location> spawnLocations = new ArrayList<>();

    private double arenaRadius;
    private Location arenaCenter;
    private Location initSpawn;

    private int chestCount = 0;
    private boolean justStarted = false;

    private final int blockPerPlayer = 33000;
    private final int arenaSpan = (int) (Math.sqrt(blockPerPlayer) * 50);
    private final int worldLimit = 29000000;

    private final int gameTime = 1000;
    private final int startCountdown = 10;
    private final int firstChestCountdown = 240;
    private final int chestCountdown = 120;
    private final int nextCountdown = 15;

    private Countdown countdown;

    public Game()
    {
        var creator = new WorldCreator("arena").generateStructures(false);
        world = Bukkit.createWorld(creator);
        world.setDifficulty(Difficulty.HARD);
        world.setMonsterSpawnLimit(0);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setTime(1000);

        Bukkit.getLogger().info("Seed: " + world.getSeed());

        initSpawn = new Location(world, -0.5, 250, -0.5, 0, 90);
        arenaCenter = new Location(world, -worldLimit, 0, arenaSpan);
        arenaCenter.setY(world.getHighestBlockYAt(arenaCenter));

        // fill some planks
        initPlanks(true);

        init();
    }

    public State getState()
    {
        return state;
    }

    public boolean hasJustStarted()
    {
        return justStarted;
    }

    public List<Player> getAlivePlayers()
    {
        return alivePlayers;
    }

    public Location getCenter()
    {
        return arenaCenter;
    }

    public void init()
    {
        // move arena center
        arenaCenter.add(arenaSpan, 0, 0);
        if (arenaCenter.getBlockX() > worldLimit)
        {
            arenaCenter.add(0, arenaSpan, 0);
            arenaCenter.setX(arenaSpan - worldLimit);
        }

        world.getWorldBorder().reset();
        world.loadChunk(arenaCenter.getChunk());

        for (var player : Bukkit.getOnlinePlayers())
            initPlayer(player);

        if (countdown != null)
            countdown.cancel();

        state = State.INIT;
        alivePlayers.clear();
        spawnLocations.clear();
        stats.clear();
        MiniMap.clear();
    }

    public void initPlayer(Player player)
    {
        player.teleport(initSpawn);
        player.setGameMode(GameMode.SPECTATOR);
        player.getInventory().clear();
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
        arenaRadius = Math.sqrt(alivePlayers.size() * blockPerPlayer) / 2;

        for (var player : alivePlayers)
            stats.put(player.getName(), new Stats());

        new Thread(this::computeSpawnLocations).start();
        new Thread(this::createMiniMap).start();

        countdown = new Countdown(startCountdown);
        countdown.onStep(this::countdownStep);
        countdown.onFinished(this::startGame);
    }

    public void kill(Player player, Player killer)
    {
        if (!alivePlayers.contains(player))
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

    public void finish()
    {
        state = State.FINISHED;

        if (alivePlayers.size() > 1)
        {
            init();
            return;
        }

        if (alivePlayers.size() == 1)
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

        countdown.cancel();
        countdown = new Countdown(nextCountdown);
        countdown.onFinished(this::init);

        world.getWorldBorder().setSize(world.getWorldBorder().getSize());
    }

    public void deleteWorld()
    {
        for (var player : Bukkit.getOnlinePlayers())
            if (player.getWorld() == world)
                player.kickPlayer("This world no longer exists");

        Bukkit.unloadWorld(world, false);
        Util.deleteDirectory(new File(world.getName()));
    }

    private void countdownStep()
    {
        var console = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(console, "title @a title {\"text\":\"" + countdown.value() + "\"}");

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

        var console = Bukkit.getConsoleSender();
        Bukkit.dispatchCommand(console, "title @a clear");

        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "La partie a débuté !");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Il y a " + alivePlayers.size() + " joueurs");

        world.getWorldBorder().setCenter(arenaCenter);
        world.getWorldBorder().setSize(arenaRadius * 2);
        world.getWorldBorder().setSize(1, gameTime);

        world.setStorm(false);
        world.setThundering(false);
        world.setTime(1000);

        var mapView = Bukkit.createMap(world);
        mapView.addRenderer(new MiniMap());

        ItemStack miniMap = new ItemStack(Material.FILLED_MAP);
        var meta = ((MapMeta) miniMap.getItemMeta());
        meta.setMapView(mapView);
        miniMap.setItemMeta(meta);

        int index = 0;
        for (var player : alivePlayers)
        {
            player.teleport(spawnLocations.get(index));
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(5);
            player.setGameMode(GameMode.SURVIVAL);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 10, 1);
            player.stopSound(Sound.MUSIC_DISC_CHIRP);
            player.getInventory().setItemInOffHand(miniMap);

            ++index;
        }

        countdown = new Countdown(firstChestCountdown);
        countdown.onFinished(this::spawnChest);

        state = State.RUNNING;
        justStarted = true;

        new Countdown(5).onFinished(() -> justStarted = false);

        // in case all players left during countdown
        if (alivePlayers.size() < 2)
            finish();
    }

    private void createMiniMap()
    {
        MiniMap.generateWaterMap(arenaCenter, arenaRadius);
    }

    private void computeSpawnLocations()
    {
        synchronized (spawnLocations)
        {
            Bukkit.getLogger().info("Computing spawn locations...");

            var safetyDist = 80;
            var spawnRadius = arenaRadius - safetyDist;

            Bukkit.getLogger().info("Player count: " + alivePlayers.size());
            Bukkit.getLogger().info("Safety dist: " + safetyDist);
            Bukkit.getLogger().info("Spawn dist: " + spawnRadius);

            for (var i = 0; i < alivePlayers.size(); ++i)
            {
                Location loc = null;
                var ok = false;
                var dist = safetyDist;

                while (!ok)
                {
                    var x = ThreadLocalRandom.current().nextDouble(-spawnRadius, spawnRadius);
                    var z = ThreadLocalRandom.current().nextDouble(-spawnRadius, spawnRadius);

                    loc = arenaCenter.clone().add(Math.round(x), 0, Math.round(z));
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
                    {
                        dist -= 5;
                        Bukkit.getLogger().warning("Bad spawn location");
                    }
                }

                // center on block
                loc.add(-0.5, 1, -0.5);
                loc.setYaw(i * 90);
                spawnLocations.add(loc);
            }

            Bukkit.getLogger().info("Spawn locations computed!");
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
        var location = arenaCenter.clone().add(x, 0, z);
        location.setY(world.getHighestBlockYAt(location) + 2);

        Bukkit.getLogger().info("X, Z : " + x + ", " + z);
        Bukkit.getLogger().info("Chest location: " + location.getBlockX() + ", " + location.getBlockZ());
        Bukkit.getLogger().info("World center: " + arenaCenter.getBlockX() + ", " + arenaCenter.getBlockZ());
        Bukkit.getLogger().info("Border center: " + world.getWorldBorder().getCenter().getBlockX() + ", " + world.getWorldBorder().getCenter().getBlockZ());

        Material glass;
        switch (Math.abs(arenaCenter.getBlockX() + chestCount++) % 8)
        {
            case 0:
                glass = Material.RED_STAINED_GLASS;
                break;
            case 1:
                glass = Material.BLUE_STAINED_GLASS;
                break;
            case 2:
                glass = Material.GREEN_STAINED_GLASS;
                break;
            case 3:
                glass = Material.YELLOW_STAINED_GLASS;
                break;
            case 4:
                glass = Material.CYAN_STAINED_GLASS;
                break;
            case 5:
                glass = Material.MAGENTA_STAINED_GLASS;
                break;
            case 6:
                glass = Material.BLACK_STAINED_GLASS;
                break;
            default:
                glass = Material.WHITE_STAINED_GLASS;
                break;
        }
        location.getBlock().setType(glass);
        location.add(0, -1, 0).getBlock().setType(Material.BEACON);
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

        countdown = new Countdown(chestCountdown);
        countdown.onFinished(this::spawnChest);
    }
}
