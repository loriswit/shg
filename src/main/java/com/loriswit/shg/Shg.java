package com.loriswit.shg;

import com.loriswit.shg.commands.ResetCommand;
import com.loriswit.shg.commands.StartCommand;
import com.loriswit.shg.listeners.*;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class Shg extends JavaPlugin
{
    private static Shg instance = null;
    private boolean aboutToStart = false;
    private boolean started = false;
    private boolean pvp = false;
    private boolean finished = false;

    private Game game = null;
    private World world = null;
    private List<Player> alivePlayers = new LinkedList<>();
    private Map<String, Stats> stats = new HashMap<>();

    public static Shg getInstance()
    {
        return instance;
    }

    @Override
    public void onEnable()
    {
        instance = this;
        getLogger().info("SHG started");
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new LoginListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        getServer().getPluginManager().registerEvents(new DropItemListener(), this);
        getServer().getPluginManager().registerEvents(new InteractListener(), this);

        getCommand("start").setExecutor(new StartCommand());
        getCommand("reset").setExecutor(new ResetCommand());

        restartGame();
    }

    public boolean deleteDirectory(File directoryToBeDeleted)
    {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null)
            for (File file : allContents)
                deleteDirectory(file);

        return directoryToBeDeleted.delete();
    }

    @Override
    public void onDisable()
    {
        getLogger().info("SHG stopped!");
    }

    public void restartGame()
    {
        if(game != null)
            game.cancel();

        var oldWorld = world;

        var uuid = UUID.randomUUID();
        var creator = new WorldCreator("world-" + uuid).generateStructures(false);
        world = Bukkit.createWorld(creator);
        world.setDifficulty(Difficulty.HARD);
        world.setMonsterSpawnLimit(0);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);

        for (var player : Bukkit.getOnlinePlayers())
        {
            LoginListener.resetPlayer(player);
            for (var p : Bukkit.getOnlinePlayers())
                player.showPlayer(Shg.getInstance(), p);
        }

        world.loadChunk(world.getSpawnLocation().getChunk());

        if (oldWorld != null)
        {
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    Bukkit.unloadWorld(oldWorld.getName(), false);
                    deleteDirectory(new File(oldWorld.getName()));
                    this.cancel();
                }
                // 10 sec delay
            }.runTaskTimer(Shg.getInstance(), 200, 0);
        }

        aboutToStart = false;
        started = false;
        pvp = false;
        finished = false;
        alivePlayers.clear();
        stats.clear();

        PlayerMoveListener.allowMoves(false);
    }

    public void startCountdown()
    {
        if (aboutToStart)
            return;

        if (Bukkit.getOnlinePlayers().size() < 2)
        {
            Bukkit.getLogger().warning("Pas assez de joueurs");
            return;
        }

        for (var player : Bukkit.getOnlinePlayers())
            player.stopSound(Sound.MUSIC_DISC_CHIRP);

        aboutToStart = true;
        alivePlayers.addAll(Bukkit.getOnlinePlayers());
        game = new Game();
        game.runTaskTimer(this, 20, 20);
    }

    public boolean isAboutToStart()
    {
        return aboutToStart;
    }

    public void startGame()
    {
        started = true;
    }

    public boolean hasStarted()
    {
        return started;
    }

    public void enablePvp()
    {
        pvp = true;
    }

    public boolean isPvpEnabled()
    {
        return pvp;
    }

    public boolean isFinished()
    {
        return finished;
    }

    public List<Player> getAlivePlayers()
    {
        return alivePlayers;
    }

    public void addStats(Entity entity)
    {
        stats.put(entity.getName(), new Stats());
    }

    public Stats getStats(Entity entity)
    {
        return stats.get(entity.getName());
    }

    public World getWorld()
    {
        return world;
    }

    public void finish()
    {
        finished = true;

        try
        {
            var writer = new PrintWriter("results-" + new java.util.Date().getTime() + ".csv");
            for (var stat : stats.entrySet())
            {
//                writer.println(stat.getKey());
//                writer.println("  rank: " + stat.getValue().rank);
//                writer.println("  kills: " + stat.getValue().kills);
//                writer.println("");
                writer.println(stat.getKey() + "," + stat.getValue().rank + "," + stat.getValue().kills);
            }
            writer.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
