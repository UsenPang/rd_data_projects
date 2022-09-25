package 博纳斯威.BN20220920合同单号提取.executor;

import cn.hutool.core.io.FileUtil;
import lombok.Data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class AbstractSimpleHandler implements SimpleHandler{
    private String[] headKey;
    private Pattern pPage = Pattern.compile(".pdf_(\\d+)");

    public Map<String,Object> getRow(File file){
        Map<String,Object> row = new HashMap<>();

        for (String key : headKey) {
            row.put(key,"");
        }

        String path = file.getParent();
        String fileName = file.getName().replaceAll("\\.pdf_\\d+.*","");

        row.put("文件路径信息",path);
        row.put("文件名称",fileName);
        Matcher mPage = pPage.matcher(file.getName());
        if(mPage.find())
            row.put("页码","P"+mPage.group(1));

        String voucher = FileUtil.getParent(file,2).getName();

        row.put("凭证号",voucher);
        return row;
    }

    @Override
    public Map<String, Object> handleKeyWord(File html) {
        return null;
    }
}
