package 博纳斯威.BN20220919凭证与合同摘取.neaten;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Neaten2 {
    public static void main(String[] args) throws IOException {
        String excelIn = "C:\\Users\\荣大\\Desktop\\BN20220919博纳斯威凭证、合同摘取(1)\\2-3-5存货采购抽凭测试1.xls";
        String excelOut = "D:\\工作空间\\博纳斯威\\凭证与合同整理\\凭证命中统计表.xlsx";
        String source = "D:\\工作空间\\博纳斯威\\博纳斯威凭证抽取\\博纳斯威";
        String dest = "D:\\工作空间\\博纳斯威\\凭证与合同整理\\存货、采购抽凭测试";

        ExcelReader reader = ExcelUtil.getReader(excelIn);
        List<Map<String, Object>> rows = reader.readAll();
        reader.close();

        //初始化表格中的值
        rows = rows.stream().map(row -> {
            String voucher = row.get("凭证号").toString();
            row = new HashMap<>();
            row.put("凭证号", voucher);
            row.put("命中统计", 0);
            return row;
        }).collect(Collectors.toList());

        //将表格中数据使用map存放  key:凭照号  value：每一行的数据
        Map<String, Map<String, Object>> rowMap = new HashMap<>();
        rows.stream().forEach(row -> {
            String voucher = row.get("凭证号").toString();
            rowMap.put(voucher, row);
        });


        //文件命中凭证统计，命中的文件整理到指定目录下
        Files.walkFileTree(new File(source).toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File voucherFile = file.toFile();
                String voucherName = "博纳斯威-" + FileUtil.mainName(voucherFile).replaceAll("[年月]", "");
                if (rowMap.containsKey(voucherName)) {
                    Map<String, Object> row = rowMap.get(voucherName);
                    row.put("命中统计", 1);

                    String copyTo = voucherName + File.separator + voucherFile.getName();
                    FileUtil.copy(voucherFile, new File(dest, copyTo), true);
                }
                return FileVisitResult.CONTINUE;
            }
        });


        ExcelWriter writer = ExcelUtil.getWriter(excelOut);
        writer.write(rows);
        writer.close();
    }
}
