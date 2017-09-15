package deleting;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DuplicateDeleteSoluter {

    private final boolean bSaveOnlyOne;
    private final List<Rule> rules = new ArrayList<>();
    private final List<String> lSavePrefer = new ArrayList<>();
    private final List<String> lNoDelete = new ArrayList<>();

    public CheckedFile[] sol(File[] dups) {
        int n = dups.length;
        if (n == 0) {
            return null;
        }
        CheckedFile[] result = new CheckedFile[n];
        for (int i = 0; i < n; i++) {
            result[i] = new CheckedFile(dups[i]);
        }
        if (!this.lSavePrefer.isEmpty()) {
            for (String dir : this.lSavePrefer) {
                boolean[] bPrefer = new boolean[result.length];
                boolean bOnePrefer = false;
                for (int i = 0; i < result.length; i++) {
                    if (bPrefer[i] = result[i].cacheAbsolutePath.startsWith(dir)) {
                        bOnePrefer = true;
                    }
                }
                if (bOnePrefer) {
                    for (int i = 0; i < result.length; i++) {
                        result[i].del = !bPrefer[i];
                    }
                    break;
                }
            }
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
        boolean[] bCanDel = new boolean[result.length];
        Arrays.fill(bCanDel, true);
        if (!this.lNoDelete.isEmpty()) {
            for (String dir : this.lNoDelete) {
                for (int i = 0; i < result.length; i++) {
                    bCanDel[i] = !result[i].cacheAbsolutePath.startsWith(dir);
                }
            }
        }
        for (int i = 0; i < result.length; i++) {
            if (!bCanDel[i]) {
                result[i].del = false;
            }
        }
        if (this.bSaveOnlyOne) {
            boolean bSaved = false;
            for (int i = 0; i < result.length; i++) {
                CheckedFile cf = result[i];
                if (cf.del) {
                    continue;
                }
                if (bSaved && bCanDel[i]) {
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

    public DuplicateDeleteSoluter(List<Rule> lSaveRules) {
        this(lSaveRules, false);
    }

    public DuplicateDeleteSoluter(List<Rule> lSaveRules, boolean bSaveOnlyOne) {
        this(lSaveRules, bSaveOnlyOne, null, null);
    }

    public DuplicateDeleteSoluter(List<Rule> lSaveRules, boolean bSaveOnlyOne, List<String> lSavePrefer, List<String> lNoDelete) {
        if (lSaveRules != null) {
            this.rules.addAll(lSaveRules);
        }
        this.bSaveOnlyOne = bSaveOnlyOne;
        if (lSavePrefer != null) {
            this.lSavePrefer.addAll(lSavePrefer);
        }
        if (lNoDelete != null) {
            this.lNoDelete.addAll(lNoDelete);
        }
    }
}