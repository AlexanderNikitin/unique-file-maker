package deleting;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CheckedFile {

    private static final Pattern IS_COPY_PATTERN = Pattern.compile("\\s\\(\\d{0,3}\\)(\\.[^\\.]+){0,1}$");

    public final File file;
    public final String cacheAbsolutePath;
    final Map<Parameters, Integer> parameterValues;
    public boolean delete;

    CheckedFile(File file) {
        this.file = file;
        this.delete = false;

        String sFileName = file.getName();
        String absPath = file.getAbsolutePath();

        this.parameterValues = new EnumMap<>(Parameters.class);

        this.parameterValues.put(Parameters.IS_ENGLISH_FILE_NAME, isEnglishFileName(sFileName) ? 1 : 0);
        this.parameterValues.put(Parameters.IS_COPY, IS_COPY_PATTERN.matcher(sFileName).find() ? 1 : 0);
        this.parameterValues.put(Parameters.DIRECTORY_DEPTH, getDepth(absPath));
        this.parameterValues.put(Parameters.FILENAME_LENGTH, sFileName.length());
        this.parameterValues.put(Parameters.PATH_LENGTH, absPath.length());

        this.cacheAbsolutePath = this.file.getAbsolutePath();
    }

    private static boolean isEnglishFileName(String fileName) {
        for (int i = 0; i < fileName.length(); i++) {
            if ((int) fileName.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }

    private static int getDepth(String absPath) {
        int slashCnt = 0;
        for (int j = 0; j < absPath.length(); j++) {
            if (absPath.charAt(j) == File.separatorChar) {
                slashCnt++;
            }
        }
        return slashCnt;
    }
}
