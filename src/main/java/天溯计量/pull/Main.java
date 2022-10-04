package 天溯计量.pull;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import utils.OkHttpUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        File file = new File("E:\\企业文件下载\\文件下载控制表");
//        String[] sheets = {"20年", "21年", "22年"};
        String[] sheets = {"20年","21年","22年"};
        File[] tables = file.listFiles();
        String dest = "G:\\潘永胜\\天溯计量";

        for (File table : tables) {
            String tableName = table.getName();
            String main = tableName.contains("TS") ? "天溯" : "中测";
            String destDir = dest + File.separator + main;
            for (String sheet : sheets) {
                ExcelReader reader = ExcelUtil.getReader(table, sheet);
                List<Map<String, Object>> rows = reader.readAll();
                reader.close();
                toDownload(rows, destDir);
            }
        }
    }


    public static void toDownload(List<Map<String, Object>> rows, String destDir) {
        for (Map<String, Object> row : rows) {
            String year = row.get("年份").toString();
            String type = row.get("附件类型").toString();
            String number = row.get("报价单号").toString();
            String url = row.get("文件路径").toString();

            String filePath = destDir + File.separator + type + File.separator + year + File.separator + number + File.separator + getNameFromURL(url);
            File file = new File(filePath);
            if (file.exists())
                continue;

            OkHttpUtil.asyncDownload(url,filePath);

//            try {
//                OkHttpUtil.download(url, filePath);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    public static String getNameFromURL(String url) {
        int index = url.lastIndexOf('/');
        return url.substring(index + 1);
    }
}
