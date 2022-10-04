package 博纳斯威.BN20220919发票信息抽取.发票;


import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
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
    private Pattern pNoPre = Pattern.compile("(N[oO]?|发票号码[:.]?)");
    private Pattern pNo = Pattern.compile("(\\d+)");
    private Pattern pDate = Pattern.compile("(\\d{4}[\\-年]\\d{1,2}[\\-月]\\d{1,2}日?)");
    private Pattern pBigAmt = Pattern.compile("(零|壹|贰|叁|肆|伍|陆|柒|捌|玖|拾)([零壹贰叁肆伍陆柒捌玖拾仟佰万分角元圆]+整?)");
    private Pattern pLowerAmt = Pattern.compile("[1-9](\\d*,)*(\\d)+(\\.\\d{2})");

    @Override
    public Map<String, Object> handleKeyWord(File html) {
        Map<String, Object> row = getRow(html);
        try {
            Document document = Jsoup.parse(html, "utf8");
            Elements pEls = document.select("p");
            //开票日期抽取
            for (Element p : pEls) {
                String content = CommonUtil.cleanBlank(p.text());
                if (!content.contains("开票日期") && !content.contains("时间")) continue;
                Matcher mDate = pDate.matcher(content);
                String date = "";
                if (mDate.find()) {
                    date = mDate.group();
                } else if (p.nextElementSibling() != null) {
                    mDate = pDate.matcher(p.nextElementSibling().text());
                    date = mDate.find() ? mDate.group() : "";
                }
                row.put("开票日期", date);
            }
            //发票号码抽取
            boolean isStart = false;
            for (Element p : pEls) {
                String content = p.text();
                Matcher m = pNoPre.matcher(content);
                if (keyInArrays(content, "开票日期", "校验码", "密码区") != null) break;
                if (!isStart && m.find()) isStart = true;
                if (!isStart) continue;
                Matcher mNo = pNo.matcher(content);
                if(mNo.find()){
                    String no = mNo.group().length() == 8 ? mNo.group() : "";
                    row.put("发票号码", no);
                    if(!StringUtil.isBlank(no)) break;
                }
            }


            Element tableEl = document.selectFirst("table");
            if (tableEl == null || "".equals(tableEl.text())) return row;
            Elements trEls = tableEl.select("tr");
            for (Element element : trEls) {
                String text = element.text();
                if (text == null || "".equals(text)) continue;
                if (text.contains("价税合计")) {
                    putAmount(element, row);
                }
                if (text.contains("纳税人识别号") || text.contains("开户行及账号")) {
                    putCounterparty(element, row);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return row;
    }


    //抽取交易方信息
    public void putCounterparty(Element tr, Map<String, Object> row) {
        Elements tdEls = tr.select("td");
        for (Element td : tdEls) {
            String content = CommonUtil.cleanBlank(td.text());
            String endKey = keyInArrays(content, "纳税人识别号", "别号");
            String startKey = keyInArrays(content, "名称", "称");
            if (startKey == null || endKey == null) continue;
            int start = content.indexOf(startKey);
            int end = content.indexOf(endKey);
            if (start < 0 || end < 0 || start > end) break;
            start = start + startKey.length();
            String company = content.substring(start, end);
            if (company.contains("博纳斯威")) break;
            row.put("交易对手名称", company);
        }
    }

    /**
     * 找出字符串在数组中包含的key
     *
     * @param content 目标字符串
     * @param array   数组
     * @return
     */
    public String keyInArrays(String content, String... array) {
        for (String key : array) {
            if (content.contains(key))
                return key;
        }
        return null;
    }


    //抽取金额信息
    public void putAmount(Element tr, Map<String, Object> row) {
        Elements tdEls = tr.select("td");
        for (int i = 0; i < tdEls.size(); i++) {
            Element td = tdEls.get(i);
            String tdText1 = td.text();
            if (tdText1.contains("价税合计")) {
                Element nextTd = td.nextElementSibling();
                nextTd = nextTd != null ? nextTd : td;
                String nextTdText = CommonUtil.cleanBlank(nextTd.text());
                Matcher mBigAmt = pBigAmt.matcher(nextTdText);
                Matcher mLowerAmt = pLowerAmt.matcher(nextTdText);
                if (mBigAmt.find()) row.put("大写合计金额", mBigAmt.group());
                if (mLowerAmt.find()) row.put("小写合计金额", mLowerAmt.group());
                break;
            }
        }
    }
}
