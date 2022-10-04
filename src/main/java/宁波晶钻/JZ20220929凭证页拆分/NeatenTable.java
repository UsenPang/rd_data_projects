package 宁波晶钻.JZ20220929凭证页拆分;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NeatenTable {
    public static void main(String[] args) {
        String excelIn = "E:\\宁波晶钻\\凭证拆分\\需求文档\\工业.xlsx";
        String excelOut = "E:\\宁波晶钻\\凭证拆分\\控制表\\工业.xlsx";
        String[] sheets = {"工业19年","工业20年","工业21年","工业22年"};
        Pattern p = Pattern.compile("(\\d{2}年)(\\d{1,2}月)");

        for (String sheet : sheets) {
            ExcelReader reader = ExcelUtil.getReader(new File(excelIn), sheet);
            List<Map<String, Object>> rows = reader.readAll();
            reader.close();

            for (Map<String, Object> row : rows) {
                String month = row.get("月份").toString();
                String voucher = row.get("凭证号").toString();
                Matcher m = p.matcher(month);
                if(!m.find()){
                    System.out.println(row);
                    continue;
                };
                String year = m.group(1);
                String month1 = m.group(2);
                row.put("年",year);
                row.put("月",month1);

                String name = "更新-"+month+"-"+voucher;
                row.put("文件命名",name);
                String index = month+voucher;
                row.put("索引号",index);
            }

            ExcelWriter writer = ExcelUtil.getWriter(excelOut, sheet);
            writer.write(rows);
            writer.close();
        }


    }
}
