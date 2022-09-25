package demons.抽取;


import demons.common.Executor;
import lombok.Data;
import org.jsoup.helper.StringUtil;

@Data
public class Extractor extends Executor {
    private SingleHandler singleHandler;
    private MultiHandler multiHandler;
    private Context context;

    public Extractor(){
        this.context = new Context(this);
    }


    public void handleSingleFile(){
        files = scanFiles();
        if(!StringUtil.isBlank(excelSource)) readExcel();
        files.stream().forEach(file -> singleHandler.handle(file,context));
        writeExcel();
    }

    public void handleMultiFile(){
        multiFiles = scanDirFiles();
        if(!StringUtil.isBlank(excelSource)) readExcel();
        multiFiles.stream().forEach(files->multiHandler.handle(files,context));
        writeExcel();
    }
}
