package 天溯计量.pull;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.util.List;

public class MovePdf {
    public static void main(String[] args) {
        String source = "E:\\企业文件下载\\天溯\\结算单附件";
        String dest = "E:\\天溯计量PDF\\天溯\\结算单附件";

        List<File> fileList = FileUtil.loopFiles(source, f -> FileUtil.getSuffix(f).equalsIgnoreCase("pdf"));
//        fileList.stream().forEach(html->{
//            String newPath = html.getPath().replace(source,dest);
//            FileUtil.move(html,new File(newPath),false);
//        });
        System.out.println(fileList.size());
    }
}
