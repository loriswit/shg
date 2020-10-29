package com.loriswit.shg;

import com.loriswit.shg.commands.EndCommand;
import com.loriswit.shg.commands.StartCommand;
import com.loriswit.shg.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

public class Shg extends JavaPlugin
{
    public static Game game;
    private static Shg instance;

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
        getServer().getPluginManager().registerEvents(new InteractListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new ServerPingListener(), this);

        getCommand("start").setExecutor(new StartCommand());
        getCommand("end").setExecutor(new EndCommand());

        game = new Game();
    }

    @Override
    public void onDisable()
    {
        getLogger().info("SHG stopped!");
        game.deleteWorld();
    }
}
