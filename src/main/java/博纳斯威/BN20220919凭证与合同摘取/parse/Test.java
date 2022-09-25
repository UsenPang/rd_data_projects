package 博纳斯威.BN20220919凭证与合同摘取.parse;

import utils.FileType;
import utils.RdPdfUtil;

public class Test {
    public static void main(String[] args) throws InterruptedException {
//        RdPdfUtil2.lsParse("D:\\工作空间\\博纳斯威\\凭证与合同整理_解析");
        RdPdfUtil.parseAll("D:\\工作空间\\博纳斯威\\凭证与合同整理_解析", FileType.IMAGE);
    }
}
