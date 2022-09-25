package 博纳斯威.BN20220920合同单号提取.合同单号抽取;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CommonUtil;
import 博纳斯威.BN20220920合同单号提取.executor.AbstractSimpleHandler;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler extends AbstractSimpleHandler {
    private Pattern pNo = Pattern.compile("合同单号[:\\.]?([A-Z\\d]+)[\\u4e00-\\u9fa5]?");


    @Override
    public Map<String, Object> handleKeyWord(File html) {
        try {
            Document document = Jsoup.parse(html, "utf8");


            Elements pEls = document.select("p");
            for (Element p:pEls){
                String content = CommonUtil.cleanBlank(p.text());

                if(!content.contains("合同单号")) continue;

                Matcher m = pNo.matcher(content);
                if(m.find()){
                    Map<String,Object> row = getRow(html);
                    row.put("合同单号",m.group(1));
                    return row;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
