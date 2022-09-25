package 博纳斯威.BN20220920合同单号提取.executor;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.Data;
import utils.CommonUtil;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class SimpleExecutor {
    private String source;
    private String excelPath;
    private String sheet;
    private FileFilter filter;
    private SimpleHandler handler;
    private List<Map<String, Object>> rows;


    public SimpleExecutor() {
    }


    public SimpleExecutor(String source, String excelPath, FileFilter filter, SimpleHandler handler) {
        this.source = source;
        this.excelPath = excelPath;
        this.filter = filter;
        this.handler = handler;
    }

    public void setExcelPath(String excelPath, String sheet) {
        this.excelPath = excelPath;
        this.sheet = sheet;
    }

    public void run() {
        List<File> fileList = FileUtil.loopFiles(source, filter);

        rows = new ArrayList<>();
        for (File file : fileList) {
            Map<String, Object> row = handler.handleKeyWord(file);
            if (row != null)
                rows.add(row);
        }
    }

    public void appendRows(List<Map<String, Object>> target) {
        rows.addAll(target);
    }


    public void writeResult() {
        ExcelWriter writer = CommonUtil.isEmpty(sheet) ? ExcelUtil.getWriter(excelPath) : ExcelUtil.getWriter(excelPath, sheet);
        writer.write(rows);
        writer.close();
    }
}
