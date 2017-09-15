package deleting;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DuplicateDeleteSoluter {

    private final boolean saveOnlyOne;
    private final List<Rule> rules = new ArrayList<>();
    private final List<String> savePrefer = new ArrayList<>();
    private final List<String> doNotDelete = new ArrayList<>();

    public CheckedFile[] sol(File[] duplicates) {
        int n = duplicates.length;
        if (n == 0) {
            return null;
        }
        CheckedFile[] result = new CheckedFile[n];
        for (int i = 0; i < n; i++) {
            result[i] = new CheckedFile(duplicates[i]);
        }
        if (!this.savePrefer.isEmpty()) {
            for (String dir : this.savePrefer) {
                boolean[] bPrefer = new boolean[result.length];
                boolean bOnePrefer = false;
                for (int i = 0; i < result.length; i++) {
                    if (bPrefer[i] = result[i].cacheAbsolutePath.startsWith(dir)) {
                        bOnePrefer = true;
                    }
                }
                if (bOnePrefer) {
                    for (int i = 0; i < result.length; i++) {
                        result[i].delete = !bPrefer[i];
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
                if (cf.delete) {
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
                    if (cf.delete) {
                        delCnt++;
                        continue;
                    }
                    if (cf.params.get(param) != saveValue) {
                        if (result.length - delCnt > 1) {
                            cf.delete = true;
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
        if (!this.doNotDelete.isEmpty()) {
            for (String dir : this.doNotDelete) {
                for (int i = 0; i < result.length; i++) {
                    bCanDel[i] = !result[i].cacheAbsolutePath.startsWith(dir);
                }
            }
        }
        for (int i = 0; i < result.length; i++) {
            if (!bCanDel[i]) {
                result[i].delete = false;
            }
        }
        if (this.saveOnlyOne) {
            boolean bSaved = false;
            for (int i = 0; i < result.length; i++) {
                CheckedFile cf = result[i];
                if (cf.delete) {
                    continue;
                }
                if (bSaved && bCanDel[i]) {
                    cf.delete = true;
                } else {
                    bSaved = true;
                }
            }
        }
        //additional final control
        boolean atLeastOneSave = false;
        for (CheckedFile cf : result) {
            if (!cf.delete) {
                atLeastOneSave = true;
                break;
            }
        }
        if (!atLeastOneSave) {
            result[0].delete = false;
        }
        return result;
    }

    public DuplicateDeleteSoluter(List<Rule> lSaveRules) {
        this(lSaveRules, false);
    }

    public DuplicateDeleteSoluter(List<Rule> lSaveRules, boolean saveOnlyOne) {
        this(lSaveRules, saveOnlyOne, null, null);
    }

    public DuplicateDeleteSoluter(List<Rule> lSaveRules, boolean saveOnlyOne, List<String> savePrefer, List<String> doNotDelete) {
        if (lSaveRules != null) {
            this.rules.addAll(lSaveRules);
        }
        this.saveOnlyOne = saveOnlyOne;
        if (savePrefer != null) {
            this.savePrefer.addAll(savePrefer);
        }
        if (doNotDelete != null) {
            this.doNotDelete.addAll(doNotDelete);
        }
    }
}
