package 江西宁新.NX20220825抽取.结算单;


import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import 江西宁新.NX20220825抽取.executor.Executor;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static String[] headKey={"文件路径信息","文件名称","凭证号","页码","订单号","规格型号","数量(吨)","数量(块)","数量"};

    public static void main(String[] args) {
        String preffix = "销售结算单-";
        String tablePath = "C:\\Users\\荣大\\Desktop\\NX20220829采购抽取\\凭证号整理完整版";
        String excelOut = "E:\\江西宁新\\宁新抽取\\宁新销售抽取\\结算单";
        String[] tables = new File(tablePath).list();

        String[] sources = {"E:\\江西宁新\\分类解析后文件_新版\\宁和达"
                ,"E:\\江西宁新\\分类解析后文件_新版\\宁新新材"
                ,"E:\\江西宁新\\分类解析后文件_新版\\宁易邦"
                ,"E:\\江西宁新\\分类解析后文件_新版\\宁昱鸿"};


        for (int i = 0; i < tables.length; i++) {

            //表格输入路径
            String table = tables[i];
            String excelInput = tablePath+File.separator+table;

            //原文件路径
            int index = indexOf(sources,table);
            String sourcePath = sources[index];

            //excelOutFile
            String excelOutFile = excelOut+File.separator+table;


            ExcelReader reader1 = ExcelUtil.getReader(new File(excelInput), "销售记账凭证");
            ExcelReader reader2 = ExcelUtil.getReader(new File(excelInput), "销售回款凭证");


            List<Map<String, Object>> rows = new ArrayList<>();
            rows.addAll(reader1.readAll());
            rows.addAll(reader2.readAll());

            reader1.close();
            reader2.close();

            Set<String> indexs = rows.stream().map(row -> preffix + row.get("索引号").toString()).collect(Collectors.toSet());

            Set<String> hit = new HashSet<>();

            Executor executor = new Executor();
            executor.setSource(sourcePath);
            executor.setExcelPath(excelOutFile);
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
