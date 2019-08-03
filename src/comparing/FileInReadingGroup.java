package comparing;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FileInReadingGroup implements Closeable {

    private List<FileInReading> reading;

    private FileInReadingGroup(List<FileInReading> files) {
        this.reading = new ArrayList<>();
        this.reading.addAll(files);
    }

    public static FileInReadingGroup initFromFiles(List<File> files) throws Exception {
        List<FileInReading> alReading = new ArrayList<>();
        long size = -1;
        String base = null;
        boolean bChanged = false;
        for (File file : files) {
            try {
                FileInReading fr = new FileInReading(file);
                long curSize = fr.getLength();
                if (size == -1) {
                    size = curSize;
                    base = fr.file().toString();
                } else {
                    if (size != curSize) {
                        System.out.println("Size changed to: " + fr.file().toString());
                        bChanged = true;
                        continue;
                    }
                }
                alReading.add(fr);
            } catch (IOException e) {
                System.err.println(e.toString());
                System.err.println(e.getMessage());
            }
        }
        if (bChanged) {
            System.out.println("Base file: " + base + " : " + size);
        }
        if (alReading.isEmpty()) {
            throw new Exception("Empty reading group!");
        }
        return new FileInReadingGroup(alReading);
    }

    public int count() {
        return this.reading.size();
    }

    public boolean read() throws IOException {
        for (FileInReading rf : this.reading) {
            if (!rf.read()) {
                return false;
            }
        }
        return true;
    }

    public List<FileInReadingGroup> separateDiff() {
        int n = this.reading.size();
        List<FileInReadingGroup> result = new ArrayList<>();
        if (n < 2) {
            return result;
        }
        Map<ByteBuffer, List<FileInReading>> grouper = new HashMap<>();
        for (FileInReading rf : this.reading) {
            ByteBuffer bb = rf.bb;
            List<FileInReading> curList;
            if (grouper.containsKey(bb)) {
                curList = grouper.get(bb);
            } else {
                grouper.put(bb, curList = new ArrayList<>());
            }
            curList.add(rf);
        }
        this.reading = null;
        for (List<FileInReading> l : grouper.values()) {
            if (this.reading == null) {
                this.reading = l;
            } else {
                result.add(new FileInReadingGroup(l));
            }
        }
        return result;
    }

    public File[] fiels() {
        int n = this.reading.size();
        File[] result = new File[n];
        for (int i = 0; i < n; i++) {
            result[i] = this.reading.get(i).file();
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        for (FileInReading rf : this.reading) {
            rf.close();
        }
    }
}
