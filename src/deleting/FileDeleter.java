package deleting;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileDeleter {

    private final Map<String, File> fileSystemFolders = new HashMap<>();

    public boolean delete(File file) {
        String root = file.toPath().getRoot().toString();
        File globalBackupDir;
        if (fileSystemFolders.containsKey(root)) {
            globalBackupDir = fileSystemFolders.get(root);
        } else {
            String sBackupDir = "backup_" + System.currentTimeMillis();
            globalBackupDir = new File(root, sBackupDir);
            if (globalBackupDir.mkdir()) {
                fileSystemFolders.put(root, globalBackupDir);
            } else {
                return false;
            }
        }
        String sAbsDir = file.getParent();
        String sDirWithoutRoot = sAbsDir.replace(root, "");
        File backupDir = new File(globalBackupDir, sDirWithoutRoot);
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
}
