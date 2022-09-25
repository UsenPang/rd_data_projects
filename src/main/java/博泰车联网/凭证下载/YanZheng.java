package 博泰车联网.凭证下载;

import cn.hutool.core.io.FileUtil;
import utils.RdFileUtil;


import java.io.File;
import java.io.IOException;
import java.util.List;

public class YanZheng {
    public static void main(String[] args) throws IOException {
        List<List<File>> multiFiles = RdFileUtil.lsDirFiles("G:\\伯纳斯威文件下载\\下载文件\\博纳斯威阀门股份有限公司\\财务部\\A001凭证及附件", f -> true);

        for (List<File> multiFile : multiFiles) {
            int htmlCount = 0, jpgCount = 0;
            for (File file : multiFile) {
                if ("html".equals(FileUtil.getSuffix(file)))
                    htmlCount++;
                else if ("jpg".equals(FileUtil.getSuffix(file)))
                    jpgCount++;
            }

            if (htmlCount != jpgCount)
                System.out.println(multiFile.get(0).getParent());
        }
    }
}
