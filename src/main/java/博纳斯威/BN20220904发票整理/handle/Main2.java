package 博纳斯威.BN20220904发票整理.handle;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import utils.RdFileUtil;
import utils.RdPdfUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
public class Main2 {
    private static String[] head = {"索引号","日期","凭证号","发票命中统计"};
    private static String[] keyWords1 = {"增值税专用发票","增值税普通发票","增值税电子普通发票","电子普通发票", "发票"};
    private static String[] keyWords2 = {"纳税人识别号",
            "价税合计",
            "开票人",
            "开票代码",
            "发票号码",
            "NO",
            "校验码"};

    private String source;
    private String dest;
    private String excelSource;
    private List<Map<String,Object>> outRows;

    public static void main(String[] args) {

        String[] years = {"2019年","2020年","2021年","2022年"};
        for (String year : years) {
            String excelSource = "D:\\工作空间\\博纳斯威\\博纳斯威发票分类整理\\控制表\\博纳斯威"+year+"抽凭控制表";
            String src = "D:\\工作空间\\博纳斯威\\博纳斯威发票分类整理\\博纳斯威凭证拆分解析\\"+year;
            String dest = "D:\\工作空间\\博纳斯威\\博纳斯威发票分类整理\\博纳斯威发票整理结果";
            String excelOut = "D:\\工作空间\\博纳斯威\\博纳斯威发票分类整理\\统计表\\博纳斯威发票整理命中统计2.xlsx";
            String[] tables = new File(excelSource).list();
            String[] months = {"11月","12月","1月","2月","3月","4月","5月","6月","7月","8月","9月","10月"};

            List<Map<String,Object>> rows = new ArrayList<>();
            for (String table : tables) {
                int index = containsOf(months, table);
                String month = months[index];
                String source = src + File.separator + month;
                String excelPath = excelSource + File.separator + table;


                Main2 runner = new Main2();
                runner.setSource(source);
                runner.setDest(dest);
                runner.setExcelSource(excelPath);
                runner.setOutRows(rows);
                runner.run();
            }

            ExcelWriter writer = ExcelUtil.getWriter(excelOut, year);
            writer.write(rows);
            writer.close();
        }
    }



    public void run(){
        ExcelReader reader = ExcelUtil.getReader(excelSource);
        List<Map<String, Object>> rows = reader.read(1, 2, Integer.MAX_VALUE);
        reader.close();
        setHead(rows,head);



        Map<String,List<File>> fileMap = RdFileUtil.lsAllFilesByType(new File(source),"html");
        Collection<List<File>> values = fileMap.values();

        //过滤文件,取出发票
        List<List<File>> llf  = values.parallelStream()
                .map(list->list.stream().filter(this::isInvoice).sorted(this::compare).collect(Collectors.toList()))
                .filter(list->!list.isEmpty())
                .collect(Collectors.toList());


        //命中客户统计
        Multimap<String,File> mergeFileMap = ArrayListMultimap.create();
        for (List<File> files : llf) {
            for (File file : files) {

                //获取相对存储路径，将待存储文件存入列表
                String relativePath = getRelativePath(file);
                mergeFileMap.put(relativePath,file);


                //开始excel表格统计操作
                String fileName = file.getName();
                String voucher = fileName.substring(0,fileName.indexOf('.'));

                //找到表格目标行
                Map<String,Object> targetRow = null;
                for (Map<String, Object> row : rows) {
                    if(voucher.equals(row.get("索引号"))){
                        targetRow = row;
                        break;
                    }
                }

                //填写表格数据
                int page = getPage(file);
                addDistinct(targetRow,"发票命中统计","P"+page);
            }
        }



        //合并文件
        merge(mergeFileMap,dest);
        outRows.addAll(rows);
    }



