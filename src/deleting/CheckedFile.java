package deleting;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CheckedFile {

    public final File file;
    public boolean delete;

    public final int DIRECTORY_DEPTH;
    public final int FILENAME_LENGTH;
    public final int PATH_LENGTH;
    public final int IS_COPY;
    public final int IS_ENGLISH_FILE_NAME;

    public final String cacheAbsolutePath;

    public final Map<Parameters, Integer> params;

    public CheckedFile(File file) {
        this.file = file;
        this.delete = false;

        String sFileName = file.getName();
        Pattern pattern = Pattern.compile("\\s\\(\\d{0,3}\\)(\\.[^\\.]+){0,1}$");

        boolean bIsEng = true;
        for (int i = 0; i < sFileName.length(); i++) {
            if ((int) sFileName.charAt(i) > 127) {
                bIsEng = false;
                break;
            }
        }

        int slashCnt = 0;
        String sAbsPath = file.getAbsolutePath();
        for (int j = 0; j < sAbsPath.length(); j++) {
            if (sAbsPath.charAt(j) == File.separatorChar) {
                slashCnt++;
            }
        }

        this.IS_COPY = pattern.matcher(sFileName).find() ? 1 : 0;
        this.IS_ENGLISH_FILE_NAME = bIsEng ? 1 : 0;
        this.DIRECTORY_DEPTH = slashCnt;
        this.FILENAME_LENGTH = sFileName.length();
        this.PATH_LENGTH = sAbsPath.length();

        this.params = new EnumMap<>(Parameters.class);

        /*DuplicateDeleteSolver.Parameters[] ps = DuplicateDeleteSolver.Parameters.values();
         for (DuplicateDeleteSolver.Parameters p : ps) {
         try {
         this.params.put(p, this.getClass().getField(p.name()).getInt(this));
         } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
         Logger.getLogger(CheckedFile.class.getName()).log(Level.SEVERE, null, ex);
         }
         }*/
        this.params.put(Parameters.IS_ENGLISH_FILE_NAME, this.IS_ENGLISH_FILE_NAME);
        this.params.put(Parameters.IS_COPY, this.IS_COPY);
        this.params.put(Parameters.DIRECTORY_DEPTH, this.DIRECTORY_DEPTH);
        this.params.put(Parameters.FILENAME_LENGTH, this.FILENAME_LENGTH);
        this.params.put(Parameters.PATH_LENGTH, this.PATH_LENGTH);

        this.cacheAbsolutePath = this.file.getAbsolutePath();
    }
}
