package comparing;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static java.nio.channels.FileChannel.open;
import static java.nio.file.StandardOpenOption.READ;

public class LockedFileInputStream implements Closeable {

    private final FileChannel fc;

    public long getLength() throws IOException {
        return this.fc.size();
    }

    public LockedFileInputStream(File file) throws IOException {
        this(file.toPath());
    }

    public LockedFileInputStream(Path path) throws IOException {
        //this.fc = open(path, READ, ExtendedOpenOption.NOSHARE_DELETE, ExtendedOpenOption.NOSHARE_WRITE);
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
