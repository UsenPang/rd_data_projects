package 博纳斯威.合同下载;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.sax.handler.RowHandler;
import utils.PathUtil;
import 博泰车联网.凭证下载.OkHttpUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Download {
    private static List<String> heads;

    public static void main(String[] args) {
        String pdfExcel = "C:\\Users\\荣大\\Desktop\\博纳斯威采购合同下载控制表\\rpa_file(1).xlsx";
        String imgExcel = "C:\\Users\\荣大\\Desktop\\博纳斯威采购合同下载控制表\\rpa_image(1).xlsx";
        String root = "F:\\博纳斯威采购合同下载";

        String imgURLPrefix = "https://rd-rpa.oss-cn-beijing.aliyuncs.com/";


        ExcelReader pdfReader = ExcelUtil.getReader(pdfExcel);

        List<Map<String, Object>> pdfList = pdfReader.readAll();
        List<Map<String, Object>> imgList = new ArrayList<>();

        Map<String, List<Map<String, Object>>> pdf_imgMap = new HashMap<>();


        ExcelUtil.readBySax(imgExcel, 0, new RowHandler() {
            @Override
            public void handle(int sheetIndex, long rowIndex, List<Object> rowList) {
                if (rowIndex == 0) {
                    heads = rowList.stream().map(obj -> obj.toString()).collect(Collectors.toList());
                    return;
                }

                Map<String, Object> rowMap = toMap(rowList, heads);
                String fileId = rowMap.get("file_id").toString();
                boolean isMatch = pdfList.stream().anyMatch(pdfRow -> pdfRow.get("id").toString().equals(fileId));
                if (isMatch)
                    imgList.add(rowMap);
            }
        });


        //获取pdf文件路径，以及路径下的jpg图片
        pdfList.parallelStream().forEach(pdfRow -> {
            String file_id = pdfRow.get("id").toString();
            String fileName = pdfRow.get("file_name").toString();
            String relativePath = pdfRow.get("local_path").toString();
            PathUtil.PathBuilder pathBuilder = PathUtil.newPathBuilder();

            String storePath = pathBuilder.append(root)
                    .appendChild(relativePath)
                    .appendChild(FileUtil.getPrefix(fileName))
                    .build();

            List<Map<String, Object>> list = imgList.stream().filter(imgRow -> imgRow.get("file_id").toString().equals(file_id)).collect(Collectors.toList());
            pdf_imgMap.put(storePath, list);
        });


        //下载文件
        pdf_imgMap.forEach((path, list) -> {
            for (Map<String, Object> imgRow : list) {
                long page = (long) imgRow.get("pdf_page");
                String imgURL = imgURLPrefix + imgRow.get("image_oss_path").toString();
                String htmlURL = imgURLPrefix + imgRow.get("html_oss_path").toString();
                String filePrefix = FileUtil.getName(path);

                String imgPath = PathUtil.newPathBuilder()
                        .append(path)
                        .appendChild(filePrefix)
                        .append(".pdf_" + page + ".jpg")
                        .build();

                String htmlPath = imgPath + ".html";
                if (!FileUtil.exist(imgPath)) {
                    OkHttpUtil.asyncDownload(imgURL, imgPath);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!FileUtil.exist(htmlPath)) {
                    OkHttpUtil.asyncDownload(htmlURL, htmlPath);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

//    public List<Map<String, Object>> readBySax(String excelPath) {
//        List<String> heads = null;
//        List<Map<String, Object>> rows = new ArrayList<>();
//        ExcelUtil.readBySax(excelPath, 0, new RowHandler() {
//            @Override
//            public void handle(int sheetIndex, long rowIndex, List<Object> rowList) {
//                if (rowIndex == 0) {
//                    heads = rowList.stream().map(obj -> obj.toString()).collect(Collectors.toList());
//                    return;
//                }
//               rows.add(toMap(rowList,heads));
//            }
//        });
//        return rows;
//    }


    public static Map<String, Object> toMap(List<Object> rowList, List<String> heads) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < heads.size(); i++) {
            map.put(heads.get(i), rowList.get(i));
        }
        return map;
    }
}
