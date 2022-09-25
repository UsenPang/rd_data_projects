package utils;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.collections4.ListUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.jsoup.helper.StringUtil;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @program: RdProjects
 * @description:
 * @author: 作者
 * @create: 2022-08-17 10:31
 */
public class RdPdfUtil {
    private static String[] orcApis = {"http://222.128.127.248:10020/icr/recognize_table_multipage", "http://222.128.127.248:10021/icr/recognize_table_multipage"};
    private static Date date;
    private static String root = "d:\\解析失败文件\\";

    public static void splitAndParseAll(String src, String dest, FileType splitType) throws InterruptedException {
        lsAllSplit(src, dest, splitType);
        parseAll(dest, splitType);
    }


    public static void lsAllSplit(String src, FileType splitType) throws InterruptedException {
        lsAllSplit(src, src, splitType, true);
    }

    public static void lsAllSplit(String src, String dest, FileType splitType) throws InterruptedException {
        lsAllSplit(src, dest, splitType, false);
    }

    public static void lsAllSplit(String src, String dest, FileType splitType, boolean isDelete) throws InterruptedException {
        Pattern p = Pattern.compile("(pdf|PDF)");
        List<File> fileList = FileUtil.loopFiles(src, f -> p.matcher(FileUtil.getSuffix(f)).find());

        int corePoolSize = 3;
        int maximumPoolSize = 3;
        long keepAliveTime = 10;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, queue);