    public  int compare(File f1,File f2){
        Pattern p = Pattern.compile("\\.pdf_(\\d+)");
        Matcher m1 = p.matcher(f1.getName());
        Matcher m2 = p.matcher(f2.getName());
        if(m1.find() &&  m2.find()){
            int num1 = Integer.parseInt(m1.group(1));
            int num2 = Integer.parseInt(m2.group(1));
            return num1-num2;
        }
        return 0;
    }


    //合并所有文件
    public  void merge(Multimap<String,File> mergeFileMap,String root){
        for (String relativePath : mergeFileMap.keySet()) {
            List<File> files = (List<File>) mergeFileMap.get(relativePath);

            //获取真实图片文件
            files = files.stream().map(f->new File(RdFileUtil.discardSuffix(f))).collect(Collectors.toList());
            String filePath = root + File.separator + relativePath;
            try {
                RdPdfUtil.mergePic(files,filePath);
            } catch (Exception e) {
                System.out.println("文件合并失败:\t"+e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }


    //不合并，移动拆分后的文件
//    public  void merge(Multimap<String,File> mergeFileMap,String root){
//        for (String relativePath : mergeFileMap.keySet()) {
//            List<File> files = (List<File>) mergeFileMap.get(relativePath);
//
//            //获取真实图片文件
//            files = files.stream().map(f->new File(RdFileUtil.discardSuffix(f))).collect(Collectors.toList());
//            String filePath = root + File.separator + relativePath;
//            File pdfFile = new File(filePath);
//            String fileName = FileUtil.mainName(pdfFile);
//
//            for (int i = 0; i < files.size(); i++) {
//                File jpgFile = files.get(i);
//                String htmlFile = jpgFile.getPath()+".html";
//                String newPath = pdfFile.getParent() + File.separator + fileName + File.separator + fileName+".pdf_"+(i+1)+".jpg";
//                FileUtil.copy(jpgFile,new File(newPath),false);
//                FileUtil.copy(htmlFile,newPath+".html",false);
//            }
//        }
//    }





    //列数据去重,添加
    public  void addDistinct(Map<String,Object> row,String headKey,String value){
        String content = row.get(headKey).toString();
        String[] values = content.split(";");
        boolean in = false;
        for (String v : values) {
            if(value.equals(v))
                in = true;
        }

        if(!in){
            content = content.concat(value+";");
            row.put(headKey,content);
        }
    }



    //设置表头
    public  void setHead(List<Map<String,Object>> rows,String[] head){
        for (Map<String, Object> row : rows) {
            for (String word : head) {
                if(row.get(word)==null){
                    row.put(word,"");
                }
            }
        }
    }


    //获取相对路径
    public  String getRelativePath(File file){
        Pattern p1 = Pattern.compile("\\d+年");
        Pattern p2 = Pattern.compile("\\d+月");

        String fileName = file.getName();
        Matcher m1 = p1.matcher(fileName);
        Matcher m2 = p2.matcher(fileName);

        if(m1.find() && m2.find()){
            String year = m1.group();
            String month = m2.group();
            fileName = "发票-"+fileName.substring(0,fileName.indexOf('.'))+".pdf";
            String path = File.separator + year + File.separator + month + File.separator + fileName;
            return path;
        }
        return null;
    }



    //获取页码
    public  int getPage(File file){
        Pattern p = Pattern.compile("\\.pdf_(\\d+)");
        String fileName = file.getName();

        Matcher m = p.matcher(fileName);
        if(m.find())
            return Integer.parseInt(m.group(1));
        return 0;
    }







    //判断这个文件是不是发票
    public  boolean isInvoice(File html){
        try {
            Document document = Jsoup.parse(html, "utf8");
            String content = document.toString();
            for (String s : keyWords1) {
                if(content.contains(s)) {
                    for (String s1 : keyWords2) {
                        if(content.contains(s1)) return true;
                    }
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static int containsOf(String[] array,String target){
        for (int i=0;i<array.length;i++) {
            if(target.contains(array[i]))
                return i;
        }
        return -1;
    }

}

