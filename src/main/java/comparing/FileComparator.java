package comparing;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class FileComparator {

    private final boolean diffByExt;
    private final boolean needCheck;

    private final Map<Long, List<File[]>> duplicationsGroups = new HashMap<>();

    public FileComparator(boolean diffByExt, boolean needCheck) {
        this.diffByExt = diffByExt;
        this.needCheck = needCheck;
    }

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
        return i >= 0 ? fileName.substring(i + 1).toLowerCase() : "";
    }

    private void compare(Map<Long, List<File>> hmSizeGrouping) throws IOException {
        System.out.println("Compare...");
        long t = System.currentTimeMillis();
        for (Map.Entry<Long, List<File>> eOneSize : hmSizeGrouping.entrySet()) {
            List<File> lOneSize = eOneSize.getValue();
            if (lOneSize.size() > 1) {
                if (this.diffByExt) {
                    Map<String, List<File>> mGroupedByExt = new HashMap<>();
                    lOneSize.stream().forEach(file -> {
                        String ext = getFileExtensionByName(file.getName());
                        List<File> filesByExt;
                        if (mGroupedByExt.containsKey(ext)) {
                            filesByExt = mGroupedByExt.get(ext);
                        } else {
                            mGroupedByExt.put(ext, filesByExt = new ArrayList<>());
                        }
                        filesByExt.add(file);
                    });
                    List<File[]> groups = new ArrayList<>();
                    for (List<File> filesByExt : mGroupedByExt.values()) {
                        if (filesByExt.size() > 1) {
                            groups.addAll(new FileSizeGroup(filesByExt).getGroups());
                        }
                    }
                    this.duplicationsGroups.put(eOneSize.getKey(), groups);
                } else {
                    this.duplicationsGroups.put(eOneSize.getKey(), new FileSizeGroup(lOneSize).getGroups());
                }
            }
        }
        System.out.println("Compare time: " + (System.currentTimeMillis() - t));
    }

    private boolean check() throws NoSuchAlgorithmException {
        System.out.println("Checking...");
        long allSize = getAllSize();
        System.out.println("All size: " + allSize);

        //long t = System.currentTimeMillis();
        int errGrCnt = 0;
        long curSize = 0;
        long oldPercent = -1;
        MessageDigest md = MessageDigest.getInstance("MD5");
        for (Map.Entry<Long, List<File[]>> arFileOneSize : this.duplicationsGroups.entrySet()) {
            List<File[]> dupGroups = arFileOneSize.getValue();
            if (dupGroups.isEmpty()) {
                continue;
            }
            long groupSize = arFileOneSize.getKey();
            for (File[] duplicates : dupGroups) {
                int n = duplicates.length;
                if (n > 1) {
                    byte[][] d = new byte[n][];
                    for (int i = 0; i < n; i++) {
                        File dup = duplicates[i];
                        try (DigestInputStream dis = new DigestInputStream(new BufferedInputStream(new FileInputStream(dup.getPath())), md)) {
                            while (dis.read() != -1) {
                            }
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                        d[i] = md.digest();
                    }
                    boolean bErr = false;
                    for (int i = 1; i < d.length; i++) {
                        if (!Arrays.equals(d[0], d[i])) {
                            System.out.println(duplicates[i].toString());
                            bErr = true;
                        }
                    }
                    if (bErr) {
                        System.out.println("Default: " + duplicates[0].toString());
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
        System.out.println("All errored groups: " + errGrCnt);
        return errGrCnt == 0;
    }

    private long getAllSize() {
        long allSize = 0;
        for (Map.Entry<Long, List<File[]>> arFileOneSize : this.duplicationsGroups.entrySet()) {
            List<File[]> dupGroups = arFileOneSize.getValue();
            long groupSize = arFileOneSize.getKey();
            for (File[] dups : dupGroups) {
                long n = dups.length;
                if (n > 1) {
                    long delta = groupSize * n;
                    assert (Long.MAX_VALUE - delta) > allSize : delta + " " + allSize;
                    allSize += delta;
                }
            }
        }
        return allSize;
    }

    public boolean compare(List<File> files) throws IOException, NoSuchAlgorithmException {
        this.compare(this.group(files));
        return !this.needCheck || this.check();
    }

    public Map<Long, List<File[]>> getResult() {
        return this.duplicationsGroups;
    }
}
