package 博纳斯威.BN20220920合同单号提取.executor;

import java.io.File;
import java.util.Map;

public interface SimpleHandler {
    Map<String,Object> handleKeyWord(File html);
}
