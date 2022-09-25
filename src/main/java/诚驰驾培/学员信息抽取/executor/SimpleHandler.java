package 诚驰驾培.学员信息抽取.executor;

import java.io.File;
import java.util.Map;

public interface SimpleHandler {
    Map<String,Object> handleKeyWord(File html);
}
