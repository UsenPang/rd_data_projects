package 博泰车联网.凭证下载;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.sax.handler.RowHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Download {
    public static void main(String[] args) {
        String pdfExcel = "E:\\博泰车联网\\解析文件下载\\控制表\\下载汇总.xlsx";
        String imgExcel = "E:\\博泰车联网\\解析文件下载\\控制表\\rpa_image.xlsx";
        String root = "E:";

        String imgURLPrefix = "https://rd-rpa.oss-cn-beijing.aliyuncs.com/";
        Pattern p = Pattern.compile("(收|付|转)");

        ExcelReader pdfReader = ExcelUtil.getReader(pdfExcel);

        List<Map<String, Object>> pdfList = pdfReader.readAll();
        List<Map<String, Object>> imgList = new ArrayList<>();

        Map<String, List<Map<String, Object>>> pdf_imgMap = new HashMap<>();


        ExcelUtil.readBySax(imgExcel, 0, new RowHandler() {
            @Override
            public void handle(int sheetIndex, long rowIndex, List<Object> rowList) {
                if (rowIndex == 0)
                    return;
                Map<String, Object> rowMap = toMap(rowList);
                String fileId = rowMap.get("file_id").toString();
                boolean isMatch = pdfList.parallelStream().anyMatch(pdfRow -> pdfRow.get("id").toString().equals(fileId));
                if (isMatch)
                    imgList.add(rowMap);
            }
        });

        //获取pdf文件路径，以及路径下的jpg图片
        pdfList.parallelStream().forEach(pdfRow -> {
            String id = pdfRow.get("id").toString();
            String fileName = pdfRow.get("file_name").toString();
            String relativePath = pdfRow.get("local_path").toString();
            Matcher m = p.matcher(fileName);
            if (!m.find())
                System.out.println("未匹配到:\t" + relativePath + File.separator + fileName);
            String storePath = root + File.separator + relativePath + File.separator + m.group() + File.separator + FileUtil.getPrefix(fileName);
            List<Map<String, Object>> list = imgList.parallelStream().filter(imgRow -> imgRow.get("file_id").toString().equals(id)).collect(Collectors.toList());
            pdf_imgMap.put(storePath, list);
        });


        //下载文件
        pdf_imgMap.forEach((path, list) -> {
            for (Map<String, Object> imgRow : list) {
                long page = (long) imgRow.get("pdf_page");
                String imgURL = imgURLPrefix + imgRow.get("image_oss_path").toString();
                String htmlURL = imgURLPrefix + imgRow.get("html_oss_path").toString();
                String imgPath = path + File.separator + FileUtil.getName(path) + ".pdf_" + page + ".jpg";
                String htmlPath = path + File.separator + FileUtil.getName(path) + ".pdf_" + page + ".jpg.html";
                if(!FileUtil.exist(imgPath)){
                    OkHttpUtil.asyncDownload(imgURL, imgPath);
                }
                if(!FileUtil.exist(htmlPath)){
                    OkHttpUtil.asyncDownload(htmlURL, htmlPath);
                }
            }
        });
    }


    public static Map<String, Object> toMap(List<Object> rowList) {
        String[] heads = {"id", "file_id", "org_id", "project_id", "image_name", "image_oss_path", "html_oss_path", "pdf_page"};
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < heads.length; i++) {
            map.put(heads[i], rowList.get(i));
        }
        return map;
    }
}
