package app.common;

import lombok.Data;
import setup.Config;
import util.Encrypt;
import util.FileUt;

@Data
public class AppConfig {
    long minimumRevision;

    private static AppConfig instance;

    public static AppConfig get() {
        if (instance == null) {
            instance = Encrypt.findJson(AppConfig.class, Config.get.appConfigFilePath);
            if (instance == null) {
                instance = new AppConfig();
            }
        }
        return instance;
    }


    public void setMinimumRevision(long minimumRevision) {
        this.minimumRevision = minimumRevision;
        save();
    }

    public void save() {
        Encrypt.saveJson(this, Config.get.databasesPath);
    }

    public void reset() {
        this.minimumRevision = 0;
        FileUt.deleteFile(Config.get.appConfigFilePath);
    }
}
