package 天溯计量.TS20220923结算单抽取;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.getter.OptNullBasicTypeFromStringGetter;
import cn.hutool.core.io.FileUtil;
import demons.抽取.Extractor;

import java.io.File;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractPdf {
    static String[] heads = {"仪器名称","规格","数量","单价","PDF文件名称","客户名称","结算单号","金额小计（含税）","金额合计（含税）","金额合计（不含税）"};
    public static void main(String[] args) {
        String src = "E:\\天溯计量PDF\\天溯\\结算单附件";
        String excelOut = "E:\\天溯计量PDF\\天溯\\结算单附件\\天溯计量结算单抽取-2021-PDF.xlsx";
        Extractor extractor = new Extractor();
        extractor.setSource(src);
        extractor.setExcelDest(excelOut);
        extractor.setTableHeadWords(heads);
        extractor.setComparator(getComparator());
        extractor.setMultiHandler(new PdfHandler());
        extractor.handleMultiFile();
    }



    public static Comparator getComparator() {
        Pattern p = Pattern.compile("(pdf|PDF)_(\\d+)");
        return Comparator.comparingInt(htmlFile -> {
            File file = (File) htmlFile;
            String name = file.getName();
            Matcher m = p.matcher(name);
            if(m.find())
                return Integer.parseInt(m.group(2));
            return 0;
        });
    }
}
