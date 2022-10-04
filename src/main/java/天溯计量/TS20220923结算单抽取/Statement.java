package 天溯计量.TS20220923结算单抽取;

import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CollectUtil;
import utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
public class Statement {
    private List<File> htmls;
    private Elements ps;
    private Elements startTables;
    private Elements numTables;
    private Elements noNumTables;
    private Elements endTables;

    private static String[] head1 = {"报价单编号", "查询码"};
    private static String[] head2 = {"号", "仪器名称", "规格", "单价", "数量"};

    public Statement(List<File> htmls) {
        if (htmls == null || htmls.size() <= 0)
            throw new IllegalArgumentException(String.format("参数异常:{}", htmls));
        this.htmls = htmls;
    }


    public void parse() {
        init();
        List<Element> tables = new ArrayList<>();
        for (File html : htmls) {
            Document doc = getDocument(html);
            String fileName = html.getName();
            Elements ps1 = doc.select("p");
            ps1.forEach(p -> p.attr("fileName", fileName));
            ps.addAll(ps1);
            Elements tables1 = doc.select("table");
            tables1.stream().map(table -> table.select("tr")).forEach(trs -> trs.forEach(tr -> tr.attr("fileName", fileName)));
            tables.addAll(tables1);
        }

        List<Element> tmpColl;

        //找出所有可能包含手写日期的表格
        tmpColl = tables.stream().filter(table -> StringUtil.containsAll(table.html(), "委托单位盖章", "签字", "日期")).collect(Collectors.toList());
        endTables.addAll(tmpColl);
        tables.removeAll(tmpColl);

        //找出所有包含客户名称的表格
        tmpColl = tables.stream().filter(table -> StringUtil.containsAll(table.html(), "电话", "传真", "邮箱")).collect(Collectors.toList());
        startTables.addAll(tmpColl);
        //从tables集合中移除
        tables.removeAll(tmpColl);


        //将粘连的表格进行切割
        tables = tableSplit(tables);
        //将截断的表格拼接
        tables = tableJoin(tables);

        //筛选出有凭证号的表格
        tmpColl = tables.stream().filter(table -> isStart1(table) && isStart2(table)).collect(Collectors.toList());
        numTables.addAll(tmpColl);
        tables.removeAll(tmpColl);

        //无凭证号的表格
        tmpColl = tables.stream().filter(this::isStart2).collect(Collectors.toList());
        noNumTables.addAll(tmpColl);
    }

    public List<Element> tableJoin(List<Element> tables) {
        Pattern p = Pattern.compile("^\\d+$");
        //筛选出被截断的后一段表格
        List<Element> rearTables = tables.stream().filter(table -> {
            Elements trs = table.select("tr");
            if (CollectUtil.isEmpty(trs)) return false;
            for (int i = 0; i < 3 && i < trs.size(); i++) {
                Element tr = trs.get(i);
                if (StringUtil.containsAny(tr.html(), "报价单编号", "查询码", "号", "仪器名称", "规格", "单价", "数量")) return false;
                Element td = tr.selectFirst("td");
                if (!Objects.isNull(td) && p.matcher(td.text()).find()) return true;
            }
            return false;
        }).collect(Collectors.toList());
        tables.removeAll(rearTables);

        //筛选出被截断的前一段表格
        List<Element> frontTables = tables.stream().filter(table -> {
            Elements trs = table.select("tr");
            if (CollectUtil.isEmpty(trs)) return false;
            int len = trs.size();
            for (int i = len - 1; i > len - 3 && i >= 0; i--) {
                Element tr = trs.get(i);
                if (StringUtil.containsAny(tr.text(), "证书编号", "备注", "小计", "总计", "费用")) return false;
                Element td = tr.selectFirst("td");
                if (Objects.nonNull(td) && p.matcher(td.text()).find()) return true;
            }
            return false;
        }).collect(Collectors.toList());
        tables.removeAll(frontTables);

        //将这两种表格拼接到一块
        for (int i = 0; i < frontTables.size(); ) {
            Element frontTable = frontTables.get(i);
            boolean isFind = false;
            Element removeTable = null;
            for (Element rearTable : rearTables) {
                if (isEnd(frontTable)) break;
                if (isFind = isOneTable(frontTable, rearTable)) {
                    Element tbody = frontTable.selectFirst("tbody");
                    tbody.append(rearTable.select("tr").outerHtml());
                    removeTable = rearTable;
                    break;
                }
            }
            if (isFind) {
                rearTables.remove(removeTable);
            } else {
                i = i + 1;
            }
        }


        tables.addAll(frontTables);
        tables.addAll(rearTables);
        return tables;
    }


