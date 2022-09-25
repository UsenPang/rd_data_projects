package 江西宁新.NX20220829采购抽取.发票;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import 江西宁新.NX20220829采购抽取.executor.AbstractSimpleHandler;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler extends AbstractSimpleHandler {
    private Pattern pNo = Pattern.compile("No(\\d{8})");
    private Pattern pAmt = Pattern.compile("((零|壹|贰|叁|肆|伍|陆|柒|捌|玖|拾)([零壹贰叁肆伍陆柒捌玖拾仟佰万]*[分角元圆]整))");
    String[] keyWords = {"规格型号","数量","单位","金额","税额"};

    @Override
    public Map<String, Object> handleKeyWord(File html) {
        Map<String,Object> row = getRow(html);
        try {
            Document document = Jsoup.parse(html, "utf8");


            Elements pEls = document.select("p");
            for (Element p:pEls){
                String content = p.text().replaceAll("\\s+","");
                Matcher m = pNo.matcher(content);
                if(m.find()){
                    row.put("发票号码",m.group(1));
                    break;
                }
            }


            Element tableEl = document.selectFirst("table");
            if(tableEl==null || "".equals(tableEl.text())){
                return row;
            }

            Elements trEls = tableEl.select("tr");
            for (Element element: trEls){
                if(element.text()!=null && !"".equals(element.text())){
                    Elements tdEls = element.select("td");
                    for(int i = 0; i < tdEls.size(); i++){
                        String contentOld = tdEls.get(i).text();
                        String content = contentOld.replaceAll("\\s+","");

                        boolean find = false;
                        for (String keyWord : keyWords) {
                            if(content.contains(keyWord)){
                                row.put(keyWord,contentOld);
                                find = true;
                            }else{
                                //大写金额
                                Matcher m = pAmt.matcher(content);
                                if(m.find()){
                                    row.put("大写金额",m.group());
                                }
                            }

                            if (find)
                                break;
                        }

                    }
                }
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
        return row;
    }
}
