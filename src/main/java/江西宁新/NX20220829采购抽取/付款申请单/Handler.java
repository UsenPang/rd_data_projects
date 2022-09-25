package 江西宁新.NX20220829采购抽取.付款申请单;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CommonUtil;
import 江西宁新.NX20220829采购抽取.executor.AbstractSimpleHandler;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler extends AbstractSimpleHandler {

    Pattern pContract = Pattern.compile("[A-Z0-9\\-\\./]{6,}");
    Pattern pDate = Pattern.compile("(\\d{4}[年\\-/\\.]\\d{1,2}[月\\-/\\.]\\d{1,2}日?|(\\d{2}\\.){2}\\d{2}|\\d{2}-[a-zA-Z]+-\\d{2}|[a-zA-Z]+\\.\\d{1,2},\\d{4})");
    Pattern pAmount1 = Pattern.compile("((零|壹|贰|叁|肆|伍|陆|柒|捌|玖|拾)([零壹贰叁肆伍陆柒捌玖拾仟佰万]*[分角元圆]整))");
    Pattern pAmount2 = Pattern.compile("(\\d+,)*(\\d)+(\\.\\d{2})?");


    @Override
    public Map<String, Object> handleKeyWord(File html) {
        Map<String,Object> row = getRow(html);
        try {
            Document document = Jsoup.parse(html, "utf8");
            Elements trEls = document.select("table tr");
            for (Element tr : trEls) {
                String trStr = CommonUtil.cleanBlank(tr.text());


                //抽取付款日期   付款金额
                String dateWord = getDateWord(trStr);
                String amountWord = getAmountWord(trStr);
                if(dateWord!=null){
                    Elements tdEls = tr.select("td");
                    int start = valueOfTd(dateWord,tr);
                    if(start!=-1){
                        for (int i = start; i < tdEls.size(); i++) {
                            Element td = tdEls.get(i);
                            String tdStr = CommonUtil.cleanBlank(td.text());
                            Matcher mDate = pDate.matcher(tdStr);
                            if(mDate.find()){
                                row.put("付款日期",mDate.group());
                                break;
                            }
                        }
                    }
                }else if(amountWord!=null){
                    Elements tdEls = tr.select("td");
                    int start = valueOfTd(amountWord,tr);
                    if(start!=-1){
                        for (int i = start; i < tdEls.size(); i++) {
                            Element td = tdEls.get(i);
                            String tdStr = CommonUtil.cleanBlank(td.text());

                            if(tdStr.contains("支付日期") || tdStr.contains("总金额"))
                                break;

                            Matcher mAmount1 = pAmount1.matcher(tdStr);
                            Matcher mAmount2 = pAmount2.matcher(tdStr);
                            if(mAmount1.find()){
                                row.put("付款金额大写",mAmount1.group());
                            }

                            if(mAmount2.find())
                                row.put("付款金额",mAmount2.group());

                        }
                    }
                }


                //抽取合同号
                if(trStr.contains("付款依据") && trStr.contains("合同")){
                    if("E:\\江西宁新\\单据分类_新版解析\\宁和达\\2021年\\10月\\155\\付款申请单-宁和达-2021-10-记155\\付款申请单-宁和达-2021-10-记155.pdf_1.jpg.html".equals(html.getPath())){
                        System.out.println(tr);
                    }
                    Elements tdEls = tr.select("td");
                    int index = valueOfTd("付款依据", tr);
                    if(index!=-1){
                        Element td = tdEls.get(index);
                        Matcher mContract = pContract.matcher(td.text());
                        if(td.text().contains("合同") && mContract.find())
                            row.put("付款合同号",mContract.group());
                    }
                }else if(!"".equals(trStr) && trStr.contains("合同编号")){
                    Elements tdEls = tr.select("td");
                    int index = valueOfTd("合同编号", tr);
                    if(index!=-1){
                        Element td = tdEls.get(index);
                        Matcher mContract = pContract.matcher(td.text());
                        if(mContract.find())
                            row.put("付款合同号",td.text());
                    }
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return row;
    }

    public String getDateWord(String target){
        if(target.contains("申请日期"))
            return "申请日期";
        if(target.contains("申请日期"))
            return "申请日期";
        return null;
    }


    public String getAmountWord(String target){
        if(target.contains("本次付款金额"))
            return "本次付款金额";
        if(target.contains("申请金额"))
            return "申请金额";
        return null;
    }

    public int valueOfTd(String keyWord,Element tr){
        Elements tdEls = tr.select("td");
        for (int i = 0; i < tdEls.size(); i++) {
            Element td = tdEls.get(i);
            if(td.text().contains(keyWord) && i+1 < tdEls.size())
                return i+1;
        }
        return -1;
    }

}
