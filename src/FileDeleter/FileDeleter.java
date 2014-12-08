package FileDeleter;

import java.io.*;
import java.util.*;

public class FileDeleter {

    private static final Map<String, File> fsf = new HashMap<>();

    public static void delDups(CheckedFile[] files) throws Exception {
        boolean bIsNotDel = false;
        for (CheckedFile file : files) {
            if (!file.del) {
                bIsNotDel = true;
                break;
            }
        }
        if (!bIsNotDel) {
            throw new Exception("All files will die!!!");
        }
        for (CheckedFile f : files) {
            if (f.del) {
                File file = f.file;
                String root = file.toPath().getRoot().toString();
                File gloablBackupDir;
                if (fsf.containsKey(root)) {
                    gloablBackupDir = fsf.get(root);
                } else {
                    String sBackupDir = "backup_" + System.currentTimeMillis();
                    gloablBackupDir = new File(root, sBackupDir);
                    gloablBackupDir.mkdir();
                    fsf.put(root, gloablBackupDir);
                }
                String sAbsDir = file.getParent();
                String sDirWithoutRoot = sAbsDir.replace(root, "");
                File backupDir = new File(gloablBackupDir, sDirWithoutRoot);
                if (!backupDir.exists()) {
                    if (!backupDir.mkdirs()) {
                        System.out.println("err mkdirs: " + backupDir.toString());
                    }
                }
                File to = new File(backupDir, file.getName());
                if (!file.renameTo(to)) {
                    System.out.println("error rename: from : " + file.toString() + " to: " + to.toString());
                }
            }
        }
    }
}
