package 天溯计量.pull;


import utils.FileType;
import utils.RdPdfUtil;

public class Split {
    public static void main(String[] args) throws InterruptedException {
        RdPdfUtil.lsAllSplit("E:\\企业文件下载\\天溯\\确认单附件\\2022","E:\\天溯计量\\天溯\\确认单附件\\2022", FileType.JPG);
    }
}
