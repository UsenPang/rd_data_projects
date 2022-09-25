package 博纳斯威.BN20220919发票信息抽取.发票;



import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CommonUtil;
import 博纳斯威.BN20220919发票信息抽取.executor.AbstractSimpleHandler;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler extends AbstractSimpleHandler {
    private Pattern pNo = Pattern.compile("(No|发票号码[:.]?)(\\d+)");
    private Pattern pDate = Pattern.compile("\\d{4}年\\d{1,2}月\\d{1,2}日");
    private Pattern pBigAmt = Pattern.compile("(零|壹|贰|叁|肆|伍|陆|柒|捌|玖|拾)([零壹贰叁肆伍陆柒捌玖拾仟佰万分角元圆]+整?)");
    private Pattern pLowerAmt = Pattern.compile("[1-9](\\d*,)*(\\d)+(\\.\\d{2})");

    @Override
    public Map<String, Object> handleKeyWord(File html) {
        Map<String, Object> row = getRow(html);
        try {
            Document document = Jsoup.parse(html, "utf8");


            Elements pEls = document.select("p");
            for (Element p : pEls) {
                String content = p.text().replaceAll("\\s+", "");
                Matcher m = pNo.matcher(content);
                if (m.find()) {
                    String no = m.group(2).length() == 8 ? m.group(2) : "";
                    row.put("发票号码", no);
                } else if (content.contains("开票日期")) {
                    Matcher mDate = pDate.matcher(content);
                    String date = "";

                    if(mDate.find()){
                        date = mDate.group();
                    }else{
                        mDate = pDate.matcher(p.nextElementSibling().text());
                        if (mDate.find()) date = mDate.group();
                    }
                    row.put("开票日期", date);
                }
            }


            Element tableEl = document.selectFirst("table");
            if (tableEl == null || "".equals(tableEl.text())) {
                return row;
            }

            Elements trEls = tableEl.select("tr");
            for (Element element : trEls) {
                String text = element.text();
                if (text == null || "".equals(text) || !text.contains("价税合计"))
                    continue;

                Elements tdEls = element.select("td");
                for (int i = 0; i < tdEls.size(); i++) {
                    Element td = tdEls.get(i);
                    String tdText1 = td.text();
                    if (tdText1.contains("价税合计")) {
                        Element nextTd = td.nextElementSibling();
                        nextTd = nextTd != null ? nextTd : td;
                        String nextTdText = CommonUtil.cleanBlank(nextTd.text());
                        Matcher mBigAmt = pBigAmt.matcher(nextTdText);
                        Matcher mLowerAmt = pLowerAmt.matcher(nextTdText);
                        if (mBigAmt.find())
                            row.put("大写合计金额", mBigAmt.group());
                        if (mLowerAmt.find())
                            row.put("小写合计金额", mLowerAmt.group());
                        break;
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return row;
    }
}
