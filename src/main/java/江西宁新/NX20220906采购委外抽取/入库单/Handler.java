package 江西宁新.NX20220906采购委外抽取.入库单;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CommonUtil;
import 江西宁新.NX20220906采购委外抽取.executor.AbstractHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler extends AbstractHandler {
    private Pattern pDate = Pattern.compile("日期:((\\d{4})年(\\d{1,2})月(\\d{1,2})日)");
    private Pattern pNo = Pattern.compile("N[Oo]:([A-Zo]+\\d{6,7})");
    @Override
    public List<Map<String, Object>> handleKeyWord(File html) {
        List<Map<String,Object>> rows = new ArrayList<>();
        try {
            Document document = Jsoup.parse(html, "utf8");

            Elements tables = document.select("table");

            List<Elements> elsList = tableSplit(document);
            for (Elements els:elsList) {
                String no = "";
                String date = "";
                Elements pEls = els.select("p");
                for (Element p:pEls){
                    String content = CommonUtil.cleanBlank(p.text());
                    Matcher mNo = pNo.matcher(content);
                    Matcher mDate = pDate.matcher(content);
                    if(mNo.find()){
                        no = mNo.group(1);
                    }else if(mDate.find()){
                        date = mDate.group(1);
                    }
                }

                Element table = els.select("table").get(0);
                if(table==null || "".equals(table.text())){
                    Map<String,Object> row = getRow(html);
                    row.put("采购入库单号",no);
                    row.put("入库日期",date);
                    row.put("表格数统计",tables.size());
                    rows.add(row);
                    continue;
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
                    if(content.contains("物料名称")){
                        indexCol1 = i;
                    }else if(content.contains("总价")){
                        indexCol2 = i;
                    }
                }


                for (int i=1;i<trEls.size();i++){
                    Element tr = trEls.get(i);
                    if(tr==null || "".equals(tr.text()))
                        continue;



                    Elements tds = tr.select("td");
                    Map<String,Object> row = getRow(html);
                    row.put("采购入库单号",no);
                    row.put("入库日期",date);
                    row.put("表格数统计",tables.size());
                    if(tds.size()!=headSize){
                        break;
                    }


                    boolean isChange = false;
                    if(indexCol1!=-1){
                        String text = tds.get(indexCol1).text();
                        if(!CommonUtil.isEmpty(text)){
                            row.put("入库内容",text);
                            isChange = true;
                        }
                    }
                    if(indexCol2!=-1){
                        String text = tds.get(indexCol2).text();
                        if (!CommonUtil.isEmpty(text)) {
                            row.put("入库金额",text);
                            isChange = true;
                        }
                    }
                    if(isChange)
                        rows.add(row);
                }

                if(rows.size()==0){
                    Map<String,Object> row = getRow(html);
                    row.put("采购入库单号",no);
                    row.put("入库日期",date);
                    row.put("表格数统计",tables.size());
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
            els.add(element);
            if("table".equals(element.tagName())){
                elList.add(els);
                els = new Elements();
            }
        }
        return elList;
    }
}
