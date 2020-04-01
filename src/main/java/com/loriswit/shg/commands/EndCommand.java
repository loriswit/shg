package com.loriswit.shg.commands;

import com.loriswit.shg.Game;
import com.loriswit.shg.Shg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EndCommand implements CommandExecutor
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings)
    {
        if (Shg.game.getState() == Game.State.RUNNING)
            Shg.game.finish();

        return true;
    }
}
