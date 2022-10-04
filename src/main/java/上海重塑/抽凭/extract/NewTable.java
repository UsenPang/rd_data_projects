package 上海重塑.抽凭.extract;

import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewTable {
    public static void main(String[] args) {
        String excelIn = "E:\\上海重塑能源\\抽凭控制表\\2021年(1).xlsx";
        String excelOut = "E:\\上海重塑能源\\抽凭控制表\\2021年抽凭控制表.xlsx";

        List<Map<String,Object>> outRows = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String sheet = i+"月";
            ExcelReader reader = ExcelUtil.getReader(new File(excelIn),sheet);
            List<Map<String, Object>> rows = reader.readAll();
            reader.close();

            for (Map<String, Object> row : rows) {
                String num = row.get("凭证号码").toString();
                int preNum = Integer.parseInt(num.substring(0,2));
                row.put("前缀码",preNum);
            }
            outRows.addAll(rows);
        }

        BigExcelWriter bigWriter = ExcelUtil.getBigWriter(excelOut);
        bigWriter.write(outRows);
        bigWriter.close();
    }
}
