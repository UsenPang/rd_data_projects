package utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileType {
    JPG("jpg"),
    PDF("pdf"),
    TXT("txt"),
    PNG("png"),
    HTML("html"),
    IMAGE("jpg jpeg png bmp JPG PNG JPEG");

    private String type;
}
