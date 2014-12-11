package FileDeleter;

import java.io.*;
import java.util.*;

public class FileDeleter {

    private final Map<String, File> fsf;
    private int cnt = 0;

    public int cnt() {
        return this.cnt;
    }

    public void delDups(CheckedFile[] files) throws Exception {
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
                boolean bExistsDir = backupDir.exists();
                if (!bExistsDir) {
                    bExistsDir = backupDir.mkdirs();
                }
                if (bExistsDir) {
                    File to = new File(backupDir, file.getName());
                    if (file.renameTo(to)) {
                        this.cnt++;
                    } else {
                        System.out.println("error rename: from : " + file.toString() + " to: " + to.toString());
                    }
                } else {
                    System.out.println("err mkdirs: " + backupDir.toString());
                }
            }
        }
    }

    public FileDeleter() {
        this.fsf = new HashMap<>();
    }
}
