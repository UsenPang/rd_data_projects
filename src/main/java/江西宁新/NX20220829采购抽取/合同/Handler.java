package 江西宁新.NX20220829采购抽取.合同;


import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import lombok.Data;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.CommonUtil;
import utils.RdFileUtil;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 1.首先扫描指定目录下的所有文件，形成key-value 为 目录地址-文件list 的map。并且获取excel表中的数据，然后对excel中的合同号按照长度排序，去除不需要的数据
 * 2.遍历文件所有文件，对命中凭证号，命中后则将数据插入到excel记录对应的map中，如果未命中则继续抽取其他数据，同时需要一个计数器记录抽取到数据的文件个数(后面用于计算出未命中的文件数)
 * 3.输出excel表
 */
@Data
public class Handler {
    private static String[] heads = {"命中文件名称", "文件所属路径", "未命中文件数", "客户/供应商名称", "合同编号", "运费条款"};
    private static String[] reverved = {"合同编号", "客户/供应商名称"};
    private static String[] keyWord1 = {
            "交(提)货方式及费用承担",
            "交(提)货地点",
            "交(提)货时间",
            "交货地点",
            "交货地址",
            "交货方式",
            "交付方式",
            "交货时间",
            "供货价格",
            "送货运费",
            "以上价格",
            "运输方式",
            "运输要求",
            "运输费用负担",
            "运输费用由"};
    private static String[] keyWord2 = {
            "运费及其他一切附加费用",
            "按照乙方要求发货",
            "承担运费",
            "承揽方负责运费",
            "供方承担",
            "供方承担运输费用",
            "甲方承担",
            "甲方承担运费",
            "其他",
            "上门",
            "违约",
            "到付",
            "自提",
            "卸货由需方负责",
            "指定地点",
            "送货上门",
            "需方承担",
            "验收",
            "乙方承担",
            "乙方负责",
            "以上价格为送货到厂家",
            "由需方负责",
            "运费由买方负责",
            "运费由乙方负担",
            "运费由甲方负担",};

    private Pattern p = Pattern.compile("(.*\\.pdf|PDF)_\\d+.*");


    private String fileSrc;
    private String excelSrc;
    private String excelDest;
    private Map<String, List<File>> fileMap;
    private List<Map<String, Object>> rows;
    private List<Map<String, Object>> newRows = new ArrayList<>();
    private Set<String> hitContract = new HashSet<>();

    public Handler(String fileSrc, String excelSrc, String excelDest) {
        this.fileSrc = fileSrc;
        this.excelSrc = excelSrc;
        this.excelDest = excelDest;
        init();
    }

    public void run() {
        for (String path : fileMap.keySet()) {
            handle(path, fileMap.get(path));
        }
        List<Map<String,Object>> noHitRows = rows.parallelStream().filter(row->!hitContract.contains(row.get("合同编号").toString())).collect(Collectors.toList());
        newRows.addAll(noHitRows);
        ExcelWriter writer = ExcelUtil.getWriter(excelDest);
        writer.write(newRows);
        writer.close();
    }


