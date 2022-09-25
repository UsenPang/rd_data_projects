package demons.common;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;

import lombok.Data;
import utils.RdFileUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public abstract class Executor {
    protected String source;
    protected String dest;
    protected String excelSource;
    protected String excelDest;
    protected List<Map<String, Object>> rows;
    protected List<Map<String, Object>> outRows;
    protected String[] tableHeadWords;
    protected Comparator<File> comparator;
    protected FileFilter fileFilter = f->f.getName().endsWith(".html");

    /**
     * source目录下的所有的文件
     */
    protected List<File> files;

    /**
     * List<File>存储了一个目录下的所有html文件，只限于这个目录，不包含子目录下的文件.
     * multiFiles存储了所有这种关系的目录文件
     */
    protected List<List<File>> multiFiles;




    public void setTableHeadWords(String ...tableHeadWords){
        this.tableHeadWords = tableHeadWords;
    }

    /**
     * 对所有拆分解析后的文件排序
     *
     * @param fileList 需要排序的文件列表
     */
    public List<File> sort(List<File> fileList) {
        if (comparator == null)
            return fileList;
        return fileList.parallelStream().sorted(comparator).collect(Collectors.toList());
    }

    /**
     * 扫描 source目录下的所有文件
     */
    public List<File> scanFiles() {
        long start = System.currentTimeMillis();
        System.out.println("正在扫描:"+source);
        this.files = FileUtil.loopFiles(source, fileFilter);
        long end = System.currentTimeMillis();
        System.out.println("扫描结束,耗时"+(end-start)+"ms");
        files = sort(files);
        return files;
    }


    /**
     * 扫描 source目录下的所有目录下对应的文件(只包含目录当前目录，不包含子目录下的文件)
     */
    public List<List<File>> scanDirFiles(){
        long start = System.currentTimeMillis();
        System.out.println("正在扫描:"+source);
        try {
            this.multiFiles = RdFileUtil.lsDirFiles(source, fileFilter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("扫描结束,耗时"+(end-start)+"ms");
        multiFiles = multiFiles.stream().map(files->sort(files)).collect(Collectors.toList());
        return multiFiles;
    }




    /**
     * 读取表格中的所有数据到 rows
     */
    public void readExcel() {
        Assert.notEmpty(excelDest, "请设置需要读取的excel路径");
        ExcelReader reader = ExcelUtil.getReader(excelSource);
        rows = reader.readAll();
        reader.close();
    }


    /**
     * 将 rows 的数据
     */
    public void writeExcel() {
        Assert.notEmpty(excelDest, "请设置需要写出的excel路径");
        ExcelWriter writer = ExcelUtil.getWriter(excelDest);
        setTableHead(outRows);
        writer.write(outRows);
        writer.close();
    }


    public void setTableHead(List<Map<String, Object>> rows) {
        rows.parallelStream().forEach(this::setRowHead);
    }

    public void setRowHead(Map<String, Object> row) {
        for (String headWord : tableHeadWords) {
            if (row.get(headWord) == null)
                row.put(headWord, "");
        }
    }

}
