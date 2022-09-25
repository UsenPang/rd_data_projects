package 江西宁新.NX20220825抽取.销售合同;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) {
        String source = "E:\\宁新新材切分解析";
        String dest = "E:\\宁新合同过滤后";
        Pattern p = Pattern.compile("(合同|CONTRACT)");


        List<File> fileList = FileUtil.loopFiles(source, f->"html".equals(FileTypeUtil.getType(f)));

        for (File file : fileList) {
            try {
                Document document = Jsoup.parse(file, "utf-8");
                Matcher m = p.matcher(document.text());
                if(m.find()){
                    String newPath = file.getPath().replace(source,dest);
                    FileUtil.copy(file,new File(newPath),true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
