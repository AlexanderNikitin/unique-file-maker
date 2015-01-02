package FileComparer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileComparer {

    private final boolean bDiffByExt;
    private final boolean bNeedCheck;

    private final Map<Long, List<File[]>> dupsGroups = new HashMap<>();

    private Map<Long, List<File>> group(List<File> files) {
        System.out.println("Grouping...");
        Map<Long, List<File>> hmSizeGrouping = new HashMap<>();
        for (File f : files) {
            long size = f.length();
            if (size > 0) {
                List<File> lEqualSize;
                if (hmSizeGrouping.containsKey(size)) {
                    lEqualSize = hmSizeGrouping.get(size);
                } else {
                    hmSizeGrouping.put(size, lEqualSize = new ArrayList<>());
                }
                lEqualSize.add(f);
            }
        }
        return hmSizeGrouping;
    }

    private String getFileExtensionByName(String fileName) {
        int i = fileName.lastIndexOf('.');
        return i >= 0 ? fileName.substring(i + 1) : "";
    }

    private void compare(Map<Long, List<File>> hmSizeGrouping) throws IOException {
        System.out.println("Compare...");
        long t = System.currentTimeMillis();
        for (Map.Entry<Long, List<File>> eOneSize : hmSizeGrouping.entrySet()) {
            List<File> lOneSize = eOneSize.getValue();
            if (lOneSize.size() > 1) {
                if (this.bDiffByExt) {
                    Map<String, List<File>> hmGroupedByExt = new HashMap<>();
                    lOneSize.stream().forEach(file -> {
                        String ext = getFileExtensionByName(file.getName());
                        List<File> filesByExt;
                        if (hmGroupedByExt.containsKey(ext)) {
                            filesByExt = hmGroupedByExt.get(ext);
                        } else {
                            hmGroupedByExt.put(ext, filesByExt = new ArrayList<>());
                        }
                        filesByExt.add(file);
                    });
                    List<File[]> groups = new ArrayList<>();
                    for (List<File> filesByExt : hmGroupedByExt.values()) {
                        if (filesByExt.size() > 1) {
                            groups.addAll(new FileSizeGroup(filesByExt).getGroups());
                        }
                    }
                    this.dupsGroups.put(eOneSize.getKey(), groups);
                } else {
                    this.dupsGroups.put(eOneSize.getKey(), new FileSizeGroup(lOneSize).getGroups());
                }
            }
        }
        System.out.println("Compare time: " + (System.currentTimeMillis() - t));
    }

    private boolean check() throws NoSuchAlgorithmException {
        System.out.println("Checking...");
        long allSize = 0;
        for (Map.Entry<Long, List<File[]>> arFileOneSize : this.dupsGroups.entrySet()) {
            List<File[]> dupGruops = arFileOneSize.getValue();
            long groupSize = arFileOneSize.getKey();
            for (File[] dups : dupGruops) {
                long n = dups.length;
                if (n > 1) {
                    long delta = groupSize * n;
                    assert (Long.MAX_VALUE - delta) > allSize : delta + " " + allSize;
                    allSize += delta;
                }
            }
        }
        System.out.println("All size: " + allSize);

        //long t = System.currentTimeMillis();
        int errGrCnt = 0;
        long curSize = 0;
        long oldPercent = -1;
        MessageDigest md = MessageDigest.getInstance("MD5");
        for (Map.Entry<Long, List<File[]>> arFileOneSize : this.dupsGroups.entrySet()) {
            List<File[]> dupGruops = arFileOneSize.getValue();
            if (dupGruops.isEmpty()) {
                continue;
            }
            long groupSize = arFileOneSize.getKey();
            for (File[] dups : dupGruops) {
                int n = dups.length;
                if (n > 1) {
                    byte[][] d = new byte[n][];
                    for (int i = 0; i < n; i++) {
                        File dup = dups[i];
                        try (DigestInputStream dis = new DigestInputStream(new BufferedInputStream(new FileInputStream(dup.getPath())), md)) {
                            while (dis.read() != -1);
                        } catch (FileNotFoundException e) {
                            System.err.println(e.getMessage());
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                        d[i] = md.digest();
                    }
                    boolean bErr = false;
                    for (int i = 1; i < d.length; i++) {
                        if (!Arrays.equals(d[0], d[i])) {
                            System.out.println(dups[i].toString());
                            bErr = true;
                        }
                    }
                    if (bErr) {
                        System.out.println("Default: " + dups[0].toString());
                        System.out.println();
                        errGrCnt++;
                    }

                    long delta = n * groupSize;
                    assert (Long.MAX_VALUE - delta) > curSize : delta + " " + allSize;
                    curSize += delta;

                    long percent = curSize * 100 / allSize;
                    if (percent > oldPercent) {
                        System.out.println(percent + "%");
                        oldPercent = percent;
                    }
                }
            }
            /*long curT = System.currentTimeMillis();
             long timeSec = (curT - t) / 1000;
             long speedByte = curSize;
             if (timeSec > 0) {
             speedByte /= timeSec;
             }
             out.println((speedByte) + " B/s");*/
        }
        System.out.println("All errord groups: " + errGrCnt);
        return errGrCnt == 0;
    }

    public FileComparer(boolean bDiffByExt, boolean bNeedCheck) {
        this.bDiffByExt = bDiffByExt;
        this.bNeedCheck = bNeedCheck;
    }

    public boolean compare(List<File> files) throws IOException, NoSuchAlgorithmException {
        this.compare(this.group(files));
        return (this.bNeedCheck && this.check()) || !this.bNeedCheck;
    }

    public Map<Long, List<File[]>> getResult() {
        return this.dupsGroups;
    }
    /*
     private static String getHumanSpeed(long speedByte) {
     long byteInKb = 1024;
     long byteInMb = byteInKb * byteInKb;
     long byteInGb = byteInMb * byteInKb;
     if (speedByte >= byteInGb) {
     return (speedByte / byteInGb) + " GB/s";
     }
     if (speedByte >= byteInMb) {
     return (speedByte / byteInMb) + " MB/s";
     }
     if (speedByte >= byteInKb) {
     return (speedByte / byteInKb) + " KB/s";
     }
     return speedByte + " B/s";
     }
     */
}
