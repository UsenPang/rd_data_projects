package demons.抽取;

import cn.hutool.core.collection.CollUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Context {
    private Extractor extractor;

    public Context(Extractor extractor){
        this.extractor = extractor;
    }

    private ThreadLocal<Map<Object,Object>> threadLocal = new ThreadLocal<>();

    public Object get(Object key){
        Map<Object,Object> currentMap = threadLocal.get();
        if(CollUtil.isEmpty(currentMap)) return null;
        return currentMap.get(key);
    }

    public Object put(Object key,Object value){
        Map<Object,Object> currentMap = threadLocal.get();
        if(CollUtil.isEmpty(currentMap)){
            currentMap = new HashMap<>();
            threadLocal.set(currentMap);
        }
        return currentMap.put(key,value);
    }

    public List<Map<String,Object>> getInputRows(){
        return this.extractor.getRows();
    }

    public void write(Map<String,Object> row){
        this.extractor.getOutRows().add(row);
    }
}
