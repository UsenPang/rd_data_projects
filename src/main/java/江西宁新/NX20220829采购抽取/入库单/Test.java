package 江西宁新.NX20220829采购抽取.入库单;


import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CommonUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) throws IOException {
//        Document document = Jsoup.parse(new File("E:\\江西宁新\\单据分类_新版解析\\宁和达\\2022年\\5月\\306\\采购入库单-宁和达-2022-5-记306\\采购入库单-宁和达-2022-5-记306.pdf_2.jpg.html"), "utf8");
//        List<Elements> elementsList = tableSplit(document);
//        for (Elements elements : elementsList) {
//            System.out.println("==========================================");
//            System.out.println(elements);
//        }
         Pattern pDate = Pattern.compile("日期:((\\d{4})年(\\d{1,2})月(\\d{1,2})日)");
        String target = CommonUtil.cleanBlank("翟进江 日期:2020年 7月24日");
        Matcher m = pDate.matcher(target);
        System.out.println(target);
        if(m.find())
            System.out.println(m.group());

    }


    public static List<Elements> tableSplit(Element document){
        Elements elements = document.select("p,table");
        List<Elements> elList = new ArrayList<>();
        Elements els = new Elements();
        for (Element element : elements) {
            els.add(element);
            if("table".equals(element.tagName())){
                elList.add(els);
                els = new Elements();
            }
        }
        elList.add(els);
        return elList;
    }
}
