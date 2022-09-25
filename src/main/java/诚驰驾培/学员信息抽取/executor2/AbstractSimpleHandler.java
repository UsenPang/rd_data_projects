package 诚驰驾培.学员信息抽取.executor2;

import lombok.Data;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class AbstractSimpleHandler implements KeyWordHandler {
    private String[] headKey;

    public Map<String,Object> getRow(File file){
        Map<String,Object> row = new HashMap<>();

        for (String key : headKey) {
            row.put(key,"");
        }

        String fileName = file.getName().replaceAll("\\.pdf_\\d+.*",".pdf");
        row.put("PDF文件名称",fileName);
        return row;
    }


    @Override
    public Map<String, Object> handleKeyWord(String key, List<File> htmls) {
        return null;
    }
}
