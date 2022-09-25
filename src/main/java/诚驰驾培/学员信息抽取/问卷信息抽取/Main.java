package 诚驰驾培.学员信息抽取.问卷信息抽取;


import 诚驰驾培.学员信息抽取.executor2.Executor;

public class Main {
    public static void main(String[] args) {
        String[] heads = {"PDF文件名称","姓名","身份证号码","联系电话","学费"};
        String source = "E:\\DG\\问卷信息抽取";
        String excelPath = "E:\\DG\\问卷信息抽取\\抽取excel\\访谈问卷信息抽取2.xlsx";
        Executor executor = new Executor();
        executor.setSource(source);
        executor.setExcelPath(excelPath);
        Handler handler = new Handler();
        handler.setHeadKey(heads);
        executor.setHandler(handler);
        executor.setFilter(f->f.getName().endsWith(".html"));

        executor.run();

    }
}
