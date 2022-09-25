package 诚驰驾培.学员信息抽取.问卷信息抽取;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import 诚驰驾培.学员信息抽取.executor2.AbstractSimpleHandler;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Handler extends AbstractSimpleHandler {
    String[] keyWords = {"姓名", "身份证号码", "联系电话"};

    @Override
    public Map<String, Object> handleKeyWord(String key, List<File> htmls) {
        Map<String, Object> row = getRow(htmls.get(0));
        for (File html : htmls) {

            Document document = null;
            try {
                document = Jsoup.parse(html, "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (document == null)
                return row;

            Elements pEls = document.select("p");
            extractBaseMsg(pEls,row);
            extractMoney(pEls,row);
        }

        return row;
    }


    public void extractBaseMsg(Elements els, Map<String, Object> row){
        boolean isStart = false;
        for (int i = 0; i < els.size(); i++) {
            String text = els.get(i).text();
            if(text.contains("请问您在驰诚驾培学习缴纳培训费金额是多少"))
                break;
            if(text.contains("请提供您的基本信息"))
                isStart = true;

            if(isStart){
                for (String keyWord : keyWords) {
                    int index = text.indexOf(keyWord);
                    if(index==-1) continue;

                    index += keyWord.length();
                    String subStr = text.substring(index + 1);
                    row.put(keyWord, subStr);
                }
            }
        }
    }


    public void extractMoney(Elements els, Map<String, Object> row) {
        for (int i = 0; i < els.size(); i++) {
            Element p = els.get(i);

            //找到抽取的位置
            if (p.text().contains("请问您在驰诚驾培学习缴纳培训费金额是多少")) {
                String nText = els.get(++i).text();

                //起始结束关键字
                String keyWord = "回复";
                String endWord = "3、";
                StringBuffer sb = new StringBuffer();

                //获取起始关键字后一行的数据
                int index = nText.indexOf(keyWord);
                sb.append(nText.substring(index + keyWord.length() + 1));

                //获取起始关键字之后到结束关键字之间的内容
                for (int j = i + 1; j < els.size(); j++) {
                    String content = els.get(j).text();
                    if (content.contains(endWord)) break;
                    sb.append(content);
                }
                row.put("学费", sb.toString());
                break;
            }
        }

    }


}
