package FileComparer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FileInRedingGroup implements Closeable {

    private List<FileInReding> alReading;

    public static FileInRedingGroup initFromFiles(List<File> files) throws Exception {
        List<FileInReding> alReading = new ArrayList<>();
        long size = -1;
        String base = null;
        boolean bChanged = false;
        for (File file : files) {
            try {
                FileInReding fr = new FileInReding(file);
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
        return new FileInRedingGroup(alReading);
    }

    private FileInRedingGroup(List<FileInReding> files) {
        this.alReading = new ArrayList<>();
        this.alReading.addAll(files);
    }

    public int count() {
        return this.alReading.size();
    }

    public boolean read() throws IOException {
        for (FileInReding rf : this.alReading) {
            if (!rf.read()) {
                return false;
            }
        }
        return true;
    }

    public List<FileInRedingGroup> separateDiff() {
        int n = this.alReading.size();
        if (n < 2) {
            return null;
        }
        Map<ByteBuffer, List<FileInReding>> grouper = new HashMap<>();
        for (FileInReding rf : this.alReading) {
            ByteBuffer bb = rf.bb;
            List<FileInReding> curList;
            if (grouper.containsKey(bb)) {
                curList = grouper.get(bb);
            } else {
                grouper.put(bb, curList = new ArrayList<>());
            }
            curList.add(rf);
        }
        this.alReading = null;
        List<FileInRedingGroup> result = new ArrayList<>();
        for (List<FileInReding> l : grouper.values()) {
            if (this.alReading == null) {
                this.alReading = l;
            } else {
                result.add(new FileInRedingGroup(l));
            }
        }
        return result;
    }

    public File[] fiels() {
        int n = this.alReading.size();
        File[] result = new File[n];
        for (int i = 0; i < n; i++) {
            result[i] = this.alReading.get(i).file();
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        for (FileInReding rf : this.alReading) {
            rf.close();
        }
    }
}
