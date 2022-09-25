package 江西宁新.NX20220825抽取.executor;

import lombok.Data;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class AbstractHandler implements MultiHandler{
    private String[] headKey;
    private Pattern pPage = Pattern.compile(".pdf_(\\d+)");

    public Map<String,Object> getRow(File file){
        Map<String,Object> row = new HashMap<>();

        for (String key : headKey) {
            row.put(key,"");
        }

        String path = file.getParentFile().getParent();
        String fileName = file.getName().replaceAll("\\.pdf_\\d+.*",".pdf");

        row.put("文件路径信息",path);
        row.put("文件名称",fileName);
        Matcher mPage = pPage.matcher(file.getName());
        if(mPage.find())
            row.put("页码","P"+mPage.group(1));

        fileName = fileName.replace(".pdf","");

        row.put("凭证号",fileName.substring(fileName.indexOf('-')+1));
        return row;
    }

    @Override
    public List<Map<String, Object>> handleKeyWord(File html) {
        return null;
    }
}
