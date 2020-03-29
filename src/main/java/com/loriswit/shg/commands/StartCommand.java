package com.loriswit.shg.commands;

import com.loriswit.shg.Shg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

public class StartCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
//        if (sender instanceof ConsoleCommandSender)
            Shg.getInstance().startCountdown();

        return true;
    }
}
