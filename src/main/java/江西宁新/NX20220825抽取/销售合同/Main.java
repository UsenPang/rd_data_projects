package 江西宁新.NX20220825抽取.销售合同;

import cn.hutool.core.io.FileTypeUtil;
import 江西宁新.NX20220825抽取.executor.Executor;


public class Main {
    public static void main(String[] args) {

        String sourcePath = "E:\\江西宁新\\销售合同_只含html";
        String excelOut = "E:\\江西宁新\\宁新抽取\\宁新销售抽取\\销售合同\\合同抽取.xlsx";

        Executor executor = new Executor();
        executor.setSource(sourcePath);
        executor.setExcelPath(excelOut);
        executor.setFilter(f -> "html".equals(FileTypeUtil.getType(f)));
        executor.setHandler(new Handler());
        executor.run();
    }
}
