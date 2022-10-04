package 上海重塑.抽凭.download;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.hutool.poi.excel.sax.handler.RowHandler;

import java.util.*;

public class NeatenTable {
    public static void main(String[] args) {
        String pdfExcel = "E:\\上海重塑能源\\文件下载控制表\\rpa_file.xls";
        String imgExcel = "E:\\上海重塑能源\\文件下载控制表\\rpa_image.xlsx";
        String newTablePath = "E:\\上海重塑能源\\文件下载控制表\\rpa_image(2).xlsx";

        ExcelReader pdfReader = ExcelUtil.getReader(pdfExcel);
        List<Map<String, Object>> pdfList = pdfReader.readAll();
        List<Map<String, Object>> imgList = new ArrayList<>();


        ExcelUtil.readBySax(imgExcel, 0, new RowHandler() {
            @Override
            public void handle(int sheetIndex, long rowIndex, List<Object> rowList) {
                if (rowIndex == 0) return;
                Map<String, Object> imgRow = toMap(rowList);
                String fileId = imgRow.get("file_id").toString();
                Optional<Map<String, Object>> optional = pdfList.parallelStream().filter(pdfRow -> pdfRow.get("id").toString().equals(fileId)).findFirst();
                if (optional.isPresent()) {
                    Map<String,Object> pdfRow = optional.get();
                    imgRow.put("file_name",pdfRow.get("file_name"));
                    imgRow.put("local_path",pdfRow.get("local_path"));
                    imgList.add(imgRow);
                }
            }
        });


        ExcelWriter writer = ExcelUtil.getWriter(newTablePath);
        writer.write(imgList);
        writer.close();
    }

    public static Map<String, Object> toMap(List<Object> rowList) {
        String[] heads = {"id", "file_id", "org_id", "project_id", "image_oss_path", "html_oss_path", "pdf_page"};
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < heads.length; i++) {
            map.put(heads[i], rowList.get(i));
        }
        return map;
    }
}
