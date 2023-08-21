package app.common;

import lombok.Data;

@Data
public class CaptureInfo {
    String ctrlUser;
    String ctrlPass;
    String ctrlUrl;
    String mainUser;
    String mainPass;
    String mainUrl;
}
