package 江西宁新.NX20220825抽取.销售订单;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CommonUtil;
import 江西宁新.NX20220825抽取.executor.AbstractHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler extends AbstractHandler {
    private Pattern pNo = Pattern.compile("N[O|o]:([A-Z]+\\d{7})");

    @Override
    public List<Map<String, Object>> handleKeyWord(File html) {
        List<Map<String,Object>> rows = new ArrayList<>();
        try {
            Document document = Jsoup.parse(html, "utf8");

            List<Elements> elements = tableSplit(document);
            for (Elements element : elements) {
                String no = "";
                Elements pEls = element.select("p");
                for (Element p:pEls){
                    String content = p.text().replaceAll("\\s+","");
                    Matcher m = pNo.matcher(content);
                    if(m.find()){
                        no = m.group(1);
                        break;
                    }
                }

                if(element.select("table").size()<=0)
                    continue;

                Element table = element.select("table").get(0);
                if(table==null || "".equals(table.text())){
                    rows.add(getRow(html));
                    return rows;
                }

                Elements trEls = table.select("tr");
                int indexCol1 = -1;
                int indexCol2 = -1;



                Element firstTr = trEls.get(0);
                Elements fTds = firstTr.select("td");
                int headSize = fTds.size();
                for (int i=0;i< headSize;i++){
                    Element td = fTds.get(i);
                    String content = td.text();
                    if(content.contains("规格型号")){
                        indexCol1 = i;
                    }else if(content.contains("数量")){
                        indexCol2 = i;
                    }
                }



                for (int i=1;i<trEls.size();i++){
                    Element tr = trEls.get(i);
                    System.out.println("tr\t"+tr.text());
                    if(tr==null || "".equals(tr.text()) || tr.text().contains("合计"))
                        continue;



                    Elements tds = tr.select("td");
                    Map<String,Object> row = getRow(html);
                    row.put("订单号",no);

                    if(tds.size()!=headSize){
                        return rows;
                    }


                    if(indexCol1!=-1){
                        row.put("规格型号",tds.get(indexCol1).text());
                    }
                    if(indexCol2!=-1){
                        row.put("数量",tds.get(indexCol2).text());
                    }

                    rows.add(row);
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }


    public List<Elements> tableSplit(Element document){
        Elements elements = document.select("p,table");
        List<Elements> elList = new ArrayList<>();
        Elements els = new Elements();
        for (Element element : elements) {
            if(CommonUtil.cleanBlank(element.text()).contains("销售订单")){
                els = new Elements();

                elList.add(els);
            }

            els.add(element);
        }
        return elList;
    }
}
