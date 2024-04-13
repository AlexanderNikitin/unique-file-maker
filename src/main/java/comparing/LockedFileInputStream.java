package comparing;

import com.sun.nio.file.ExtendedOpenOption;
import utils.Utils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import static java.nio.channels.FileChannel.open;
import static java.nio.file.StandardOpenOption.READ;

public class LockedFileInputStream implements AutoCloseable, Closeable {

    private final FileChannel fileChannel;

    public LockedFileInputStream(File file) throws IOException {
        this(file.toPath());
    }

    public LockedFileInputStream(Path path) throws IOException {
        this.fileChannel = Utils.IS_WINDOWS ?
                open(path, READ, ExtendedOpenOption.NOSHARE_DELETE, ExtendedOpenOption.NOSHARE_WRITE) :
                open(path, READ);
    }

    public long getLength() throws IOException {
        return this.fileChannel.size();
    }

    public int read(ByteBuffer buffer) throws IOException {
        return this.fileChannel.read(buffer);
    }

    @Override
    public void close() throws IOException {
        this.fileChannel.close();
    }
}
