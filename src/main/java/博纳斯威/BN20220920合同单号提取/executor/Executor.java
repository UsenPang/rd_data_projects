package 博纳斯威.BN20220920合同单号提取.executor;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.Data;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class Executor {
    private String source;
    private String excelPath;
    private FileFilter filter;
    private MultiHandler handler;
    private List<Map<String,Object>> rows;

    public Executor(){}

    public Executor(String source, String excelPath, FileFilter filter, MultiHandler handler) {
        this.source = source;
        this.excelPath = excelPath;
        this.filter = filter;
        this.handler = handler;
    }




    public void run(){
        List<File> fileList = FileUtil.loopFiles(source,filter);
        rows = new ArrayList<>();
        for (File file : fileList) {
            rows.addAll(handler.handleKeyWord(file));
        }
    }

    public void appendRows(List<Map<String,Object>> target){
        rows.addAll(target);
    }


    public void writeResult(){
        ExcelWriter writer = ExcelUtil.getWriter(excelPath);
        writer.write(rows);
        writer.close();
    }
}
