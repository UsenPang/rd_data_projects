package 诚驰驾培.学员信息抽取.executor2;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.Data;
import utils.RdFileUtil;
import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class Executor {
    private String source;
    private String excelPath;
    private FileFilter filter;
    private KeyWordHandler handler;

    public Executor(){}


    public Executor(String source, String excelPath, FileFilter filter, KeyWordHandler handler) {
        this.source = source;
        this.excelPath = excelPath;
        this.filter = filter;
        this.handler = handler;
    }




    public void run(){
        Map<String,List<File>> filesMap = RdFileUtil.lsAllFilesByType(new File(source),filter);

        List<Map<String,Object>> rows = new ArrayList<>();

        for (Map.Entry<String, List<File>> entry : filesMap.entrySet()) {
            String key = entry.getKey();
            List<File> files = entry.getValue();
            sort(files);
            Map<String,Object> row = handler.handleKeyWord(key,files);
            rows.add(row);
        }

        ExcelWriter writer = ExcelUtil.getWriter(excelPath);
        writer.write(rows);
        writer.close();
    }

    private void sort(List<File> files){
        Pattern p = Pattern.compile("\\.pdf_(\\d+)");
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                Matcher m1 = p.matcher(o1.getName());
                Matcher m2 = p.matcher(o2.getName());
                if(m1.find() && m2.find()){
                    int num1 = Integer.parseInt(m1.group(1));
                    int num2 = Integer.parseInt(m2.group(1));
                    return num1-num2;
                }
                return 0;
            }
        });
    }
}
