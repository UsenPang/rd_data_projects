package utils;



import cn.hutool.core.io.IORuntimeException;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

/**
 * @program: RdProjects
 * @description:
 * @author: 作者
 * @create: 2022-08-17 10:22
 */
public class RdPathUtil {

    /**
     * 递归遍历目录以及子目录中的所有文件<br>
     * 如果提供path为文件，直接返回过滤结果
     *
     * @param path       当前遍历目录
     * @param maxDepth   遍历最大深度，-1表示遍历到没有目录为止
     * @return 文件列表
     * @since 5.4.1
     */
    public static void loopDirs(Path path, int maxDepth, DirVisitor dirVisitor) {

        if (null == path || false == Files.exists(path)) {
            return;
        }

        walkFiles(path, maxDepth, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                File file = dir.toFile();
                if(dirVisitor==null)
                    return FileVisitResult.TERMINATE;

                return dirVisitor.visitor(file,attrs);
            }

        });
    }


    /**
     * 遍历指定path下的文件并做处理
     *
     * @param start    起始路径，必须为目录
     * @param maxDepth 最大遍历深度，-1表示不限制深度
     * @param visitor  {@link FileVisitor} 接口，用于自定义在访问文件时，访问目录前后等节点做的操作
     * @see Files#walkFileTree(Path, java.util.Set, int, FileVisitor)
     * @since 4.6.3
     */
    public static void walkFiles(Path start, int maxDepth, FileVisitor<? super Path> visitor) {
        if (maxDepth < 0) {
            // < 0 表示遍历到最底层
            maxDepth = Integer.MAX_VALUE;
        }

        try {
            Files.walkFileTree(start, EnumSet.noneOf(FileVisitOption.class), maxDepth, visitor);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }


    /**
     * 判断是否为目录，如果file为null，则返回false
     *
     * @param path          {@link Path}
     * @return 如果为目录true
     * @since 3.1.0
     */
    public static boolean isDirectory(Path path) {
        if (null == path) {
            return false;
        }
        final LinkOption[] options = new LinkOption[]{LinkOption.NOFOLLOW_LINKS};
        return Files.isDirectory(path, options);
    }
}
