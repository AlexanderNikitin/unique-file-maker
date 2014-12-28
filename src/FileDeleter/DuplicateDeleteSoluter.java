package FileDeleter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DuplicateDeleteSoluter {

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
                int delCnt = 0;
                boolean bEnd = false;
                for (CheckedFile cf : result) {
                    if (cf.del) {
                        delCnt++;
                        continue;
                    }
                    if (cf.params.get(param) != saveValue) {
                        if (result.length - delCnt > 1) {
                            cf.del = true;
                            delCnt++;
                        } else {
                            bEnd = true;
                            break;
                        }
                    }
                }
                if (bEnd) {
                    break;
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
                } else {
                    bSaved = true;
                }
            }
        }
        //additional final control
        boolean atLeastOneSave = false;
        for (CheckedFile cf : result) {
            if (!cf.del) {
                atLeastOneSave = true;
                break;
            }
        }
        if (!atLeastOneSave) {
            result[0].del = false;
        }
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
