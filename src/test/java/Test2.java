import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * @program: rd_data_projects
 * @description:
 * @author: 作者
 * @create: 2022-09-26 02:12
 */
public class Test2 {
    public static void main(String[] args) throws IOException {
        String root = "E:\\50G测试数据";
        String dest = "E:\\50G测试数据";
        Files.walkFileTree(new File(root).toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File f = file.toFile();
                FileUtil.move(f,new File(dest+File.separator+f.getName()),false);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                File dirFile = dir.toFile();
                File[] files = dirFile.listFiles();
                if (files == null || files.length <= 0)
                    dirFile.delete();
                return FileVisitResult.CONTINUE;
            }
        });
    }


    public static void move(String src, String dest) {
        List<File> files = FileUtil.loopFiles(src, f -> true);
        for (File file : files) {
            String newPath = file.getPath().replaceFirst(src, dest);
            FileUtil.copy(file, new File(newPath), false);
        }
    }
}
