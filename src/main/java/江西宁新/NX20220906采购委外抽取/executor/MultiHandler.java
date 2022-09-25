package 江西宁新.NX20220906采购委外抽取.executor;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface MultiHandler {
    List<Map<String,Object>> handleKeyWord(File html);
}
