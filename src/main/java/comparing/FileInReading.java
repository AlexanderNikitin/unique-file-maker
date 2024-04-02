package comparing;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FileInReading implements Closeable {

    private static final int BUFFER_SIZE = 1024 * 4;
    public final ByteBuffer bb;
    private final LockedFileInputStream stream;
    private final File file;

    public FileInReading(File file) throws IOException {
        this.file = file;
        this.stream = new LockedFileInputStream(file);
        this.bb = ByteBuffer.allocate((int) Math.min(BUFFER_SIZE, this.file.length()));
    }

    public File file() {
        return this.file;
    }

    public long getLength() throws IOException {
        return this.stream.getLength();
    }

    public ByteBuffer getBuffer() {
        return this.bb.asReadOnlyBuffer();
    }

    public boolean read() throws IOException {
        int res = this.stream.read(this.bb);
        this.bb.rewind();
        return res >= 0;
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }
}
