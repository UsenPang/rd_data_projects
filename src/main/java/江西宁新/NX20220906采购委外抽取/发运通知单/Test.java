package 江西宁新.NX20220906采购委外抽取.发运通知单;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {
        File f = new File("E:\\江西宁新\\分类解析后文件_新版\\宁和达\\2021年\\10月\\记104\\发运通知单-宁和达-2021-10-记104\\发运通知单-宁和达-2021-10-记104.pdf_2.jpg.html");
        Document document = Jsoup.parse(f, "utf-8");
        Elements pEls = document.select("table ~p");
        Elements preEls = document.select("p");
        System.out.println(pEls+"\n============================");
        System.out.println(preEls+"\n============================");
        preEls.removeAll(pEls);
        System.out.println(preEls+"\n============================");
    }
}
