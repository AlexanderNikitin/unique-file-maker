package FileDeleter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DuplicateDeleteSoluter {

    public static enum Param {

        DIRECTORY_DEPTH,
        FILENAME_LENGTH,
        PATH_LENGTH,
        IS_COPY,
        IS_ENGLISH_FILE_NAME
    }

    public static class Rule {

        public final Param param;
        public final boolean saveByMax;

        public Rule(Param param, boolean saveByMax) {
            this.param = param;
            this.saveByMax = saveByMax;
        }
    }

    private final boolean bSaveOnlyOne;
    private final List<Rule> rules = new ArrayList<>();

    public CheckedFile[] sol(File[] dups) {
        int n = dups.length;
        if (n == 0) {
            return null;
        }
        CheckedFile[] result = new CheckedFile[n];
        for (int i = 0; i < n; i++) {
            result[i] = new CheckedFile(dups[i]);
        }
        for (Rule rule : this.rules) {
            int saveCnt = 0;
            for (CheckedFile cf : result) {
                if (!cf.del) {
                    saveCnt++;
                }
            }
            assert saveCnt > 0;
            if (saveCnt < 2) {
                break;
            }
            Param param = rule.param;
            boolean saveByMax = rule.saveByMax;
            int valueMax = Integer.MIN_VALUE;
            int valueMin = Integer.MAX_VALUE;
            for (CheckedFile cf : result) {
                if (cf.del) {
                    continue;
                }
                int valueParam = cf.params.get(param);
                if (valueParam > valueMax) {
                    valueMax = valueParam;
                }
                if (valueParam < valueMin) {
                    valueMin = valueParam;
                }
            }
            if (valueMax > valueMin) {
                int saveValue = saveByMax ? valueMax : valueMin;
                for (CheckedFile cf : result) {
                    if (!cf.del && (cf.params.get(param) != saveValue)) {
                        cf.del = true;
                    }
                }
            }
        }
        if (this.bSaveOnlyOne) {
            boolean bSaved = false;
            for (CheckedFile cf : result) {
                if (cf.del) {
                    continue;
                }
                if (bSaved) {
                    cf.del = true;
                }
                bSaved = true;
            }
        }
        boolean atLeastOneSave = false;
        for (CheckedFile cf : result) {
            if (!cf.del) {
                atLeastOneSave = true;
                break;
            }
        }
        assert atLeastOneSave;
        return result;
    }

    public DuplicateDeleteSoluter(List<Rule> rules) {
        this(rules, false);
    }

    public DuplicateDeleteSoluter(List<Rule> rules, boolean bSaveOnlyOne) {
        this.rules.addAll(rules);
        this.bSaveOnlyOne = bSaveOnlyOne;
    }
}
