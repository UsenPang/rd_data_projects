package 上海重塑.抽凭.parse;


import utils.FileType;
import utils.RdPdfUtil;


public class Split {
    public static void main(String[] args) throws InterruptedException {
//        RdPdfUtil.lsAllSplit("E:\\上海重塑能源", FileType.JPG);
//        RdPdfUtil.lsAllSplit("C:\\Users\\荣大\\Desktop\\2022-09-14 20_08_09-回函附件", FileType.JPG);
        RdPdfUtil.parseAll("C:\\Users\\荣大\\Desktop\\2022-09-14 20_08_09-回函附件", FileType.JPG);
    }
}
