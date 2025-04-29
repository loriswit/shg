package com.loriswit.shg;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.map.*;

public class MiniMap extends MapRenderer
{
    private static boolean[][] waterMap = new boolean[128][128];
    private static double originX;
    private static double originZ;
    private static double step;

    public static void generateWaterMap(Location center, double radius)
    {
        originX = center.getX() - radius;
        originZ = center.getZ() - radius;
        step = radius / 64;

        Bukkit.getLogger().info("Generating minimap...");
        for (var y = 0; y < 128; ++y)
            for (var x = 0; x < 128; ++x)
            {
                var loc = center.clone().add(x * step - radius, 0, y * step - radius);
                var block = loc.getWorld().getHighestBlockAt(loc).getType();
                waterMap[x][y] = block == Material.WATER
                        || block == Material.SEAGRASS
                        || block == Material.TALL_SEAGRASS;
            }
        Bukkit.getLogger().info("Generated minimap!");
    }

    public static void clear()
    {
        waterMap = new boolean[128][128];
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player)
    {
        var borderSize = player.getWorld().getWorldBorder().getSize() / step;
        var borderStart = Math.round((128 - borderSize) / 2);
        var borderEnd = 128 - borderStart;
        --borderStart;

        for (var y = 0; y < 128; ++y)
            for (var x = 0; x < 128; ++x)
                if ((x == borderStart || x == borderEnd) && y >= borderStart && y <= borderEnd
                        || (y == borderStart || y == borderEnd) && x >= borderStart && x <= borderEnd)
                    mapCanvas.setPixel(x, y, MapPalette.RED);
                else if (x < borderStart || x > borderEnd || y < borderStart || y > borderEnd)
                    mapCanvas.setPixel(x, y, waterMap[x][y] ? MapPalette.DARK_GRAY : MapPalette.GRAY_1);
                else
                    mapCanvas.setPixel(x, y, waterMap[x][y] ? MapPalette.PALE_BLUE : MapPalette.LIGHT_BROWN);

        if (mapCanvas.getCursors().size() == 0)
            mapCanvas.getCursors().addCursor(new MapCursor((byte) 0, (byte) 0, (byte) 0, MapCursor.Type.PLAYER, true));

        var cursor = mapCanvas.getCursors().getCursor(0);
        cursor.setX((byte) ((player.getLocation().getBlockX() - originX) / step * 2 - 128));
        cursor.setY((byte) ((player.getLocation().getBlockZ() - originZ) / step * 2 - 128));

        var dir = player.getLocation().getDirection().clone().setY(0).normalize();
        var angle = (byte) (Math.round(Math.atan2(dir.getZ(), dir.getX()) / Math.PI * 8 + 12) % 16);
        cursor.setDirection(angle);
    }
}
