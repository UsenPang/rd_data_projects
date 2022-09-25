package 博纳斯威.BN20220919发票信息抽取.download;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class OkHttpUtil {
    private static OkHttpClient client;


    static{
        client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30,TimeUnit.SECONDS)
                .writeTimeout(30,TimeUnit.SECONDS)
                .build();
        //设置单个主机的最大并发
        client.dispatcher().setMaxRequestsPerHost(6);
    }


    /**
     * 使用同步的方式下载文件到本地
     * @param url  文件的下载地址
     * @param path  文件保存地址
     * @throws IOException
     */
    public static void download(String url, String path) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Call call = client.newCall(request);
        Response resp = call.execute();
        InputStream is = resp.body().byteStream();
        File file = new File(path);
        save(is,file);
    }


    /**
     * 异步下载文件
     * @param url  文件的下载地址
     * @param path  文件保存地址
     */
    public static void asyncDownload(String url, String path){
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("请求失败:\t"+url);
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                InputStream is = response.body().byteStream();
                File file = new File(path);
                save(is,file);
            }
        });
    }



    /**
     * 将文件读取保存到本地
     * @param is   文件流
     * @param file  保存文件
     * @throws IOException
     */
    private static void save(InputStream is, File file) throws IOException {
        File dir = file.getParentFile();
        if(!dir.exists())
            dir.mkdirs();

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
        byte[] buf = new byte[1024];
        int len = -1;
        while ((len = is.read(buf)) != -1) {
            bos.write(buf, 0, len);
        }
        System.out.println("已下载到本地:\t"+file);
        bos.flush();

        //关闭流
        bos.close();
        is.close();
    }
}
