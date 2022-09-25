package demons.抽取;

import java.io.File;
import java.util.List;

public interface MultiHandler {
    void handle(List<File> files, Context context);
}
