package 天溯计量.pull;


import utils.FileType;
import utils.RdPdfUtil;

public class Parse {
    public static void main(String[] args) throws InterruptedException {
//        RdPdfUtil2.lsParse("E:\\企业文件下载\\天溯\\结算单附件");
//        RdPdfUtil.parseAll("E:\\企业文件下载\\天溯\\结算单附件",FileType.IMAGE);
        RdPdfUtil.parseAll("E:\\天溯计量\\天溯\\结算单附件", FileType.IMAGE);
    }
}
