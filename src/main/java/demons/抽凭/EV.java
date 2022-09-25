package demons.抽凭;

import cn.hutool.core.lang.Assert;
import com.itextpdf.text.DocumentException;
import demons.common.Executor;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import utils.RdFileUtil;
import utils.RdPdfUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
public class EV extends Executor {
    private String indexKey;
    private List<String> outPathKey;
    private Pattern pattern;
    private List<String> extraKeyWord;
    private Map<String, List<File>> willMergeMap;

    public EV() {
        this.willMergeMap = new HashMap<>();
    }


    public void handleSinglePage() throws Exception {
        scanFiles();  //扫描所有文件
        files = sort(files);   //按指定规则排序
        readExcel();        //读取控制表
        handle(files);  //处理这些文件
        mergeAll();   //合并抽凭后的文件
        setTableHeadWords(tableHeadWords);
        writeExcel();  //写出命中统计
    }


    public void handle(List<File> files) {

        List<File> fileList = new ArrayList<>();
        for (File file : files) {
            Document document = null;
            try {
                document = Jsoup.parse(file, "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (document == null)
                continue;

            //获取段落内容，并去掉空格
            String content = document.select("p").text();

            //如果命中凭证号，计算出该凭证的输出路径，并创建一个新的list存放对应的凭证页
            Map<String, Object> hitRow = matchRowByIndex(content);
            if (containKeyWord(content) && hitRow != null) {
                String relativePath = "";
                for (String key : outPathKey) {
                    relativePath += File.separator + hitRow.get(key);
                }
                relativePath = relativePath + File.separator + hitRow.get("索引号") + ".pdf";
                fileList = new ArrayList<>();
                willMergeMap.put(relativePath, fileList);
            }

            File img = new File(RdFileUtil.discardSuffix(file));
            fileList.add(img);
        }

        //设置要导出的excel表格数据
        outRows = rows.stream().map(this::filtration).collect(Collectors.toList());
    }

    public Map<String, Object> filtration(Map<String, Object> row) {
        Map<String, Object> newRow = new HashMap<>();
        newRow.put("索引号", row.get("索引号"));
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
     * @param content
     * @return
     */
    public Map<String, Object> matchRowByIndex(String content) {
        Assert.notNull(pattern, "请添加匹配规则");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find())
            return null;

        String keyWord = matcher.group();


        Optional<Map<String, Object>> hitResult = rows
                .parallelStream()
                .filter(row -> keyWord.equals(row.get(indexKey) + ""))
                .findFirst();

        if (hitResult.isPresent()) {
            Map<String, Object> hitRow = hitResult.get();
            hitRow.put("命中统计", 1);
            return hitRow;
        }
        return null;
    }


    /**
     * 用户可以输入一些可选的关键字，加大命中率
     *
     * @param content
     * @return
     */
    public boolean containKeyWord(String content) {
        if (extraKeyWord == null || extraKeyWord.isEmpty())
            return true;

        for (String keyWord : extraKeyWord) {
            if (content.contains(keyWord))
                return true;
        }
        return false;
    }
}
