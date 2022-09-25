package 江西宁新.NX20220829采购抽取.合同;

import cn.hutool.core.io.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.List;

public class Test {
    private static String[] keyWord1 = {
            "交(提)货方式及费用承担",
            "交(提)货地点",
            "交(提)货时间",
            "交货地点",
            "交货地址",
            "交货方式",
            "交付方式",
            "交货时间",
            "供货价格",
            "送货运费",
            "以上价格",
            "运输方式",
            "运输要求",
            "运输费用负担",
            "运输费用由"};


    public static void main(String[] args) throws Exception{
        List<File> files = FileUtil.loopFiles("E:\\江西宁新\\采购合同解析后_删除后\\01江西宁新新材料股份有限公司",f->f.getName().endsWith(".html"));

        int count = 0;

        for (File file : files) {
            Document document = Jsoup.parse(file, "utf8");
            Elements pEls = document.select("p");
            String content = pEls.text();
            for (String s : keyWord1) {
                if(content.contains(s)){
                    count++;
                    break;
                }
            }
        }


        System.out.println(count);
    }
}
