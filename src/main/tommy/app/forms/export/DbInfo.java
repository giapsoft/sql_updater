package app.forms.export;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DbInfo {
    String id;
    String url;
    String user;
    String password;
    Long lastRevision;
    boolean isCtrl;

    public Long getLastRevision() {
        if(lastRevision == null) {
            lastRevision = 0l;
        }
        return lastRevision;
    }
}
