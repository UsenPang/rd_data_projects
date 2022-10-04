package 上海重塑.抽凭.extract;

import demons.抽凭.ExtractVoucher;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CommonUtil;

import java.io.File;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Extract {
    public static void main(String[] args) throws Exception {

        String sourceDir = "E:\\上海重塑能源\\会计凭证";
        String dest = "E:\\上海重塑能源\\抽凭结果\\凭证文件";
        String excelSource = "E:\\上海重塑能源\\抽凭控制表\\2021年抽凭控制表.xlsx";
        String excelDest = "E:\\上海重塑能源\\抽凭结果\\统计表\\2021年抽凭统计表.xlsx";


        ExtractVoucher extract = new ExtractVoucher();
        extract.setVoucherMatcher(Extract::match);
        extract.setComparator(getComparator2());
        extract.setSource(sourceDir);
        extract.setDest(dest);
        extract.setExcelSource(excelSource);
        extract.setExcelDest(excelDest);
        extract.setOutPathKey("名称", "前缀码", "年度", "月度");
        extract.setVoucherHead("凭证号码");
        extract.setVoucherName("文件名称");
        extract.handleSinglePdf();
    }


    public static String match(Elements els) {
        String allContent = els.text();
        if (!allContent.contains("记账凭证") && !allContent.contains("上海重塑")) return null;
        Pattern pattern = Pattern.compile("(SAP)?凭?证编号:(\\d+)");
        for (Element p : els) {
            Matcher m = pattern.matcher(CommonUtil.cleanBlank(p.text()));
            if (m.find()) return m.group(2);
        }
        return null;
    }


    public static Comparator getComparator2() {
        Pattern p = Pattern.compile("\\.pdf_(\\d+)");
        return Comparator.comparingInt(f -> {
            File file = (File) f;
            Matcher m = p.matcher(file.getName());
            if (m.find())
                return Integer.parseInt(m.group(1));
            return 0;
        });
    }


    public static Comparator getComparator() {
        Pattern p1 = Pattern.compile("上海重塑(\\d+)");
        Pattern p2 = Pattern.compile("第(\\d+)册");
        Pattern p3 = Pattern.compile("第(\\d+)本");
        Pattern p4 = Pattern.compile("\\.pdf_(\\d+)");

        return Comparator.comparingInt(f -> {
            File file = (File) f;
            String fileName = file.getName();
            Matcher m = p1.matcher(fileName);
            if (m.find())
                return Integer.parseInt(m.group(1));
            return 0;
        }).thenComparingInt(f -> {
            File file = (File) f;
            String fileName = file.getName();
            Matcher m = p2.matcher(fileName);
            if (m.find())
                return Integer.parseInt(m.group(1));
            return 0;
        }).thenComparingInt(f -> {
            File file = (File) f;
            String fileName = file.getName();
            Matcher m = p3.matcher(fileName);
            if (m.find())
                return Integer.parseInt(m.group(1));
            return 0;
        }).thenComparingInt(f -> {
            File file = (File) f;
            String fileName = file.getName();
            Matcher m = p4.matcher(fileName);
            if (m.find())
                return Integer.parseInt(m.group(1));
            return 0;
        });
    }


}
