package DuplicateFinder;

import static com.sun.nio.file.ExtendedOpenOption.NOSHARE_DELETE;
import static com.sun.nio.file.ExtendedOpenOption.NOSHARE_WRITE;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import static java.nio.channels.FileChannel.open;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.READ;

public class LockFileInputStream implements Closeable {

    private final FileChannel fc;

    public long getLength() throws IOException {
        return this.fc.size();
    }

    public LockFileInputStream(File file) throws IOException {
        this(file.toPath());
    }

    public LockFileInputStream(Path path) throws IOException {
        //this.fc = open(path, READ, NOSHARE_DELETE, NOSHARE_WRITE);
        this.fc = open(path, READ);
    }

    public int read(ByteBuffer buffer) throws IOException {
        return this.fc.read(buffer);
    }

    @Override
    public void close() throws IOException {
        this.fc.close();
    }
}
