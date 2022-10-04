package 宁波晶钻.JZ20220929凭证页拆分;

import demons.抽凭.ExtractVoucher;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.StringUtil;



import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws Exception {
        String source = "E:\\宁波晶钻\\凭证拆分\\原文件\\新材料";
        String dest = "E:\\宁波晶钻\\凭证拆分\\输出文件";
        String excelIn = "E:\\宁波晶钻\\凭证拆分\\控制表\\新材料.xlsx";
        String excelOut = "E:\\宁波晶钻\\凭证拆分\\统计表\\新材料2.xlsx";
        Map<String, String> map = getMap();
        for (String year : map.keySet()) {
            String sheet = map.get(year);
            String sourceDir = source + File.separator + year;
//            String sourceDir = "E:\\宁波晶钻\\凭证拆分\\原文件\\新材料\\2019";
            ExtractVoucher extractor = new ExtractVoucher();
            extractor.setVoucherMatcher(Main::match);
            extractor.setComparator(createComparator());
            extractor.setSource(sourceDir);
            extractor.setDest(dest);
            extractor.setSheetIn(sheet);
            extractor.setSheetOut(sheet);
            extractor.setExcelSource(excelIn);
            extractor.setExcelDest(excelOut);
            extractor.setOutPathKey("对应主体", "年", "月");
            extractor.setVoucherHead("索引号");
            extractor.setVoucherName("文件命名");
            extractor.handleAllPdf();
        }


    }


    public static String match(Elements els){
        String allContent = els.text();
        if (!allContent.contains("记账凭证")) return null;
        Pattern pattern = Pattern.compile("第(\\d+)号");
        Pattern pattern1 = Pattern.compile("\\d{2}年\\d{1,2}月");
        String date = "";
        String voucher = "";
        for (Element p : els) {
            String text = StringUtil.rmBlank(p.text());
            Matcher m = pattern.matcher(text);
            Matcher m2 = pattern1.matcher(text);
            if (m.find()) voucher = m.group();
            if(m2.find()) date = m2.group();
        }
        return date+voucher;
    }

    public static Map<String, String> getMap() {
        Map<String, String> yearMap = new HashMap<>();
        yearMap.put("2019", "新材料19年");
        yearMap.put("2020", "新材料20年");
        yearMap.put("2021", "新材料21年");
        yearMap.put("2022", "新材料22年");
        return yearMap;
    }

    public static Comparator createComparator() {
        Pattern p = Pattern.compile("(\\d{4})\\.(\\d{2})-(\\d{4})\\.(\\d{2})");
        Pattern p2 = Pattern.compile("\\.(PDF|pdf)_(\\d+)");
        return Comparator.comparingInt(f -> {
            File file = (File) f;
            Matcher m = p.matcher(file.getName());
            if (m.find())
                return Integer.parseInt(m.group(1));
            return 0;
        }).thenComparingInt(f -> {
            File file = (File) f;
            Matcher m = p.matcher(file.getName());
            if (m.find())
                return Integer.parseInt(m.group(2));
            return 0;
        }).thenComparingInt(f->{
            File file = (File) f;
            Matcher m = p2.matcher(file.getName());
            if (m.find())
                return Integer.parseInt(m.group(2));
            return 0;
        });
    }
}
