package DuplicateFinder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Log {

    private final List<PrintStream> logFiles;

    public Log(List<String> filenames) {
        if (filenames == null || filenames.isEmpty()) {
            this.logFiles = null;
        } else {
            this.logFiles = new ArrayList<>();
            for (String filename : filenames) {
                try {
                    this.logFiles.add(new PrintStream(new File(filename)));
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    public void log(String message) {
        if (message != null) {
            if (this.logFiles != null) {
                for (PrintStream log : this.logFiles) {
                    log.println(message);
                }
            }
            System.out.println(message);
        }
    }

    @Override
    public void finalize() {
        for (PrintStream log : this.logFiles) {
            log.close();
        }
    }
}
