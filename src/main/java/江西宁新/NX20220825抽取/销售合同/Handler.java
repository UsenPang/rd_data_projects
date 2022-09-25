package 江西宁新.NX20220825抽取.销售合同;

import cn.hutool.core.io.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CommonUtil;
import utils.RdPdfUtil;
import 江西宁新.NX20220825抽取.executor.MultiHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Handler implements MultiHandler {
    private Pattern pPage = Pattern.compile("(pdf|PDF)_(\\d+)");
    String[] headKey = {"文件路径信息","文件名称","合同编号","时间","规格"};
    Pattern p1 = Pattern.compile("(合同编?号|Invoice N[Oo]|PI N[oO]|N[Oo]|采购订单号)\\.?:?([a-zA-Z0-9\\-]+[0-9])");  //合同编号
    Pattern p2 = Pattern.compile("(签订时间|制表日期|签开日期|DATE|Date|签订日期|订单日期):?(\\d{4}[年\\-/\\.]\\d{1,2}[月\\-/\\.]\\d{1,2}日?|(\\d{2}\\.){2}\\d{2}|\\d{2}-[a-zA-Z]+-\\d{2})");    //日期


    @Override
    public List<Map<String, Object>> handleKeyWord(File html) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            Document document = Jsoup.parse(html, "utf8");


            //获取合同编号  与  合同签订日期
            String contractNum = "";
            String date = "";

            Elements pELs = document.select("p");
            boolean isFind1 = false;
            boolean isFind2 = false;
            for(Element p:pELs){
                String content = CommonUtil.cleanBlank(p.text());
                Matcher m1 = p1.matcher(content);
                Matcher m2 = p2.matcher(content);
                if(m1.find() && !isFind1){
                    contractNum = m1.group(2);
                    isFind1 = true;
                }


                if(m2.find() && !isFind2){
                    date = m2.group(2);
                    isFind2 = true;
                }

            }






            Element tableEl = document.selectFirst("table");

            if(tableEl==null || "".equals(tableEl.text())){
                if("".equals(date) && "".equals(contractNum))
                    return rows;

                Map<String,Object> row = getRow(html);
                row.put("合同编号",contractNum);
                row.put("时间",date);
                rows.add(row);
                return rows;
            }

            tableEl = RdPdfUtil.tableBreaking(tableEl);


            //从表头找到索引列
            Elements trEls = tableEl.select("tr");

            Element firstTr = trEls.get(0);
            Elements firstTds = firstTr.select("td");

            int indexCol = -1;
            for (int i = 0; i < firstTds.size(); i++) {
                String content = CommonUtil.cleanBlank( firstTds.get(i).text() );
                if(containSize(content)){
                    indexCol = i;
                    break;
                }
            }



            //遍历该列所有的 规格信息
            for (int i = 1; i < trEls.size(); i++) {
                Element tr = trEls.get(i);
                if(tr!=null && isEnd(CommonUtil.cleanBlank(tr.text())))
                    break;

                if(indexCol!=-1){
                    Element td = tr.select("td").get(indexCol);
                    String content = td.text();
                    if(content==null || "".equals(content))
                        break;

                    if(containSize(content))
                        continue;

                    Map<String,Object> row = getRow(html);
                    row.put("合同编号",contractNum);
                    row.put("时间",date);
                    row.put("规格",content);
                    rows.add(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rows;
    }


    public boolean isEnd(String content){
        if(CommonUtil.isEmpty(content))
            return false;
        Matcher m = Pattern.compile("(总计|合计|小计|预付|Total|不含税|大写|人民币|¥)").matcher(content);
        return m.find();
    }




    public boolean containSize(String content){
        return content.contains("规格") || content.contains("Size") || content.contains("型号") || content.contains("SPECIFICATION");
    }



    public Map<String,Object> getRow(File file){
        Map<String,Object> row = new HashMap<>();

        for (String key : headKey) {
            row.put(key,"");
        }


        String path = null;
        String fileName = null;
        Matcher mPage = pPage.matcher(file.getName());
        if(mPage.find()){
            path = file.getParentFile().getParent();
            Matcher m = Pattern.compile("\\.(pdf|PDF)_\\d+.*").matcher(file.getName());
            fileName = m.replaceAll("\\.$1");
        }else{
            path = file.getParent();
            fileName  = FileUtil.mainName(file);
        }

        row.put("文件路径信息",path);
        row.put("文件名称",fileName);
        return row;
    }
}
