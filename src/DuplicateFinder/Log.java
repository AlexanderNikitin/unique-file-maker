package DuplicateFinder;

import java.io.FileNotFoundException;
import java.io.*;

public class Log {

    private final PrintStream logFile;

    public Log(String filename) throws FileNotFoundException {
        if (filename == null) {
            this.logFile = null;
        } else {
            this.logFile = new PrintStream(new File(filename));
        }
    }

    public void log(String message) {
        if (message != null) {
            if (this.logFile != null) {
                this.logFile.println(message);
            }
            System.out.println(message);
        }
    }

    @Override
    public void finalize() {
        this.logFile.close();
    }
}
