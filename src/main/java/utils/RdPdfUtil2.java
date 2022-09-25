package utils;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import org.apache.commons.collections4.ListUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RdPdfUtil2 {
    //默认ocr接口
    private static String[] ocrApis = {"http://222.128.127.248:10020/icr/recognize_table_multipage", "http://222.128.127.248:10021/icr/recognize_table_multipage"};


    //核心线程数
    private static int corePoolSize = 2;
    //最大线程数
    private static int maximumPoolSize = 4;
    //线程任务执行完毕后将在指定时间后被销毁
    private static long keepAliveTime = 5;
    //时间单位，默认为秒
    private static TimeUnit timeUnit = TimeUnit.SECONDS;
    //任务队列容量
    private static int capacity = 200;
    //切分任务线程池
    private static ThreadPoolExecutor splitThreadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, new ArrayBlockingQueue<>(capacity), new ThreadPoolExecutor.CallerRunsPolicy());
    //解析任务线程池
    private static ThreadPoolExecutor parseThreadPool = new ThreadPoolExecutor(4, 4, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
//    private static ExecutorService parseThreadPool = Executors.newSingleThreadExecutor();

    //当前ocr api索引
    private static AtomicInteger currentApi = new AtomicInteger();

//    static {
//        //添加一个线程来监视线程池中的任务是否执行完毕，执行完后关闭线程池
//        addWatcher(threadPool);
//        addWatcher(parseThreadPool);
//    }

    public static void addWatcher(ThreadPoolExecutor executor) {
        executor.execute(() -> {
            boolean isRun = true;
            while (isRun) {
                try {
                    Thread.sleep(10000);
                    int activeCount = executor.getActiveCount();
                    if (activeCount == 1) {
                        isRun = false;
                        executor.shutdown();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 采用轮询策略
     *
     * @return
     */
    private static String getOcrApi() {
        int index = currentApi.getAndIncrement() % ocrApis.length;
        return ocrApis[index];
    }

    /**
     * 可以通过设置orc的api，改变ocr的调用
     *
     * @param apis
     */
    public static void setOcrApis(String... apis) {
        if (apis == null || apis.length == 0)
            return;
        ocrApis = apis;
    }


    public static void lsAllCast2Pic(String source) throws InterruptedException {
        Pattern p = Pattern.compile("\\.(pdf|PDF)_\\d+\\.pdf");
        List<File> fileList = FileUtil.loopFiles(new File(source), f -> !f.getName().endsWith(".html") && p.matcher(f.getName()).find());


        //设置线程池参数
        corePoolSize = Runtime.getRuntime().availableProcessors() - 2;
        maximumPoolSize = corePoolSize + 1;
        splitThreadPool.setCorePoolSize(corePoolSize);
        splitThreadPool.setMaximumPoolSize(maximumPoolSize);


        int listNum = corePoolSize * 5;
        listNum = fileList.size() > listNum ? listNum : 1;
        List<List<File>> lls = ListUtils.partition(fileList, (fileList.size() + listNum - 1) / listNum);
        for (List<File> list : lls) {
            splitThreadPool.execute(() -> {
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    File file = list.get(i);
                    //转换图片
                    simplePagePdf2Pic(file);
                    list.set(i, null);
                }
            });
        }
    }


    public static void lsSplitAndParse(String src) {
        lsSplitAndParse(src, src, true);
    }

    public static void lsSplitAndParse(String src, String dest) {
        lsSplitAndParse(src, dest, false);
    }


    public static void lsSplitAndParse(String src, String dest, boolean isDelete) {
        String picType = FileType.IMAGE.getType();
        List<File> fileList = FileUtil.loopFiles(dest, f -> picType.contains(FileUtil.getSuffix(f)));

        fileList.forEach(f -> {
            parseThreadPool.execute(() -> parse(f, getOcrApi()));
        });
        fileList.clear();


        Pattern p = Pattern.compile("\\.(pdf|PDF)_\\d+\\.pdf");

        //找出所有被拆分成pdf,但还没被转换成JPG的文件
        fileList = FileUtil.loopFiles(new File(src), f -> p.matcher(f.getName()).find());

        //未拆分pdf执行拆分，然后加入到列表中
        fileList.addAll(lsSplit2Pdf(src, dest, isDelete));


        //设置线程池参数
        corePoolSize = Runtime.getRuntime().availableProcessors() - 4;
        maximumPoolSize = corePoolSize + 1;
        splitThreadPool.setCorePoolSize(corePoolSize);
        splitThreadPool.setMaximumPoolSize(maximumPoolSize);
        splitThreadPool.setKeepAliveTime(10, TimeUnit.SECONDS);

        //执行只有一页的pdf转图片任务
        List<Future<File>> results = new ArrayList<>();
        fileList.stream().forEach(f -> {
            results.add(splitThreadPool.submit(() -> simplePagePdf2Pic(f)));
        });

        //清空列表
        fileList.clear();

        results.forEach(result -> {
            try {
                File file = result.get();
                if (file != null)
                    parseThreadPool.execute(() -> parse(file, getOcrApi()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /*
        lsSplit2Jpg()方法用于扫描src目录下所有的pdf,并将这些pdf文件拆分成图片
     */

    /**
     * 改方法会将拆分后的文件保存到源文件的根目录下，拆分后会将源文件删除
     *
     * @param src 包含pdf文件的根目录
     */
    public static void lsSplit2Jpg(String src) {
        lsSplit2Jpg(src, src, true);
    }

    /**
     * 改方法会将拆分后的文件保存到指定根目录下，拆分后不会将源文件删除
     *
     * @param src  包含pdf文件的根目录
     * @param dest 拆分后的jpg文件保存的根目录
     */
    public static void lsSplit2Jpg(String src, String dest) {
        lsSplit2Jpg(src, dest, false);
    }

    /**
     * 改方法会将拆分后的文件保存到指定根目录下，可以根据需要指定源文件是否删除
     * 执行该方法不会阻塞
     *
     * @param src      包含pdf文件的根目录
     * @param dest     拆分后的jpg文件保存的根目录
     * @param isDelete 是否删除原文件
     */
    public static void lsSplit2Jpg(String src, String dest, boolean isDelete) {
        Pattern p = Pattern.compile("\\.(pdf|PDF)_\\d+\\.pdf");
        //找出所有被拆分成pdf,但还没被转换成JPG的文件
        List<File> fileList = FileUtil.loopFiles(src, f -> p.matcher(f.getName()).find());
        //未拆分pdf执行拆分，然后加入到列表中
        fileList.addAll(lsSplit2Pdf(src, dest, isDelete));

        //设置线程池参数
        corePoolSize = Runtime.getRuntime().availableProcessors() - 4;
        maximumPoolSize = corePoolSize + 1;
        splitThreadPool.setCorePoolSize(corePoolSize);
        splitThreadPool.setMaximumPoolSize(maximumPoolSize);
        splitThreadPool.setKeepAliveTime(10, TimeUnit.SECONDS);

        //执行只有一页的pdf转图片任务
        fileList.stream().forEach(f -> {
            splitThreadPool.execute(() -> simplePagePdf2Pic(f));
        });

        //清空列表
        fileList.clear();
    }

    /*
     * 使用lsSplit2Pdf()方法将pdf切分成一页一页的pdf,需指定包含pdf文件的根路径，
     * 可以指定pdf输出路径，默认不删除原pdf文件
     * 也可以设置isDelete参数,指定是否删除源文件
     */

    /**
     * 将所有拆分后的pdf保存到根目录，并删除源文件
     *
     * @param src pdf文件根目录
     * @return
     */
    public static List<File> lsSplit2Pdf(String src) {
        return lsSplit2Pdf(src, src, true);
    }

    /**
     * 指定了输出的目录，不会删除原文件
     *
     * @param src  pdf文件根目录
     * @param dest 拆分后pdf文件输出根目录
     * @return
     */
    public static List<File> lsSplit2Pdf(String src, String dest) {
        return lsSplit2Pdf(src, dest, false);
    }


    /**
     * @param src      pdf文件根目录
     * @param dest     拆分后pdf文件输出根目录
     * @param isDelete 指定是否删除源文件
     * @return 返回所有切分后的pdf文件
     */
    public static List<File> lsSplit2Pdf(String src, String dest, boolean isDelete) {
        String pdfType = "PDF pdf";
        Pattern p = Pattern.compile("\\.(PDF|pdf)_\\d+");

        //列出根目录下所有的pdf文件
        List<File> pdfFiles = FileUtil.loopFiles(src, f -> {
            String suffix = FileUtil.getSuffix(f);
            return !StringUtil.isBlank(suffix) && pdfType.contains(suffix);
        });
        //过滤掉拆分后的pdf文件
        pdfFiles = pdfFiles.parallelStream().filter(f -> !p.matcher(f.getName()).find()).collect(Collectors.toList());

        //设置线程池参数
        corePoolSize = Runtime.getRuntime().availableProcessors();
        maximumPoolSize = corePoolSize * 2;
        splitThreadPool.setCorePoolSize(corePoolSize);
        splitThreadPool.setMaximumPoolSize(maximumPoolSize);
        splitThreadPool.setKeepAliveTime(5, TimeUnit.SECONDS);

        //使用线程池执行拆分任务,返回拆分后的文件对象
        List<Future<List<File>>> results = new ArrayList<>();
        pdfFiles.stream().forEach(f -> {
            String storeDir = f.getParent().replace(src, dest) + File.separator + FileUtil.mainName(f);
            Future<List<File>> result = splitThreadPool.submit(() -> splitToPdf(f, storeDir, isDelete));
            results.add(result);
        });

        //清除列表
        pdfFiles.clear();

        //获取结果，加入列表
        List<File> finalPdfFiles = pdfFiles;
        results.forEach(res -> {
            try {
                List<File> fs = res.get();
                if (fs != null && !fs.isEmpty())
                    finalPdfFiles.addAll(fs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return finalPdfFiles;
    }


    /**
     * 将指定的pdf文件按页拆分
     *
     * @param pdfFile  目标pdf文件
     * @param destDir  拆分后的文件保存目录
     * @param isDelete 拆分后是否删除原文件
     * @return
     */
    public static List<File> splitToPdf(File pdfFile, String destDir, boolean isDelete) {
        List<File> splits = new ArrayList<>();

        //获取改路径的正确表示形式
        destDir = FileUtil.getCanonicalPath(new File(destDir));
        File dirFile = new File(destDir);

        if (!dirFile.exists()) dirFile.mkdirs();

        PdfReader reader = null;

        try {
            reader = new PdfReader(pdfFile.getPath());

            // PageNumber是从1开始计数的
            int numberOfPages = reader.getNumberOfPages();
            int pageNumber = 1;
            while (pageNumber <= numberOfPages) {
                Document doc = new Document();
                String fileName = pdfFile.getName() + "_" + pageNumber + ".pdf";

                PdfCopy pdfCopy = null;
                File splitFile = new File(dirFile, fileName);
                pdfCopy = new PdfCopy(doc, new FileOutputStream(splitFile));
                splits.add(splitFile);

                // 将pdf按页复制到新建的PDF中
                doc.open();
                doc.newPage();
                PdfImportedPage page = pdfCopy.getImportedPage(reader, pageNumber);
                pdfCopy.addPage(page);

                pdfCopy.close();
                doc.close();
                pageNumber++;
            }

            reader.close();
            if (isDelete) pdfFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("问题文件:\t" + pdfFile);
        }
        return splits;
    }

    /**
     * 将单页的pdf(这个pdf文件只有一页)转换成jpg类型的图片
     *
     * @param pdfFile
     */
    public static File simplePagePdf2Pic(File pdfFile) {
        if (!FileUtil.exist(pdfFile))
            return null;

        PDDocument doc = null;
        try {
            //拆分后的图片名称
            String destPath = RdFileUtil.discardSuffix(pdfFile.getPath()) + ".jpg";
            File pictureFile = new File(destPath);
            //已经拆分过的不在创建
            if (!pictureFile.exists()) {
                doc = PDDocument.load(pdfFile, MemoryUsageSetting.setupTempFileOnly());
                PDFRenderer renderer = new PDFRenderer(doc);
                //第二个参数越大生成图片分辨率越高。
                BufferedImage image = renderer.renderImageWithDPI(0, 105);
                //生成图片
                ImageIO.write(image, "jpg", pictureFile);
                doc.close();
            }
            //删除原pdf文件
            FileUtil.del(pdfFile);
            System.out.println("转换完成:\t" + pdfFile);
            return pictureFile;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("问题文件：\t" + pdfFile);
        }
        return null;
    }


    /**
     * 解析文件，并保存为html
     *
     * @param file
     * @param actionUrl
     */
    public static void parse(File file, String actionUrl) {
        File htmlFile = new File(file.getPath() + ".html");
        if (htmlFile.exists())
            return;


        org.jsoup.nodes.Document doc = Ocr.getOcrJSON(actionUrl, file, 0);
        if (doc == null) {
            System.out.println("无法解析:\t" + file);
            return;
        }
        String content = doc.toString();
        FileUtil.writeUtf8String(content, htmlFile);
        System.out.println("解析完成:\t" + file);
    }


    /**
     * 扫描src目录下所有切分后的文件，调用ocr接口解析
     *
     * @param src 包含切分后文件根目录
     */
    public static void lsParse(String src) {
        Pattern p = Pattern.compile("\\.(pdf|PDF)_\\d+\\.pdf");
        String picType = FileType.IMAGE.getType();

        //找出所有切分的文件(包括pdf和图片)
        List<File> fileList = FileUtil.loopFiles(src, f -> {
            String fileName = f.getName();
            return picType.contains(FileUtil.getSuffix(fileName)) || (!fileName.endsWith(".html") && p.matcher(fileName).find());
        });

        if (fileList == null || fileList.isEmpty())
            return;

        //去重
        fileList.stream().distinct();


//        //对fileList 分组
//        List<List<File>> multiFileList = new ArrayList<>();
//        //分组数
//        int listNum = 4;
//        int count = (fileList.size() + listNum - 1) / listNum;
//        Stream.iterate(0, n -> n + 1).limit(listNum).forEach(i -> {
//            multiFileList.add(fileList.stream().skip(i * count).collect(Collectors.toList()));
//        });


        //提交解析任务
        for (int i = 0; i < fileList.size(); i++) {
            int finalI = i;
            parseThreadPool.execute(() -> {
                parse(fileList.get(finalI), getOcrApi());
                fileList.set(finalI, null);  //解析后的文件设置为空，以便垃圾回收
            });
        }
    }


    /**
     * 将list中所有的图片文件按顺序合成一个pdf文件
     *
     * @param files     需要被合并的文件列表
     * @param storeFile 合并后的文件路径
     * @throws IOException
     * @throws DocumentException
     */
    public static void mergePic(List<File> files, String storeFile) throws IOException, DocumentException {
        File parent = new File(FileUtil.getParent(storeFile, 1));
        if (!parent.exists())
            FileUtil.mkdir(parent);

        // 创建一个 document 流
        Document document = new Document(PageSize.A4, 0, 0, 0, 0);
        FileOutputStream fos = new FileOutputStream(storeFile);
        PdfWriter.getInstance(document, fos);
        //打开文档
        document.open();

        for (File img : files) {
            //获取图片的宽高
            com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(img.getPath());
            // 也可设置页面尺寸
            float imageHeight = image.getScaledHeight();
            float imageWidth = image.getScaledWidth();
            document.setPageSize(new Rectangle(imageWidth, imageHeight));

            //新建一页添加图片
            document.newPage();
            document.add(image);
        }
        document.close();
        fos.flush();
        fos.close();
    }

    public static void picRecover2Pdf(String src) throws DocumentException, IOException {
        Pattern p = Pattern.compile("pdf_(\\d+)");
        Map<String, List<File>> fileMap = RdFileUtil.lsAllFilesByType(new File(src), f -> f.getName().endsWith(".jpg") && p.matcher(f.getName()).find());


        for (Map.Entry<String, List<File>> entry : fileMap.entrySet()) {
            List<File> files = entry.getValue();

            Collections.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    Matcher m1 = p.matcher(o1.getName());
                    Matcher m2 = p.matcher(o2.getName());

                    if (m1.find() && m2.find()) {
                        int num1 = Integer.parseInt(m1.group(1));
                        int num2 = Integer.parseInt(m2.group(1));
                        return num1 - num2;
                    }
                    return 0;
                }
            });

            String storePath = entry.getKey() + ".pdf";
            RdPdfUtil.mergePic(files, storePath);
            FileUtil.del(entry.getKey());
        }
    }


    /**
     * 将有合并单元格的表格，拆分，使其变得规整
     *
     * @param tableElement
     * @return
     */
    public static Element tableBreaking(Element tableElement) {

        try {
            Element tmpElement = tableElement;
            if (null != tableElement.getElementsByTag("tbody")) {
                tableElement = tableElement.getElementsByTag("tbody").first();
            }
            Elements trs = tableElement.getElementsByTag("tr");
            int trsCount = trs.size(); // 行数
            int tdsCount = 0; // 列数 需计算
            for (org.jsoup.nodes.Element tr : trs) {
                int tmpTdsCount = 0;
                for (Element td : tr.getElementsByTag("td")) {
                    tmpTdsCount += StringUtil.isBlank(td.attr("colspan")) ? 1 : Integer.parseInt(td.attr("colspan"));
                }
                if (tmpTdsCount > tdsCount) {
                    tdsCount = tmpTdsCount;
                }
            }
            int indexY = 0, indexX = 0;
            Element[][] tableElements = new Element[trsCount][tdsCount];
            for (Element tr : trs) {
                indexX = trs.indexOf(tr);
                indexY = 0;
                for (Element td : tr.getElementsByTag("td")) {
                    int colspan = StringUtil.isBlank(td.attr("colspan")) ? 1 : Integer.parseInt(td.attr("colspan"));
                    int rowspan = StringUtil.isBlank(td.attr("rowspan")) ? 1 : Integer.parseInt(td.attr("rowspan"));
                    org.jsoup.nodes.Element e = td.clone();
                    e.removeAttr("colspan").removeAttr("rowspan");
                    for (int i = 0; i < colspan; i++) {
                        for (int j = 0; j < rowspan; j++) {
                            try {
                                while (tableElements.length > indexX + j
                                        && tableElements[indexX + j].length > indexY + i
                                        && tableElements[indexX + j][indexY + i] != null) {
                                    indexY++;
                                }
                            } catch (Exception ex) {
                                System.out.println();
                            }
                            if (tableElements.length > indexX + j
                                    && tableElements[indexX + j].length > indexY + i) {
                                tableElements[indexX + j][indexY + i] = e;
                            }

                        }
                    }
                    indexY += colspan;
                }
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < trsCount; i++) {
                sb.append("\n<tr>");
                for (int j = 0; j < tdsCount; j++) {
                    if (tableElements[i][j] == null) {
                        continue;
                    }
                    sb.append(tableElements[i][j].toString());
                }
                sb.append("</tr>");
            }
            tmpElement.html(sb.toString());
            return tmpElement;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("拆分表格出现问题");
        }
        return tableElement;
    }


}
