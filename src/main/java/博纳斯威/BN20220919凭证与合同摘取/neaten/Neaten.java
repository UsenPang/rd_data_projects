package 博纳斯威.BN20220919凭证与合同摘取.neaten;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import utils.CommonUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Neaten {
    public static void main(String[] args) throws IOException {
        String excelSource = "C:\\Users\\荣大\\Desktop\\BN20220919博纳斯威凭证、合同摘取(1)\\2-3-5存货采购抽凭测试1.xls";
        String excelDest = "D:\\工作空间\\博纳斯威\\凭证与合同整理\\合同命中统计表.xlsx";
        String source = "D:\\工作空间\\博纳斯威\\采购合同";
        String dest = "D:\\工作空间\\博纳斯威\\凭证与合同整理\\存货、采购抽凭测试";
        Pattern p1 = Pattern.compile("(\\d{4})\\s*年");
        Pattern p2 = Pattern.compile("(\\d{4})-\\d{1,2}-\\d{1,2}");

        ExcelReader reader = ExcelUtil.getReader(excelSource);
        List<Map<String, Object>> rows = reader.readAll();
        reader.close();

        Multimap<String,File> multiFileMap = ArrayListMultimap.create();

        Files.walkFileTree(new File(source).toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File pdfFile = file.toFile();
                String fileName = pdfFile.getName();
                Matcher m = p1.matcher(fileName);
                if(m.find())
                    multiFileMap.put(m.group(1),pdfFile);
                return FileVisitResult.CONTINUE;
            }
        });


        rows.forEach(row->{
            Matcher m2 = p2.matcher(row.get("日期").toString());
            //获取表格行中的年份信息
            String year = m2.find() ? m2.group(1) : null;

            Collection<File> files = multiFileMap.get(year);
            for (File file : files) {
                String conpany = CommonUtil.cleanBlank(row.get("摘要").toString()).replaceAll(" ","");
                String fileName = file.getName();
                if(!fileName.contains(conpany))
                    continue;

                Object sum = row.get("命中统计");
                sum = sum == null ? "1" : Integer.parseInt(sum.toString()) + 1;
                row.put("命中统计", sum);

                String voucherNum = row.get("凭证号").toString();
                String toPath = dest + File.separator + voucherNum + File.separator + fileName;
                FileUtil.copy(file, new File(toPath), false);
            }
        });



        for (Map<String, Object> row : rows) {
            if (row.get("命中统计") == null)
                row.put("命中统计", 0);
        }


        ExcelWriter writer = ExcelUtil.getWriter(excelDest);
        writer.write(rows);
        writer.close();
    }
}
