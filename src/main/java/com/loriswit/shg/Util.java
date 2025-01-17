package com.loriswit.shg;

import java.io.File;

public class Util
{
    public static boolean deleteDirectory(File directoryToBeDeleted)
    {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null)
            for (File file : allContents)
                deleteDirectory(file);

        return directoryToBeDeleted.delete();
    }
}
