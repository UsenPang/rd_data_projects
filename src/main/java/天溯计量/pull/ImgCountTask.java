package 天溯计量.pull;

import cn.hutool.core.io.FileUtil;
import utils.FileType;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class ImgCountTask extends RecursiveTask<Integer> {
    private File source;
    private String type;

    public ImgCountTask(File source, String type) {
        this.source = source;
        this.type = type;
    }


    public static void main(String[] args) {
        File rootFile = new File("E:\\天溯计量\\天溯\\确认单附件");

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        ImgCountTask task = new ImgCountTask(rootFile, FileType.IMAGE.getType());
        ForkJoinTask<Integer> result = forkJoinPool.submit(task);
        try {
            System.out.println(rootFile + "\n共有图片：" + result.get() + "张");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected Integer compute() {
        int sum = 0;
        File[] files = source.listFiles();
        List<ImgCountTask> tasks = null;
        for (File file : files) {
            if (file.isDirectory()) {
                if (tasks == null)
                    tasks = new ArrayList<>();
                ImgCountTask task = new ImgCountTask(file, type);
                task.fork();
                tasks.add(task);
            } else if (file.isFile() && type.contains(FileUtil.getSuffix(file))) {
                sum++;
            }
        }

        for (int i = 0; tasks != null && i < tasks.size(); i++) {
            sum += tasks.get(i).join();
        }

        return sum;
    }
}
