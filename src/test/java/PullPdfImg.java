import cn.hutool.core.io.FileUtil;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import org.apache.commons.collections4.MultiValuedMap;
import utils.RdFileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

public class PullPdfImg {
    public static void main(String[] args) throws IOException {
        Pattern p = Pattern.compile("\\.pdf_\\d+",Pattern.CASE_INSENSITIVE);
        Random random = new Random();
        String source = "G:\\邓俊\\江西宁新\\江西\\江西";
        String dest = "E:\\50G测试数据";
        List<List<File>> dirFiles = RdFileUtil.lsDirFiles(source, f -> f.getName().endsWith(".jpg") && p.matcher(f.getName()).find());
        SetMultimap<String, File> setMultimap = HashMultimap.create();
        for (List<File> dirFile : dirFiles) {
            int len = dirFile.size();
            int count = 30;
            count = len > count ? count : len / 2 + 1;
            for (int i = 0; i < count; i++) {
                int index = random.nextInt(len);
                File file = dirFile.get(index);
                String dirName = file.getParentFile().getName();
                setMultimap.put(dirName, file);
            }
        }


        for (String dirName : setMultimap.keySet()) {
            Set<File> files = setMultimap.get(dirName);
            for (File file : files) {
                String path = dest + File.separator + dirName + File.separator + file.getName();
                FileUtil.copy(file, new File(path), false);
            }
        }
    }
}
