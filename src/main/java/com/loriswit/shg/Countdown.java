package com.loriswit.shg;

import org.bukkit.scheduler.BukkitRunnable;

public class Countdown extends BukkitRunnable
{
    private int counter;
    private Runnable step = null;
    private Runnable after = null;

    public Countdown(int value)
    {
        counter = value;
        runTaskTimer(Shg.getInstance(), 0, 20);
    }

    public int value()
    {
        return counter;
    }

    public void onFinished(Runnable callback)
    {
        after = callback;
    }

    public void onStep(Runnable callback)
    {
        step = callback;
    }

    @Override
    public void run()
    {
        if (--counter == 0)
        {
            if (after != null)
                after.run();

            cancel();
        }

        else if (step != null)
            step.run();
    }
}
