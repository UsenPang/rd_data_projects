package 诚驰驾培.学员信息抽取.executor;

import lombok.Data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Data
public abstract class AbstractSimpleHandler implements SimpleHandler{
    private String[] headKey;
    private Pattern pPage = Pattern.compile(".pdf_(\\d+)");

    public Map<String,Object> getRow(File file){
        Map<String,Object> row = new HashMap<>();

        for (String key : headKey) {
            row.put(key,"");
        }

        String fileName = file.getName().replaceAll("\\.pdf_\\d+.*",".pdf");
        row.put("PDF文件名称",fileName);
        return row;
    }
}
