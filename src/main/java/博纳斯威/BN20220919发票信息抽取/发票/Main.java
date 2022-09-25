package 博纳斯威.BN20220919发票信息抽取.发票;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import 博纳斯威.BN20220919发票信息抽取.executor.SimpleExecutor;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    static String[] headKey = {"文件路径信息","文件名称","凭证号","页码","开票日期","发票号码","小写合计金额","大写合计金额"};

    public static void main(String[] args) {

        String prefix = "发票-";
        String tableIn = "D:\\工作空间\\博纳斯威\\博纳斯威发票信息抽取\\发票抽取控制表\\凭证总表.xlsx";
        String[] sheets = {"19年","20年","21年","22年"};
        String tableOut = "D:\\工作空间\\博纳斯威\\博纳斯威发票信息抽取\\抽取结果\\博纳斯威发票抽取.xlsx";
        String source = "D:\\工作空间\\博纳斯威\\博纳斯威发票信息抽取\\发票拆分解析";

        for (String sheet : sheets) {
            ExcelReader excelReader = ExcelUtil.getReader(new File(tableIn), sheet);
            List<Map<String, Object>> rows = excelReader.readAll();
            excelReader.close();


            Set<String> indexs = rows.stream().map(row -> prefix + row.get("凭证号").toString()).collect(Collectors.toSet());


            Set<String> hit = new HashSet<>();

            SimpleExecutor executor = new SimpleExecutor();
            executor.setSource(source);
            executor.setExcelPath(tableOut,sheet);
            executor.setFilter(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if(pathname.getName().endsWith("html")){
                        String fileName = mainName(pathname);
                        if( indexs.contains(fileName)){
                            hit.add(fileName);
                            return true;
                        }
                    }
                    return false;
                }
            });

            Handler handler = new Handler();
            handler.setHeadKey(headKey);
            executor.setHandler(handler);
            executor.run();

            indexs.removeAll(hit);
            List<Map<String, Object>> attachRows = indexs.stream()
                    .map(name -> {
                        name = name.substring(name.indexOf("-")+1);
                        Map<String, Object> map = getRow();
                        map.put("凭证号", name);
                        return map;
                    }).collect(Collectors.toList());
            executor.appendRows(attachRows);
            executor.writeResult();
        }
    }
    public static String mainName(File f){
        String fileName = f.getName();
        fileName = fileName.substring(0,fileName.indexOf('.'));
        return fileName;
    }


    public static Map<String,Object> getRow(){
        Map<String,Object> row = new HashMap<>();

        for (String key : headKey) {
            row.put(key,"");
        }
        return row;
    }


    public static int indexOf(String[] words,String target){
        for (int i = 0; i < words.length; i++) {
            if(target.contains(FileUtil.getName(words[i])))
                return i;
        }
        return -1;
    }
}
