package setup;

import app.common.DataType;
import util.StringUt;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Config {
    public static Config get;

    public Config(String svnDir) {
        this.svnDir = svnDir;
        appHistoryFilePath = joinDir(svnDir, "data", "app_history.store");
        appConfigFilePath = joinDir(svnDir, "data", "app_config.store");
        workingDir = joinDir(System.getProperty("java.io.tmpdir"), "sql-extractor");
        workingFilePath = joinDir(workingDir, "working.store");
        databasesPath = joinDir(workingDir, "databases.store");
        updateErrorsFilePath = joinDir(workingDir, "errors", "update_errors.log");
        captureErrorPath = joinDir(workingDir, "errors", "capture_errors.log");
        get = this;
    }

    public final String appConfigFilePath;
    static String joinDir(String... parts) {
        return Arrays.stream(parts).flatMap(p -> Arrays.stream(p.split("\\\\"))).filter(s -> !s.trim().isEmpty()).collect(Collectors.joining("\\"));
    }
    public final String captureErrorPath;
    public final String svnDir;
    public final String appHistoryFilePath;
    public final String workingFilePath;
    public final String workingDir;
    public final String databasesPath;

    public String tableFilePath(String catName, DataType dataType, String tableName) {
        return joinDir(svnDir, "data", StringUt.lower_underline(catName), dataType.name(), StringUt.lower_underline(tableName) + ".tbl_store");
    }

    public boolean isNotTableFilePath(String filePath) {
        return filePath == null || !filePath.endsWith(".tbl_store");
    }

    public final String updateErrorsFilePath;
}
