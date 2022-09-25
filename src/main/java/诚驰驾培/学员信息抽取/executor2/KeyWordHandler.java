package 诚驰驾培.学员信息抽取.executor2;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface KeyWordHandler {
    Map<String,Object> handleKeyWord(String key, List<File> htmls);
}
