package 上海重塑.抽凭.extract;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;

import java.io.File;
import java.util.List;
import java.util.Map;

public class NewTable {
    public static void main(String[] args) {
        String excelIn = "E:\\上海重塑能源\\抽凭控制表\\控制表 - 副本";

        File fin = new File(excelIn);
        File[] files = fin.listFiles();
        for (File file : files) {
            ExcelReader reader = ExcelUtil.getReader(file);
            List<Map<String, Object>> rows = reader.readAll();
            reader.close();
            for (Map<String, Object> row : rows) {
                String num = row.get("凭证号码").toString();
                int preNum = Integer.parseInt(num.substring(0,2));
                row.put("前缀码",preNum);
            }

            file.delete();
            ExcelWriter writer = ExcelUtil.getWriter(file);
            writer.write(rows);
            writer.close();
        }


    }
}