        int listNum = corePoolSize * 5;
        listNum = fileList.size() > listNum ? listNum : 1;
        List<List<File>> lls = ListUtils.partition(fileList, (fileList.size() + listNum - 1) / listNum);
        CountDownLatch latch = new CountDownLatch(lls.size());
        for (List<File> list : lls) {
            executor.execute(() -> {
                try {
                    int size = list.size();
                    for (int i = 0; i < size; i++) {
                        File file = list.get(i);
                        //保存相对src的目录结构，根目录替改为dest
                        String storeDir = file.getParent().replace(src, dest) + File.separator + FileUtil.mainName(file).trim();
                        if (splitType == FileType.JPG) {
                            splitToPic(file, storeDir, isDelete);
                        } else if (splitType == FileType.PDF) {
                            splitToPdf(file, storeDir);
                        }
                        list.set(i, null);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
    }


    public static void parseAll(String src, FileType fileType) throws InterruptedException {
        setDate();
        String type = fileType.getType();
        List<File> fileList = FileUtil.loopFiles(src, f -> type.contains(FileUtil.getSuffix(f)));

        if (fileList == null || fileList.isEmpty())
            return;


        //对fileList 切分为指定个数组
        List<List<File>> mutiFileList = new ArrayList<>();
        int listNum = 2;
        int count = (fileList.size() + listNum - 1) / listNum;
        Stream.iterate(0, n -> n + 1).limit(listNum).forEach(i -> {
            mutiFileList.add(fileList.stream().skip(i * count).limit(count).collect(Collectors.toList()));
        });


        fileList.clear();
        CountDownLatch latch = new CountDownLatch(mutiFileList.size());

        for (List<File> files : mutiFileList) {
            new Thread(() -> {
                try {
                    int len = files.size();
                    for (int i = 0; i < len; i++) {
//                        parse(files.get(i), orcApis[i & 1]);
                        parse(files.get(i), "http://222.128.127.248:10020/icr/recognize_table_multipage");
                        files.set(i, null);
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
    }

    private static void setDate(){
        date = new Date();
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


        org.jsoup.nodes.Document doc = Ocr.getOcrJSON(actionUrl, file, 2);
        int threshold = 0;
        int count = 0;
        while (doc == null && count < threshold) {
            doc = Ocr.getOcrJSON(actionUrl, file, 0);
        }
        if (doc == null) {
            //打印无法解析文件
            print(file);
            return;
        }
        String content = doc.toString();
        FileUtil.writeUtf8String(content, htmlFile);
        System.out.println("解析完成:\t" + file);
    }


    private static void print(File file){
        String fileName = root + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)+".txt";
        try{
            File errorFile = new File(fileName);
            if(!errorFile.exists()) errorFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(errorFile, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter writer = new BufferedWriter(osw);
            writer.newLine();
            writer.write(file.getPath());
            writer.close();
            osw.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static List<File> splitToPdf(File pdfFile) {
        return splitToPdf(pdfFile, pdfFile.getParent(), true);
    }


    public static List<File> splitToPdf(File pdfFile, String destDir) {
        return splitToPdf(pdfFile, destDir, false);
    }


    /**
     * 将一个pdf按一定的页数切割成多份，返回切割后的文件
     *
     * @param pdfFile 待切割的pdf
     * @param destDir 切割后的文件输出目录
     * @return
     * @throws Exception
     */
    public static List<File> splitToPdf(File pdfFile, String destDir, boolean isDelete) {
        if (destDir.endsWith("."))
            destDir = destDir.replaceAll("(.*)\\.+", "$1");


        if (!FileUtil.exist(destDir))
            FileUtil.mkdir(destDir);

        PdfReader reader = null;

        List<File> splits = new ArrayList<>();
        try {
            reader = new PdfReader(pdfFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        int numberOfPages = reader.getNumberOfPages();
        // PageNumber是从1开始计数的
        int pageNumber = 1;
        while (pageNumber <= numberOfPages) {
            Document doc = new Document();
            String fileName = FileUtil.mainName(pdfFile).replaceAll("(.*)\\.+", "$1") + "." + FileUtil.getSuffix(pdfFile);
            fileName = fileName + "_" + pageNumber + ".pdf";

            PdfCopy pdfCopy = null;
            try {
                File splitFile = new File(destDir, fileName);
                pdfCopy = new PdfCopy(doc, new FileOutputStream(splitFile));
                splits.add(splitFile);

                // 将pdf按页复制到新建的PDF中
                doc.open();
                doc.newPage();
                PdfImportedPage page = pdfCopy.getImportedPage(reader, pageNumber);
                pdfCopy.addPage(page);
            } catch (Exception e) {
                e.printStackTrace();
            }

            pdfCopy.close();
            doc.close();
            pageNumber++;
        }

        reader.close();

        if (isDelete)
            pdfFile.delete();

        return splits;
    }

    /**
     * @param pdfFile 需要被拆分的文件
     * @param destDir 拆分后文件存储位置
     */
    public static void splitToPic(File pdfFile, String destDir) {
        splitToPic(pdfFile, destDir, false);
    }


    /**
     * @param pdfFile  需要被拆分的文件
     * @param destDir  拆分后文件存储位置
     * @param isDelete 是否删除源pdf文件
     */
    public static void splitToPic(File pdfFile, String destDir, boolean isDelete) {
        /**
         * windows创建文件会将文件名后的 "..."省略 ，如 c:test/xxx.. --> c:test/xxx , c:test...pdf  --> c:test.pdf
         * 该方法会将文件名后的...去掉，防止判断文件是否存在时出错
         */
        if (destDir.endsWith(".")) {
            destDir = destDir.replaceAll("(.*)\\.+", "$1");
        }

        if (!FileUtil.exist(destDir))
            FileUtil.mkdir(destDir);

        PDDocument doc = null;
        try {
            doc = PDDocument.load(pdfFile, MemoryUsageSetting.setupTempFileOnly());
            int pageCount = doc.getNumberOfPages();
            PDFRenderer renderer = new PDFRenderer(doc);

            //pdf拆分
            for (int i = 0; i < pageCount; i++) {
                //拆分后的图片名称
                String fileName = FileUtil.mainName(pdfFile).replaceAll("(.*)\\.+", "$1") + "." + FileUtil.getSuffix(pdfFile);
                fileName = fileName + "_" + (i + 1) + ".jpg";
                File pictureFile = new File(destDir, fileName);
                //已经拆分过的不在创建
                if (pictureFile.exists()) continue;

                BufferedImage image = null;
                //第二个参数越大生成图片分辨率越高。
                image = renderer.renderImageWithDPI(i, 300);
                //生成图片
                ImageIO.write(image, "jpg", pictureFile);
            }

            doc.close();
            //删除原pdf文件
            if (isDelete)
                FileUtil.del(pdfFile);
            System.out.println("拆分完成:\t" + pdfFile);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("问题文件：\t" + pdfFile);
        }
    }


    public static void lsAllCast2Pic(String source) throws InterruptedException {
        Pattern p = Pattern.compile("\\.(pdf|PDF)_\\d+\\.pdf");
        List<File> fileList = FileUtil.loopFiles(new File(source), f -> p.matcher(f.getName()).find());
        int corePoolSize = 5;
        int maximumPoolSize = 5;
        long keepAliveTime = 10;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, timeUnit, queue);

        int listNum = corePoolSize * 5;
        listNum = fileList.size() > listNum ? listNum : 1;
        List<List<File>> lls = ListUtils.partition(fileList, (fileList.size() + listNum - 1) / listNum);
        CountDownLatch latch = new CountDownLatch(lls.size());
        for (List<File> list : lls) {
            executor.execute(() -> {
                try {
                    int size = list.size();
                    for (int i = 0; i < size; i++) {
                        File file = list.get(i);
                        //转换图片
                        onePdfCast2Pic(file);
                        list.set(i, null);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();
    }


    public static void onePdfCast2Pic(File pdfFile) {
        if (!FileUtil.exist(pdfFile))
            return;

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
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("问题文件：\t" + pdfFile);
        }
    }


    public static void recoverToPdf(String src) throws DocumentException, IOException {
        Pattern p = Pattern.compile("pdf_\\d+");
        Map<String, List<File>> fileMap = RdFileUtil.lsAllFilesByType(new File(src), f -> p.matcher(f.getName()).find());


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


    public static void mergePdf(List<File> files, String destFile) throws IOException, DocumentException {

    }


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
            imageHeight = imageHeight < 14400F ? imageHeight : 14400F;
            float imageWidth = image.getScaledWidth();
            imageWidth = imageWidth < 14400F ? imageWidth : 14400F;
            document.setPageSize(new Rectangle(imageWidth, imageHeight));

            //新建一页添加图片
            document.newPage();
            document.add(image);
        }
        document.close();
        fos.flush();
        fos.close();
    }


    public static org.jsoup.nodes.Element tableBreaking(org.jsoup.nodes.Element tableElement) {

        try {
            org.jsoup.nodes.Element tmpElement = tableElement;
            if (null != tableElement.getElementsByTag("tbody")) {
                tableElement = tableElement.getElementsByTag("tbody").first();
            }
            Elements trs = tableElement.getElementsByTag("tr");
            int trsCount = trs.size(); // 行数
            int tdsCount = 0; // 列数 需计算
            for (org.jsoup.nodes.Element tr : trs) {
                int tmpTdsCount = 0;
                for (org.jsoup.nodes.Element td : tr.getElementsByTag("td")) {
                    tmpTdsCount += StringUtil.isBlank(td.attr("colspan")) ? 1 : Integer.parseInt(td.attr("colspan"));
                }
                if (tmpTdsCount > tdsCount) {
                    tdsCount = tmpTdsCount;
                }
            }
            int indexY = 0, indexX = 0;
            org.jsoup.nodes.Element[][] tableElements = new org.jsoup.nodes.Element[trsCount][tdsCount];
            for (org.jsoup.nodes.Element tr : trs) {
                indexX = trs.indexOf(tr);
                indexY = 0;
                for (org.jsoup.nodes.Element td : tr.getElementsByTag("td")) {
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
