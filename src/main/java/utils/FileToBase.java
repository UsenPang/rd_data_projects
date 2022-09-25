package utils;

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 *
 * @author maliang
 * @version V1.0
 * @date 2021/8/30 9:55
 * @Copyright 2021/8/30 www.rongdasoft.com Inc. All rights reserved.
 */

import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;

@Slf4j
public class FileToBase {
    /**
     * 图片转化成base64字符串
     * @param imgPath
     * @return
     */
    public static String getImageStr(String imgPath) {//将图片文件转化为字节数组字符串，并对其进行Base64编码处理
        String imgFile = imgPath;// 待处理的图片
        byte[] data = null;
        String encode = null; // 返回Base64编码过的字节数组字符串
        // 对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        try (InputStream in = new FileInputStream(imgFile)) //与根据File类对象的所代表的实际文件建立链接创建fileInputStream对象
        {
            // 读取图片字节数组
            data = new byte[in.available()];//在读写操作前先得知数据流里有多少个字节可以读取
            in.read(data);
            encode = encoder.encode(data);////编码
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return encode;
    }

    /**
     * base64字符串转化成图片
     *
     * @param imgData     图片编码
     * @param imgFilePath 存放到本地路径
     * @return
     * @throws IOException
     */
    @SuppressWarnings("finally")
    public static boolean generateImage(String imgData, String imgFilePath) { // 对字节数组字符串进行Base64解码并生成图片
        BASE64Decoder decoder = new BASE64Decoder();//解码
        OutputStream out = null;
        try {
            byte[] b = decoder.decodeBuffer(imgData);//解码
            if(b.length < 1){
                return false;
            }
            out = new FileOutputStream(imgFilePath);//文件字节输出流
            // Base64解码
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {// 调整异常数据
                    b[i] += 256;
                }
            }
            out.write(b);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        } finally {
            try {
                if(out != null) {
                    out.flush();//清空缓冲区的数据流
                    out.close();//关闭输出流
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            return true;
        }
    }
}