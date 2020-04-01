package com.loriswit.shg;

import com.loriswit.shg.commands.EndCommand;
import com.loriswit.shg.commands.StartCommand;
import com.loriswit.shg.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Shg extends JavaPlugin
{
    public static Game game = null;

    private static Shg instance = null;

    public static Shg getInstance()
    {
        return instance;
    }

    public void newGame()
    {
        var previous = game;
        game = new Game();

        // delete old world 10 seconds later
        if (previous != null)
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    previous.deleteWorld();
                    cancel();
                }
            }.runTaskTimer(this, 200, 0);
    }

    @Override
    public void onEnable()
    {
        instance = this;
        getLogger().info("SHG started");
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new LoginListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        getServer().getPluginManager().registerEvents(new InteractListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);

        getCommand("start").setExecutor(new StartCommand());
        getCommand("end").setExecutor(new EndCommand());

        newGame();
    }

    @Override
    public void onDisable()
    {
        getLogger().info("SHG stopped!");

        game.deleteWorld();
        if (game.getState() != Game.State.INIT)
            game.cancel();
    }
}
