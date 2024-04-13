package utils;

public abstract class Utils {
    public static final boolean IS_WINDOWS = System.getProperty("os.name")
            .toLowerCase()
            .contains("win");
    private static final long BYTE_IN_KB = 1024;
    private static final long BYTE_IN_MB = BYTE_IN_KB * BYTE_IN_KB;
    private static final long BYTE_IN_GB = BYTE_IN_MB * BYTE_IN_KB;

    public static String getHumanDataSize(long speedByte) {
        if (speedByte >= BYTE_IN_GB) {
            return (speedByte / BYTE_IN_GB) + " GB";
        }
        if (speedByte >= BYTE_IN_MB) {
            return (speedByte / BYTE_IN_MB) + " MB";
        }
        if (speedByte >= BYTE_IN_KB) {
            return (speedByte / BYTE_IN_KB) + " KB";
        }
        return speedByte + " B";
    }

    public static String repeatString(String string, int count) {
        if (string == null) {
            return null;
        }
        return string.repeat(count);
    }
}