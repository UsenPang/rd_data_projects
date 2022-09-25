package demons.抽取;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Context {
    private Extractor extractor;

    public Context(Extractor extractor){
        this.extractor = extractor;
    }

    Map<Object,Object> contextMap = new HashMap<>();

    public Object get(Object key){
        return contextMap.get(key);
    }

    public Object put(Object key,Object value){
        return contextMap.put(key,value);
    }

    public List<Map<String,Object>> getInputRows(){
        return this.extractor.getRows();
    }

    public void write(Map<String,Object> row){
        this.extractor.getOutRows().add(row);
    }
}
