package demons.抽凭;

import cn.hutool.core.lang.Assert;
import com.itextpdf.text.DocumentException;
import demons.common.Executor;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import utils.CommonUtil;
import utils.RdFileUtil;
import utils.RdPdfUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class ExtractVoucher extends Executor {

    private String voucherHead;  //凭证号表头关键字
    private String voucherName = "索引号"; //凭证名称表头关键字
    private String[] outPathKey; //输出的文件路径表头关键字
    private VoucherMatcher voucherMatcher;
    private Map<String, List<File>> willMergeMap;

    public ExtractVoucher() {
        this.willMergeMap = new HashMap<>();
    }


    public void setOutPathKey(String ...outPathKey){
        this.outPathKey = outPathKey;
    }


    public void handleAllPdf() throws Exception {
        files = scanFiles();   //扫描所有文件,按指定规则排序
        readExcel();        //读取控制表
        handle(files);  //处理这些文件
        mergeAll();   //合并抽凭后的文件
        setTableHeadWords(new String[]{voucherName,"命中统计"});
        writeExcel();  //写出命中统计
    }


    public void handleSinglePdf() throws Exception {
        multiFiles = scanDirFiles();
        readExcel();        //读取控制表
        multiFiles.stream().forEach(fList->{
            handle(fList);  //处理这些文件
        });
        mergeAll();   //合并抽凭后的文件
        setTableHeadWords(new String[]{voucherName,"命中统计"});
        writeExcel();  //写出命中统计
    }



    public void handle(List<File> files) {

        List<File> fileList = new ArrayList<>();
        Map<String, Object> preHitRow = null;
        for (File file : files) {
            Document document = null;
            try {
                document = Jsoup.parse(file, "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (document == null)
                continue;

            //获取段落元素
            Elements pELs = document.select("p");


            //如果命中凭证号，计算出该凭证的输出路径，并创建一个新的list存放对应的凭证页
            Map<String, Object> hitRow = matchRowByIndex(pELs);

            if (hitRow != null && hitRow != preHitRow) {
                preHitRow = hitRow;

                String relativePath = "";
                for (String key : outPathKey) {
                    relativePath += File.separator + hitRow.get(key);
                }
                relativePath = relativePath + File.separator + hitRow.get(voucherName) + ".pdf";
                if (willMergeMap.get(relativePath) == null) {
                    fileList = new ArrayList<>();
                    willMergeMap.put(relativePath, fileList);
                }
            }

            File img = new File(RdFileUtil.discardSuffix(file));
            fileList.add(img);
        }

        //设置要导出的excel表格数据
        outRows = rows.stream().map(this::filtration).collect(Collectors.toList());
    }





    public Map<String, Object> filtration(Map<String, Object> row) {
        Map<String, Object> newRow = new HashMap<>();
        newRow.put(voucherName, row.get(voucherName));
        Object hitNum = row.get("命中统计");
        hitNum = Objects.isNull(hitNum) ? 0 : hitNum;
        newRow.put("命中统计", hitNum);
        return newRow;
    }


    /**
     * 将带需合并的文件一次性合并，输出到dest目录中
     *
     * @throws DocumentException
     * @throws IOException
     */
    public void mergeAll() throws DocumentException, IOException {
        for (String path : willMergeMap.keySet()) {
            RdPdfUtil.mergePic(willMergeMap.get(path), new File(dest, path).getPath());
        }
    }


    /**
     * 根据输入的规则抽取字符串中的凭证号，并查找excel中与凭照对应的记录
     *
     * @param pEls
     * @return
     */
    public Map<String, Object> matchRowByIndex(Elements pEls) {

        Assert.notNull(voucherMatcher, "请添加凭证匹配器");
        String keyWord = voucherMatcher.findVoucher(pEls);
        if (CommonUtil.isEmpty(keyWord))
            return null;

        Optional<Map<String, Object>> hitResult = rows
                .parallelStream()
                .filter(row -> keyWord.equals(row.get(voucherHead) + ""))
                .findFirst();

        if (hitResult.isPresent()) {
            Map<String, Object> hitRow = hitResult.get();
            hitRow.put("命中统计", 1);
            return hitRow;
        }
        return null;
    }
}
