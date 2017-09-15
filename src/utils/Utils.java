package utils;

public abstract class Utils {

    public static String getHumanDataSize(long speedByte) {
        long byteInKb = 1024;
        long byteInMb = byteInKb * byteInKb;
        long byteInGb = byteInMb * byteInKb;
        if (speedByte >= byteInGb) {
            return (speedByte / byteInGb) + " GB";
        }
        if (speedByte >= byteInMb) {
            return (speedByte / byteInMb) + " MB";
        }
        if (speedByte >= byteInKb) {
            return (speedByte / byteInKb) + " KB";
        }
        return speedByte + " B";
    }

    public static String strRepeat(String s, int c) {
        if (s == null) {
            return null;
        }
        if (s.isEmpty() || c <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(c * s.length());
        for (int i = 0; i < c; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}