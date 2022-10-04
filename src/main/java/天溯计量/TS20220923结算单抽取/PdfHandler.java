package 天溯计量.TS20220923结算单抽取;


import demons.抽取.Context;
import demons.抽取.MultiHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CollectUtil;
import utils.CommonUtil;
import utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfHandler implements MultiHandler {
    Pattern datePattern = Pattern.compile("\\d{4}\\.\\d{1,2}\\.\\d{1,2}");
    Pattern numPattern = Pattern.compile("[a-zA-Z0-9]{15,20}");
    Pattern amountPtn = Pattern.compile("(\\d+,)*(\\d)+(\\.\\d{2})?");
    Pattern pagePtn = Pattern.compile("\\.(PDF|pdf)_(\\d+)");

    @Override
    public void handle(List<File> files, Context context) {
        //这些文件中如果不报含结算单则跳过
        if (!have(files)) return;

        Statement stat = new Statement(files);
        stat.parse();

        //********************抽取单号与客户名称******************************
        Map<String, String> numMap = new HashMap<>();
        Map<String, String> clientMap = new HashMap<>();

        //从段落中找结算单号和客户名称
        Elements pEls = stat.getPs();
        for (Element p : pEls) {
            String content = CommonUtil.cleanBlank(p.text());
            if (StringUtil.containsAny(content, "单号", "SN")) {
                Matcher numMatcher = numPattern.matcher(content);
                if (numMatcher.find()) {
                    String fileName = p.attr("fileName");
                    numMap.put(fileName, numMatcher.group());
                }
            }

            String keyWord = StringUtil.anyWordOf(content, "委托单位", "致", "委托方", "付款方");
            if (keyWord != null) {
                int st = content.indexOf(keyWord) + keyWord.length() + 1;
                int end;
                end = (end = content.indexOf("电")) > 0 ? end : content.length();
                if (st < end) {
                    String fileName = p.attr("fileName");
                    String clientName = content.substring(st, end);
                    if (clientName.length() > 6)
                        clientMap.put(fileName, clientName);
                }
            }
        }

        //从表格中找结算单号与客户名称
        Elements stTable = stat.getStartTables();
        for (Element table : stTable) {
            Elements trs = table.select("tr");
            for (Element tr : trs) {
                String content = CommonUtil.cleanBlank(tr.selectFirst("td").text());
                if (StringUtil.containsAny(content, "单号", "SN")) {
                    Matcher numMatcher = numPattern.matcher(content);
                    if (numMatcher.find()) {
                        String fileName = tr.attr("fileName");
                        numMap.put(fileName, numMatcher.group());
                    }
                }

                String keyWord = StringUtil.anyWordOf(content, "委托单位", "致", "委托方", "付款方");
                if (keyWord != null) {
                    int st = content.indexOf(keyWord) + keyWord.length() + 1;
                    String fileName = tr.attr("fileName");
                    if (st < content.length()) {
                        String clientName = content.substring(st);
                        if (clientName.length() > 6)
                            clientMap.put(fileName, clientName);
                    }
                }
            }
        }


        //**************************开始抽取结算单仪器信息***************************************


        //**************************抽取所有表格***************************
        boolean isWrite = false;
        Elements tables = stat.getNumTables();
        tables.addAll(stat.getNoNumTables());
        for (Element table : tables) {
            String num = "", client = "", total1 = "", total2 = "", lowerTotal = "";
            Elements trs = table.select("tr");
            Element firstTr = trs.first();
            Element lastTr = trs.last();

            //抽取所有金额信息
            int count = 0;
            while (count <= 4 && lastTr != null) {
                String text = lastTr.text();
                if (text.contains("费用小计")) {
                    Matcher m = amountPtn.matcher(text);
                    if (m.find()) lowerTotal = m.group();
                } else if (StringUtil.containsAny(text, "总计", "合计")) {
                    if (text.contains("不含税")) {
                        Matcher m = amountPtn.matcher(text);
                        if (m.find()) total2 = m.group();
                    } else if (text.contains("含税")) {
                        Matcher m = amountPtn.matcher(text);
                        if (m.find()) total1 = m.group();
                    }
                }
                lastTr = lastTr.previousElementSibling();
            }

            //抽取结算单号和金额
            if (StringUtil.containsAny(firstTr.text(), "报价单", "查询码")) {
                Matcher numMatcher = numPattern.matcher(firstTr.text());
                if (!numMatcher.find()) continue;
                num = numMatcher.group();
                firstTr = firstTr.nextElementSibling();
            }

            //结算单号
            if (StringUtil.isEmpty(num))
                num = getOne(numMap, firstTr.attr("fileName"));
            //客户名称
            client = getOne(clientMap, firstTr.attr("fileName"));

            //抽取所有仪器信息行
            Element tr = firstTr;
            if (!StringUtil.containsAny(tr.text(), "仪器名称", "规格", "单价", "数量")) break;
            int index1 = -1, index2 = -1, index3 = -1, index4 = -1;
            Elements tds = tr.select("td");
            int colCount = tds.size();
            for (int i = 0; i < colCount; i++) {
                Element td = tds.get(i);
                String text = td.text();
                if (text.contains("仪器名称")) index1 = i;
                else if (text.contains("规格")) index2 = i;
                else if (text.contains("单价")) index3 = i;
                else if (text.contains("数量")) index4 = i;
            }
            tr = tr.nextElementSibling();

            //抽取所有仪器信息
            while (tr != null) {
                tds = tr.select("td");
                if (StringUtil.containsAny(tr.text(), "费用小计", "总计", "合计", "含税")
                        || tds.size() != colCount) break;
                String fileName = tr.attr("fileName");
                Map<String, Object> row = new HashMap<>();
                if (index1 != -1) row.put("仪器名称", tds.get(index1).text());
                if (index2 != -1) row.put("规格", tds.get(index2).text());
                if (index3 != -1) row.put("单价", tds.get(index3).text());
                if (index4 != -1) row.put("数量", tds.get(index4).text());
                if (row.size() > 0) {
                    //页码
                    Matcher pageMatcher = pagePtn.matcher(fileName);
                    if (pageMatcher.find())
                        row.put("页码", pageMatcher.group(2));
                    //文件名称
                    fileName = fileName.replaceAll("(.*)(PDF|pdf)_\\d+.*", "$1$2");
                    row.put("PDF文件名称", fileName);
                    row.put("客户名称", client);
                    row.put("结算单号", num);
                    row.put("金额小计（含税）", lowerTotal);
                    row.put("金额合计（含税）", total1);
                    row.put("金额合计（不含税）", total2);
                    //写出
                    context.write(row);
                    isWrite = true;
                }
                tr = tr.nextElementSibling();
            }

            if (!isWrite) {
                Map<String, Object> row = new HashMap<>();
                String fileName = table.selectFirst("tr").attr("fileName");
                //页码
                String page = "";
                Matcher pageMatcher = pagePtn.matcher(fileName);
                if (pageMatcher.find())
                    page = pageMatcher.group(2);
                //文件名称
                fileName = fileName.replaceAll("(.*)(PDF|pdf)_\\d+.*", "$1$2");
                row.put("PDF文件名称", fileName);
                row.put("客户名称", client);
                row.put("结算单号", num);
                row.put("金额小计（含税）", lowerTotal);
                row.put("金额合计（含税）", total1);
                row.put("金额合计（不含税）", total2);
                row.put("页码", page);
                //写出
                context.write(row);
            }
        }
    }

    public String getOne(Map<String, String> map, String key) {
        String value;
        if (map.size() == 1) {
            Iterator<String> iterator = map.values().iterator();
            value = iterator.next();
        } else {
            value = map.get(key);
        }
        return value != null ? value : "";
    }


    public boolean have(List<File> htmls) {
        if (CollectUtil.isEmpty(htmls)) return false;
        for (File html : htmls) {
            Document doc = getDocument(html);
            Elements pEls = doc.select("p");
            if (pEls.text().contains("结算通知单")) return true;
        }
        return false;
    }


    private Document getDocument(File htmlFile) {
        Document document = null;
        try {
            document = Jsoup.parse(htmlFile, "utf8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }
}
