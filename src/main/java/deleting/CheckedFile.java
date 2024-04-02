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

        boolean isEnglishName = true;
        for (int i = 0; i < sFileName.length(); i++) {
            if ((int) sFileName.charAt(i) > 127) {
                isEnglishName = false;
                break;
            }
        }

        int slashCnt = 0;
        String absPath = file.getAbsolutePath();
        for (int j = 0; j < absPath.length(); j++) {
            if (absPath.charAt(j) == File.separatorChar) {
                slashCnt++;
            }
        }

        this.parameterValues = new EnumMap<>(Parameters.class);
        this.parameterValues.put(Parameters.IS_ENGLISH_FILE_NAME, isEnglishName ? 1 : 0);
        this.parameterValues.put(Parameters.IS_COPY, IS_COPY_PATTERN.matcher(sFileName).find() ? 1 : 0);
        this.parameterValues.put(Parameters.DIRECTORY_DEPTH, slashCnt);
        this.parameterValues.put(Parameters.FILENAME_LENGTH, sFileName.length());
        this.parameterValues.put(Parameters.PATH_LENGTH, absPath.length());

        this.cacheAbsolutePath = this.file.getAbsolutePath();
    }
}
