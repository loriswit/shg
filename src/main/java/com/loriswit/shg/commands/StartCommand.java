package com.loriswit.shg.commands;

import com.loriswit.shg.Game;
import com.loriswit.shg.Shg;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StartCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        if (Bukkit.getOnlinePlayers().size() < 2)
            sender.sendMessage(ChatColor.RED + "Il n'y a pas assez de joueurs");

        else if(Shg.game.getState() == Game.State.INIT)
            Shg.game.startCountdown();

        return true;
    }
}
