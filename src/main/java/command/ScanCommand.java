package command;

import fs.ExtFileNameFilter;
import picocli.CommandLine;

import java.io.File;
import java.io.FilenameFilter;

@CommandLine.Command(name = "scan")
public class ScanCommand implements Runnable {
    @CommandLine.Parameters(arity = "1..*")
    File[] folders;

    @CommandLine.Option(names = {"-r", "--recursive"})
    boolean recursive;

    @CommandLine.Option(names = {"-x", "--extensions"}, arity = "0..*")
    String[] extensions;

    FilenameFilter filenameFilter;

    @Override
    public void run() {
        filenameFilter = new ExtFileNameFilter(extensions);
        for (File folder : folders) {
            if (!folder.exists()) {
                continue;
            }
            if (folder.isDirectory()) {
                processFolder(folder);
            }
            if (folder.isFile()) {
                processFile(folder);
            }
        }
    }

    private void processFolder(File folder) {
        for (File file : folder.listFiles(filenameFilter)) {
            if(file.isDirectory() && recursive) {
                processFolder(file);
            }
            if(file.isFile()) {
                processFile(file);
            }
        }
    }

    private void processFile(File file) {
        //TODO save file to the list or DB
    }
}