    /**
     * 处理某个目录下的文件
     *
     * @param path  目录路径
     * @param files 目录下的文件
     */
    private void handle(String path, List<File> files) {
        int hitCount = 0;
        List<Map<String, Object>> tmpRows = new ArrayList<>();
        for (File file : files) {
            Document document = null;
            try {
                document = Jsoup.parse(file, "utf8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (document == null) continue;

            String content = document.toString();

            Map<String, Object> row = null;


            Optional<Map<String, Object>> matchRow = rows.stream().filter(r -> content.contains(r.get("合同编号").toString())).findFirst();
            if (matchRow.isPresent()) {
                row = matchRow.get();
                hitContract.add(row.get("合同编号").toString());
            }


            if (row == null)
                row = getInitRow();

            row.put("命中文件名称", getFileName(file));
            row.put("文件所属路径", path);

            Elements pEls = document.select("p");
            StringBuilder sb = new StringBuilder();
            boolean isEmpty = true;
            boolean isHit = false;
            for (int i = 0; i < pEls.size(); i++) {
                Element p = pEls.get(i);
                String text = CommonUtil.cleanBlank(p.text());

                if (isEmpty) {
                    int start = startIndex(text);
                    int end = start > 0 ? endIndex(text, start) : -1;
                    if (start != -1 && end != -1 && start < end) {
                        Map<String, Object> newRow = copyRow(row);
                        newRow.put("运费条款", text.substring(start, end));
                        tmpRows.add(newRow);
                        isHit = true;
                    } else if (start != -1 && end == -1) {
                        sb.append(text.substring(start));
                        isEmpty = false;
                        isHit = true;
                    }
                } else {
                    int end = endIndex(text, 0);
                    if (end != -1) {
                        sb.append(text.substring(0, end));
                        Map<String, Object> newRow = copyRow(row);
                        newRow.put("运费条款", sb.toString());
                        tmpRows.add(newRow);
                        sb = new StringBuilder();
                        isEmpty = true;
                        i--;
                    } else {
                        sb.append(text);
                    }
                }

            }

            if (!isEmpty) {
                Map<String, Object> newRow = copyRow(row);
                newRow.put("运费条款", sb.toString());
                tmpRows.add(newRow);
            }

            if (isHit) hitCount++;


        }

        int noHitCount = files.size() - hitCount;
        for (Map<String, Object> tmpRow : tmpRows)
            tmpRow.put("未命中文件数", noHitCount);
        newRows.addAll(tmpRows);
    }


    private Map<String, Object> copyRow(Map<String, Object> row) {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }


    //获取图片或者pdf的文件名
    private String getFileName(File file) {
        String fileName = file.getName();
        Matcher m = p.matcher(fileName);
        if (m.find())
            fileName = m.replaceAll("$1");
        else
            fileName = RdFileUtil.discardSuffix(fileName);
        return fileName;
    }


    /**
     * 判断该文本内容中的凭照是否能和excel中的凭证号匹配上,匹配上了则返回该条数据
     *
     * @param content 文本内容
     * @return
     */
    private Map<String, Object> getRowByVoucher(String content) {
        for (Map<String, Object> row : rows) {
            String voucher = row.get("合同编号").toString();
            if (!CommonUtil.isEmpty(voucher) && content.contains(voucher))
                return row;
        }
        return null;
    }


    //找到起始关键字的位置
    private int startIndex(String content) {
        for (String word : keyWord1) {
            int index = content.indexOf(word);
            if (index != -1) {
                return index;
            }
        }
        return -1;
    }

    //返回结束关键字的位置
    private int endIndex(String content, int start) {

        if (start == 0) {
            Pattern p = Pattern.compile("第?[一二三四五六七八九十]+条?");
            String preText = content.length() > 6 ? content.substring(0, 7) : content;
            Matcher m = p.matcher(preText);
            if (m.find())
                return preText.indexOf(m.group()) + m.group().length();
        }


        for (String word : keyWord2) {
            int index = content.indexOf(word, start);
            if (index != -1)
                return index + word.length();
        }
        return -1;
    }


    private void init() {
        fileMap = RdFileUtil.lsAllFilesByType(new File(fileSrc), "html");
        ExcelReader reader = ExcelUtil.getReader(excelSrc);
        rows = reader.readAll();
        reader.close();

        //剔除无用数据并排序
        rows = rows.parallelStream()
                .map(this::filration)
                .sorted((a, b) -> b.get("合同编号").toString().length() - a.get("合同编号").toString().length())
                .distinct()
                .collect(Collectors.toList());

        //初始化表头
        initHead();
    }


    //剔除无用数据，并返回一个新的记录
    private Map<String, Object> filration(Map<String, Object> row) {
        Map<String, Object> newRow = new HashMap<>();
        for (String head : reverved) {
            newRow.put(head, row.get(head));
        }
        return newRow;
    }


    //初始化表格每一行的表头
    private void initHead() {
        for (Map<String, Object> row : rows) {
            for (String head : heads) {
                if (row.get(head) == null)
                    row.put(head, "");
            }
        }
    }


    private Map<String, Object> getInitRow() {
        Map<String, Object> row = new HashMap<>();
        for (String head : heads) {
            if (row.get(head) == null)
                row.put(head, "");
        }
        return row;
    }
}
