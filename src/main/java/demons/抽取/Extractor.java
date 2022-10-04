package demons.抽取;


import demons.common.Executor;
import lombok.Data;
import org.jsoup.helper.StringUtil;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Data
public class Extractor extends Executor {
    private ThreadPoolExecutor threadPoolExecutor;
    private SingleHandler singleHandler;
    private MultiHandler multiHandler;
    private Context context;

    public Extractor() {
        this.context = new Context(this);
    }


    public void handleSingleFile() {
        files = scanFiles();
        if (!StringUtil.isBlank(excelSource)) readExcel();
        files.stream().forEach(file -> singleHandler.handle(file, context));
        writeExcel();
    }

    public void handleMultiFile() {
        multiFiles = scanDirFiles();
        if (!StringUtil.isBlank(excelSource)) readExcel();
        multiFiles.stream().forEach(files -> multiHandler.handle(files, context));
        writeExcel();
    }

    private void getThreadPool() {
        if (threadPoolExecutor == null) {
            int corePoolSize = Runtime.getRuntime().availableProcessors();
            int maximumSize = corePoolSize + 1;
            threadPoolExecutor = new ThreadPoolExecutor(
                    corePoolSize
                    , maximumSize
                    , 300
                    , TimeUnit.MINUTES
                    , new ArrayBlockingQueue<>(2000)
                    , new ThreadPoolExecutor.CallerRunsPolicy());
        }
    }
}
