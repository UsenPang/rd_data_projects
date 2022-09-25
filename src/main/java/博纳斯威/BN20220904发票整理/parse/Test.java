package 博纳斯威.BN20220904发票整理.parse;


import utils.FileType;
import utils.RdPdfUtil;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        RdPdfUtil.lsAllSplit("E:\\博纳斯威\\博纳斯威凭证抽取\\博纳斯威\\2020年","E:\\博纳斯威\\伯纳斯威发票分类整理\\博纳斯威\\2020年", FileType.JPG);
        RdPdfUtil.parseAll("E:\\博纳斯威\\伯纳斯威发票分类整理\\博纳斯威\\2020年",FileType.JPG);
    }
}
