package utils;

import cn.hutool.core.io.FileTypeUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: RdProjects
 * @description:
 * @author: 作者
 * @create: 2022-08-17 10:29
 */
public class RdFileUtil extends RdPathUtil {


    public static void loopDirs(String path, DirVisitor visitor) {
        loopDirs(new File(path), -1, visitor);
    }


    public static void loopDirs(File file, int maxDepth, DirVisitor visitor) {
        loopDirs(file.toPath(), maxDepth, visitor);
    }


    public static Map<String, List<File>> lsAllFilesByType(File source, FileFilter filter) {
        if (source == null)
            return null;

        //map-->  key:文件的父路径   value: 该目录下的所有文件
        Map<String, List<File>> fileMap = new HashMap<>();


        if (source.exists() && !source.isDirectory()) {
            //满足条件的文件加入列表
            if (filter == null || filter.accept(source)) {
                String pKey = source.getParent();
                List<File> fileList = fileMap.get(pKey);
                if (fileList == null) {
                    fileList = new ArrayList<>();
                    fileMap.put(pKey, fileList);
                }
                fileList.add(source);
            }
            return fileMap;
        }


        //采用中序遍历所有文件，找到满足类型的文件加入到集合中
        Queue<File> queue = new LinkedList<>();
        File[] files = source.listFiles();
        if (files != null)
            queue.addAll(Arrays.asList(files));
        while (!queue.isEmpty()) {
            File f = queue.poll();
            if (f.isDirectory()) {
                //子文件加入队列
                queue.addAll(Arrays.asList(f.listFiles()));
                continue;
            }


            //满足条件的文件加入列表
            if (filter == null || filter.accept(f)) {
                String pKey = f.getParent();
                List<File> fileList = fileMap.get(pKey);
                if (fileList == null) {
                    fileList = new ArrayList<>();
                    fileMap.put(pKey, fileList);
                }
                fileList.add(f);
            }
        }
        return fileMap;
    }


    public static Map<String, List<File>> lsAllFilesByType(File source, String type) {

        if (source == null)
            return null;

        //map-->  key:文件的父路径   value: 该目录下的所有文件
        Map<String, List<File>> fileMap = new HashMap<>();


        if (source.exists() && !source.isDirectory()) {
            //满足条件的文件加入列表
            if (type == null || "".equals(type) || type.equals(FileTypeUtil.getType(source))) {
                String pKey = source.getParent();
                List<File> fileList = fileMap.get(pKey);
                if (fileList == null) {
                    fileList = new ArrayList<>();
                    fileMap.put(pKey, fileList);
                }
                fileList.add(source);
            }
            return fileMap;
        }


        //采用中序遍历所有文件，找到满足类型的文件加入到集合中
        Queue<File> queue = new LinkedList<>();
        File[] files = source.listFiles();
        if (files != null)
            queue.addAll(Arrays.asList(files));
        while (!queue.isEmpty()) {
            File f = queue.poll();
            if (f.isDirectory()) {
                //子文件加入队列
                queue.addAll(Arrays.asList(f.listFiles()));
                continue;
            }


            //满足条件的文件加入列表
            if (type == null || "".equals(type) || type.equals(FileTypeUtil.getType(f))) {
                String pKey = f.getParent();
                List<File> fileList = fileMap.get(pKey);
                if (fileList == null) {
                    fileList = new ArrayList<>();
                    fileMap.put(pKey, fileList);
                }
                fileList.add(f);
            }
        }
        return fileMap;
    }


    /**
     * 排序切分后的文件   xxx.pdf_1.jpg.html   xxx.pdf_2.jpg.html  Or  xxx.pdf_1.jpg   xxx.pdf_2.jpg
     *
     * @param files 待排序的文件
     */
    //文件排序
    public static void sort(List<File> files) {
        Collections.sort(files, new Comparator<File>() {
            Pattern p = Pattern.compile("\\.pdf_(\\d+)");

            @Override
            public int compare(File o1, File o2) {
                Matcher m1 = p.matcher(o1.getName());
                Matcher m2 = p.matcher(o2.getName());


                if (m1.find() && m2.find()) {
                    int num1 = 0;
                    int num2 = 0;
                    num1 = Integer.parseInt(m1.group(1));
                    num2 = Integer.parseInt(m2.group(1));
                    return num1 - num2;
                }

                return 0;
            }
        });
    }


    /**
     * 文件如果存在，则获取一个新的名字     xxx.txt  -->xxx(2).txt
     *
     * @param f 文件
     * @return
     */
    public static String getUsefulPath(File f) {
        if (!f.exists())
            return f.getPath();

        String parentPath = f.getParent();
        String fileName = f.getName();
        String suffix = fileName.substring(fileName.indexOf('.'));
        fileName = fileName.substring(0, fileName.indexOf('.'));
        String filePath = null;
        int num = 1;

        while (f.exists()) {
            num++;
            filePath = parentPath + "/" + fileName + "(" + num + ")" + suffix;
            f = new File(filePath);
        }
        return f.getPath();
    }


    /**
     * 以目录分组，列出所有目录下的文件
     * @param root   //根目录
     * @return      返回所有目录下文件对应的list
     * @throws IOException
     */
    public static List<List<File>> lsDirFiles(String root,FileFilter fileFilter) throws IOException {
        List<List<File>> multiFileList = new ArrayList<>();

        //遍历这个root目录下所有的目录，拿出目录对应的文件，存入multiFileList中
        Files.walkFileTree(new File(root).toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                File dirFile = dir.toFile();
                //列出所有文件
                File[] innerFiles = dirFile.listFiles(f -> f.isFile() && fileFilter.accept(f));

                //有文件则加入list中
                if (innerFiles != null && innerFiles.length > 0)
                    multiFileList.add(Arrays.asList(innerFiles));
                return FileVisitResult.CONTINUE;
            }
        });
        return multiFileList;
    }


    /**
     * 去掉文件的后缀   如 aa.txt.jpg  -->   aa.txt
     *
     * @param file
     * @return 去掉后缀的路径
     */
    public static String discardSuffix(File file) {
        return discardSuffix(file.getPath());
    }

    public static String discardSuffix(String filePath) {
        int index1 = filePath.lastIndexOf('.');
        int index2 = filePath.lastIndexOf("/");
        if (index2 == -1)
            index2 = filePath.lastIndexOf("\\");
        if (index1 == -1 || index1 < index2)
            return filePath;
        return filePath.substring(0, index1);
    }
}
