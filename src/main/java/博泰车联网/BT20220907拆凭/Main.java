package 博泰车联网.BT20220907拆凭;


import demons.抽凭.ExtractVoucher;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CommonUtil;

import java.io.File;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static String word;
    public static void main(String[] args) throws Exception {
        Pattern p = Pattern.compile("(\\d{4})-(\\d{1,2})-(收|付|转)");
        String sourcePath = "E:\\博泰车联网\\博泰车联网拆凭\\解析文件";
        String destPath = "G:\\博泰车联网拆凭\\输出文件";
        String excelSource = "E:\\博泰车联网\\博泰车联网拆凭\\控制表\\2020年重新输出";
        String excelDest = "G:\\博泰车联网拆凭\\统计表\\2020年";


        File tableSource = new File(excelSource);
        File[] tables = tableSource.listFiles();
        for (File table : tables) {
            String tableName = table.getName();
            Matcher m = p.matcher(tableName);
            if (!m.find())
                continue;

            String year = m.group(1) + "年";
            String month = m.group(2) + "月";
            word = m.group(3);

            String source = sourcePath + File.separator + year + File.separator + month + File.separator + word;


            ExtractVoucher extractor = new ExtractVoucher();
            extractor.setSource(source);
            extractor.setDest(destPath);
            extractor.setExcelSource(table.getPath());
            extractor.setExcelDest(excelDest+File.separator+tableName);
            extractor.setComparator(getComParator());
            extractor.setVoucherHead("凭证号处理");
            extractor.setVoucherMatcher(Main::findVoucher);
            extractor.setOutPathKey("主体", "年", "月", "凭证字");
            extractor.handleAllPdf();
        }

    }


    public static Comparator getComParator() {
        Pattern p1 = Pattern.compile("(\\d+)册");
        Pattern p2 = Pattern.compile("\\.pdf_(\\d+)");

        return Comparator.comparingInt(f -> {
            File file = (File) f;
            Matcher m1 = p1.matcher(file.getName());
            if (m1.find())
                return Integer.parseInt(m1.group(1));
            return 0;
        }).thenComparingInt(f -> {
            File file = (File) f;
            Matcher m2 = p2.matcher(file.getName());
            if (m2.find())
                return Integer.parseInt(m2.group(1));
            return 0;
        });
    }


    public static String findVoucher(Elements pEls){

        String content = pEls.toString();
        Pattern p = Pattern.compile("("+word+"\\s*\\d+)\\s+\\d+/\\d+");
        Pattern p2 = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})日");
        Matcher m = p.matcher(content);
        Matcher m2 = p2.matcher(content);

        if(!m2.find())
            return null;

        if(m.find())
            return m2.group()+ CommonUtil.cleanBlank(m.group(1));

        content = "";
        for (Element pEl : pEls) {
            content +=pEl.text()+" ";
        }

        m = p.matcher(content);
        if(m.find())
            return m2.group()+CommonUtil.cleanBlank(m.group(1));

        return null;
    }
}
