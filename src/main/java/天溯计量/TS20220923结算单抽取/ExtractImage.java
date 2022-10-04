package 天溯计量.TS20220923结算单抽取;

import demons.抽取.Extractor;

import java.util.Comparator;

public class ExtractImage {
    static String[] heads = {"仪器名称","规格","数量","单价","图片文件名称","客户名称","结算单号","金额小计（含税）","金额合计（含税）","金额合计（不含税）"};
    public static void main(String[] args) {
        String src = "E:\\天溯计量PDF\\天溯\\结算单附件\\2021图片";
        String excelOut = "E:\\天溯计量PDF\\天溯\\结算单附件\\天溯计量结算单抽取-2021-图片.xlsx";
        Extractor extractor = new Extractor();
        extractor.setSource(src);
        extractor.setExcelDest(excelOut);
        extractor.setTableHeadWords(heads);
        extractor.setComparator(getComparator());
        extractor.setMultiHandler(new ImageHandler());
        extractor.handleMultiFile();
    }



    public static Comparator getComparator() {
        return Comparator.comparingInt(htmlFile -> 0);
    }
}
