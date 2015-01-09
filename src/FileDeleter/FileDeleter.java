package FileDeleter;

import java.io.*;
import java.util.*;

public class FileDeleter {

    private final Map<String, File> fsf;

    public boolean delete(File file) {
        String root = file.toPath().getRoot().toString();
        File gloablBackupDir;
        if (fsf.containsKey(root)) {
            gloablBackupDir = fsf.get(root);
        } else {
            String sBackupDir = "backup_" + System.currentTimeMillis();
            gloablBackupDir = new File(root, sBackupDir);
            if (gloablBackupDir.mkdir()) {
                fsf.put(root, gloablBackupDir);
            } else {
                return false;
            }
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
                return true;
            } else {
                System.out.println("error rename: from : " + file.toString() + " to: " + to.toString());
            }
        } else {
            System.out.println("err mkdirs: " + backupDir.toString());
        }
        return false;
    }

    public FileDeleter() {
        this.fsf = new HashMap<>();
    }
}
