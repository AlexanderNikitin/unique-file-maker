package DuplicateFinder;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FileInReding implements Closeable {

    private static final int BUFFER_SIZE = 1024 * 4;

    private final LockFileInputStream lfis;
    public final ByteBuffer bb;
    private final File file;

    public File file() {
        return this.file;
    }

    public long getLength() throws IOException {
        return this.lfis.getLength();
    }

    public ByteBuffer getBuffer() {
        return this.bb.asReadOnlyBuffer();
    }

    public FileInReding(File file) throws IOException {
        this.file = file;
        this.lfis = new LockFileInputStream(file);
        this.bb = ByteBuffer.allocate((int) Math.min(BUFFER_SIZE, this.file.length()));
    }

    public boolean read() throws IOException {
        int res = this.lfis.read(this.bb);
        this.bb.rewind();
        return res > 0;
    }

    @Override
    public void close() throws IOException {
        this.lfis.close();
    }
}
