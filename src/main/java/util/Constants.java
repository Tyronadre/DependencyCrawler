package util;

import java.io.File;

public class Constants {

    public static File getDataFolder() {
        if (dataFolder == null) {
            dataFolder = new File(new File(Constants.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile(), "data");
            if (dataFolder.exists() && !dataFolder.isDirectory())
                return null;
            if (!dataFolder.exists() && !dataFolder.mkdirs())
                return null;
        }
        return dataFolder;
    }

    public static boolean setDataFolder(File dataFolder) {
        Constants.dataFolder = dataFolder;
        if (dataFolder.exists() && !dataFolder.isDirectory())
            return false;
        return dataFolder.exists() || dataFolder.mkdirs();
    }

    public static Boolean crawlOptional = false;
    public static Boolean crawlEverything = false;
    public static Integer crawlThreads = 20;
    private static File dataFolder;

}
