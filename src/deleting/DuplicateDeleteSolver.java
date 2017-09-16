package deleting;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DuplicateDeleteSolver {

    private final boolean saveOnlyOne;
    private final List<Rule> rules = new ArrayList<>();
    private final List<String> savePreferDirectories = new ArrayList<>();
    private final List<String> doNotDeleteDirectories = new ArrayList<>();

    public CheckedFile[] solve(File[] duplicates) {
        if (duplicates == null) {
            return null;
        }
        int n = duplicates.length;
        if (n == 0) {
            return null;
        }
        CheckedFile[] result = new CheckedFile[n];
        for (int i = 0; i < n; i++) {
            result[i] = new CheckedFile(duplicates[i]);
        }
        for (String dir : this.savePreferDirectories) {
            boolean[] isPreferFile = new boolean[result.length];
            boolean preferFileExists = false;
            for (int i = 0; i < result.length; i++) {
                if (isPreferFile[i] = result[i].cacheAbsolutePath.startsWith(dir)) {
                    preferFileExists = true;
                }
            }
            if (preferFileExists) {
                for (int i = 0; i < result.length; i++) {
                    result[i].delete = !isPreferFile[i];
                }
                break;
            }
        }
        for (Rule rule : this.rules) {
            Parameters parameters = rule.parameters;
            boolean saveByMax = rule.saveByMax;
            int valueMax = Integer.MIN_VALUE;
            int valueMin = Integer.MAX_VALUE;
            for (CheckedFile checkedFile : result) {
                if (checkedFile.delete) {
                    continue;
                }
                int valueParam = checkedFile.parameterValues.get(parameters);
                if (valueParam > valueMax) {
                    valueMax = valueParam;
                }
                if (valueParam < valueMin) {
                    valueMin = valueParam;
                }
            }
            if (valueMax > valueMin) {
                int saveValue = saveByMax ? valueMax : valueMin;
                int deleteCount = 0;
                boolean end = false;
                for (CheckedFile cf : result) {
                    if (cf.delete) {
                        deleteCount++;
                        continue;
                    }
                    if (cf.parameterValues.get(parameters) != saveValue) {
                        if (result.length - deleteCount > 1) {
                            cf.delete = true;
                            deleteCount++;
                        } else {
                            end = true;
                            break;
                        }
                    }
                }
                if (end) {
                    break;
                }
            }
        }
        boolean[] canDelete = new boolean[result.length];
        Arrays.fill(canDelete, true);
        if (!this.doNotDeleteDirectories.isEmpty()) {
            for (String directory : this.doNotDeleteDirectories) {
                for (int i = 0; i < result.length; i++) {
                    canDelete[i] = !result[i].cacheAbsolutePath.startsWith(directory);
                }
            }
        }
        for (int i = 0; i < result.length; i++) {
            if (!canDelete[i]) {
                result[i].delete = false;
            }
        }
        if (this.saveOnlyOne) {
            boolean saved = false;
            for (int i = 0; i < result.length; i++) {
                CheckedFile cf = result[i];
                if (cf.delete) {
                    continue;
                }
                if (saved && canDelete[i]) {
                    cf.delete = true;
                } else {
                    saved = true;
                }
            }
        }
        //additional final control
        boolean atLeastOneSave = false;
        for (CheckedFile checkedFile : result) {
            if (!checkedFile.delete) {
                atLeastOneSave = true;
                break;
            }
        }
        if (!atLeastOneSave) {
            result[0].delete = false;
        }
        return result;
    }

    public DuplicateDeleteSolver(List<Rule> saveRules, boolean saveOnlyOne, List<String> savePreferDirectories, List<String> doNotDeleteDirectories) {
        if (saveRules != null) {
            this.rules.addAll(saveRules);
        }
        this.saveOnlyOne = saveOnlyOne;
        if (savePreferDirectories != null) {
            this.savePreferDirectories.addAll(savePreferDirectories);
        }
        if (doNotDeleteDirectories != null) {
            this.doNotDeleteDirectories.addAll(doNotDeleteDirectories);
        }
    }
}
