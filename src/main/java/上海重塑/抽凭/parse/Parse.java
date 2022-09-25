package 上海重塑.抽凭.parse;


import utils.FileType;
import utils.RdPdfUtil;

public class Parse {
    public static void main(String[] args) throws InterruptedException {
        RdPdfUtil.parseAll("E:\\上海重塑能源", FileType.JPG);
        RdPdfUtil.parseAll("E:\\企业文件下载\\天溯\\结算单附件", FileType.IMAGE);
    }
}
