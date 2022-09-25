package 江西宁新.NX20220825抽取.发运通知单;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
//    private Pattern pNo = Pattern.compile("No:([A-Z]+\\d{7})");
    private Pattern pNo = Pattern.compile("(\\d{7})");
    private Pattern pDate = Pattern.compile("(\\d{4}[\\-\\.、年/])?(\\d{1,2}[\\-\\.、月/]\\d{1,2}日?)");

    @Override
    public List<Map<String, Object>> handleKeyWord(File html) {
        List<Map<String,Object>> rows = new ArrayList<>();
        try {
            Document document = Jsoup.parse(html, "utf8");


            //获取编号
            String no = "";
            Elements pEls = getTablePreP(document);
            for (Element p:pEls){
                String content = p.text().replaceAll("\\s+","");
                Matcher m = pNo.matcher(content);
                if(m.find()){
                    no = m.group(1);
                    break;
                }
            }


            //获取签收日期
            String signDate = "";
            Elements pEls2 = document.select("table ~p");
            for (Element p:pEls2){
                String content = p.text().replaceAll("\\s+","");
                Matcher m = pDate.matcher(content);
                if(!(content.contains("电话") || content.contains("联系人")) && m.find()){
                    signDate = m.group();
                    break;
                }
            }




            //表格无数据直接返回
            Element table = document.selectFirst("table");
            if(table==null || "".equals(table.text())){
                rows.add(getRow(html));
                return rows;
            }

            Elements trEls = table.select("tr");
            int indexCol = -1;


            //找到关键字在表头的位置
            Element firstTr = trEls.get(0);
            Elements fTds = firstTr.select("td");
            for (int i=0;i< fTds.size();i++){
                Element td = fTds.get(i);
                String content = td.text();
                if(content.contains("发运日期")){
                    indexCol = i;
                }
            }


            if(indexCol!=-1){
                //拿到表格列中的内容
                for (int i=1;i<trEls.size();i++){
                    Element tr = trEls.get(i);
                    if(tr==null || "".equals(tr.text()))
                        break;

                    Elements tds = tr.select("td");

                    if(tds.size()<=indexCol)
                        break;

                    String content = tds.get(indexCol).text();

                    if(content==null || "".equals(content))
                        break;

                    Map<String,Object> row = getRow(html);
                    row.put("订单号",no);
                    row.put("签收日期",signDate);
                    row.put("发运日期",content);
                    rows.add(row);

                }
            }else{
                Map<String,Object> row = getRow(html);
                row.put("订单号",no);
                row.put("签收日期",signDate);
                rows.add(row);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    //获取表格之前的p标签
    public Elements getTablePreP(Element document){
        Elements els = document.select("p");
        els.removeAll(document.select("table~p"));
        return els;
    }
}