    public boolean isStart1(Element table) {
        return StringUtil.containsAll(table.html(), "报价单", "查询码");
    }

    public boolean isStart2(Element table) {
        return StringUtil.containsAll(table.html(), "号", "仪器名称", "规格", "单价", "数量");
    }

    public boolean isEnd(Element table) {
        return StringUtil.containsAny(table.html(),  "备注", "费用小计", "总计", "费用","优惠价格");
    }

    public boolean isOneTable(Element front, Element rear) {
        Pattern p = Pattern.compile("^\\d+$");
        Elements trs1 = front.select("tr");
        Elements trs2 = rear.select("tr");
        Element lastTr = trs1.last();
        Element firstTr = trs2.first();
        if (lastTr.childNodeSize() != firstTr.childNodeSize()) return false;
        int div = 1;
        while (div < 6) {
            if (lastTr == null || firstTr == null) return false;
            Matcher m1 = p.matcher(lastTr.selectFirst("td").text());
            Matcher m2 = p.matcher(firstTr.selectFirst("td").text());
            if (!m1.find()) {
                lastTr = lastTr.previousElementSibling();
                div++;
                continue;
            }
            if (!m2.find()) {
                firstTr = firstTr.nextElementSibling();
                div++;
                continue;
            }
            int num1 = Integer.parseInt(m1.group());
            int num2 = Integer.parseInt(m2.group());
            return num2 > num1 && (num2 - num1) == div;
        }
        return false;
    }


    public List<Element> tableSplit(List<Element> tables) {
        List<Element> newTables = new ArrayList<>();
        List<Element> finalNewTables = newTables;
        tables.stream().map(table -> table.select("tr"))
                .forEach(trs -> {
                    List<Integer> indexs = trs.stream().filter(tr -> isStart2(tr))
                            .map(tr -> {
                                Element preTr = tr.previousElementSibling();
                                if (preTr != null && isStart1(preTr)) return preTr;
                                return tr;
                            }).map(tr -> trs.indexOf(tr)).collect(Collectors.toList());
                    int len = indexs.size();
                    //表格无需拆分
                    if (indexs.isEmpty()) finalNewTables.add(createTable(trs));
                    //拆分表格
                    for (int i = 0; i < len; i++) {
                        List<Element> newTrs;
                        //对于前面无表头的情况，截取前一段
                        if (i == 0 && indexs.get(i) != 0) {
                            newTrs = trs.subList(0, indexs.get(i));
                            finalNewTables.add(createTable(newTrs));
                        }
                        newTrs = (i + 1) < len ? trs.subList(indexs.get(i), indexs.get(i+1)) : trs.subList(indexs.get(i), trs.size());
                        finalNewTables.add(createTable(newTrs));
                    }
                });
        //过滤掉tr个数为1的表格
        newTables = newTables.stream().filter(table -> table.select("tr").size() > 1).collect(Collectors.toList());
        return newTables;
    }

    public Element createTable(List<Element> trs) {
        Element table = new Element("table");
        table.attr("border", "1");
        Element tbody = new Element("tbody");
        trs.forEach(tr -> tbody.appendChild(tr));
        table.appendChild(tbody);
        return table;
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


    private void init() {
        this.ps = new Elements();
        this.startTables = new Elements();
        this.numTables = new Elements();
        this.noNumTables = new Elements();
        this.endTables = new Elements();
    }
}
