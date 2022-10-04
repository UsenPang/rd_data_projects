package 上海重塑.抽凭.download;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import utils.OkHttpUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

public class Download {
    public static void main(String[] args) {
        String excelIn = "E:\\上海重塑能源\\文件下载控制表\\rpa_image(2).xlsx";
        String root = "E:\\上海重塑能源\\下载文件";
        String imgURLPrefix = "https://rd-rpa.oss-cn-beijing.aliyuncs.com/";

        ExcelReader reader = ExcelUtil.getReader(excelIn);
        List<Map<String, Object>> imgList = reader.readAll();
        reader.close();


        imgList.forEach(imgRow -> {
            long page = (long) imgRow.get("pdf_page");
            String fileName = imgRow.get("file_name").toString();
            String path = root + File.separator + imgRow.get("local_path") + File.separator + FileUtil.getPrefix(fileName);

            String imgURL = imgURLPrefix + imgRow.get("image_oss_path").toString();
            String htmlURL = imgURLPrefix + imgRow.get("html_oss_path").toString();
            String imgPath = path + File.separator + fileName + "_" + page + ".jpg";
            String htmlPath = path + File.separator + fileName + "_" + page + ".jpg.html";
            if (!FileUtil.exist(imgPath)) {
                OkHttpUtil.asyncDownload(imgURL, imgPath);
            }
            if (!FileUtil.exist(htmlPath)) {
                OkHttpUtil.asyncDownload(htmlURL, htmlPath);
            }
        });

    }
}
