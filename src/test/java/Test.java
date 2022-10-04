import cn.hutool.core.io.FileUtil;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.FileType;
import utils.RdPdfUtil;
import utils.RdPdfUtil2;
import 天溯计量.TS20220923结算单抽取.Statement;


import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: rd_data_projects
 * @description:
 * @author: 作者
 * @create: 2022-09-25 23:21
 */
public class Test {
    static int cnt = 0;

    public static void main(String[] args) throws IOException {

        Files.walkFileTree(new File("G:\\潘永胜\\天溯计量\\天溯\\委托单附件").toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                cnt++;
                return FileVisitResult.CONTINUE;
            }
        });

        System.out.println(cnt);
    }
}
