package 博纳斯威.BN20220920合同单号提取.合同单号抽取;

import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import 博纳斯威.BN20220920合同单号提取.executor.SimpleExecutor;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    static String[] headKey = {"文件路径信息","文件名称","凭证号","页码","合同单号"};

    public static void main(String[] args) {
        String excelIn = "E:\\博纳斯威\\合同号抽取\\控制表\\博纳斯威合同抽取控制表.xlsx";
        String excelOut = "E:\\博纳斯威\\合同号抽取\\抽取结果\\博纳斯威合同抽取.xlsx";


        String[] sheets = {"2019年","2020年","2021年","2022年"};
        String source = "D:\\工作空间\\博纳斯威\\凭证与合同整理_解析";

        for (String sheet : sheets) {
            ExcelReader reader1 = ExcelUtil.getReader(new File(excelIn), sheet);

            List<Map<String, Object>> rows = new ArrayList<>();
            rows.addAll(reader1.readAll());
            reader1.close();


            Set<String> indexs = rows.stream().map(row -> row.get("凭证号").toString()).collect(Collectors.toSet());


            Set<String> hit = new HashSet<>();

            SimpleExecutor executor = new SimpleExecutor();
            executor.setSource(source);
            executor.setExcelPath(excelOut,sheet);
            executor.setFilter(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if(pathname.getName().endsWith("html")){
                        String fileName = mainName(pathname);
                        String voucherName = FileUtil.getParent(pathname,2).getName();
                        if( indexs.contains(voucherName)){
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
//                        name = name.substring(name.indexOf("-")+1);
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
