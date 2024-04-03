package utils;

public abstract class Utils {

    public static final boolean IS_WINDOWS;
    private static final long BYTE_IN_KB = 1024;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        IS_WINDOWS = os.contains("win");
    }

    public static String getHumanDataSize(long speedByte) {
        long byteInMb = BYTE_IN_KB * BYTE_IN_KB;
        long byteInGb = byteInMb * BYTE_IN_KB;
        if (speedByte >= byteInGb) {
            return (speedByte / byteInGb) + " GB";
        }
        if (speedByte >= byteInMb) {
            return (speedByte / byteInMb) + " MB";
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