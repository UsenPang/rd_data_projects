package utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * @program: RdProjects
 * @description:
 * @author: 作者
 * @create: 2022-08-11 23:29
 */
@Slf4j
public class Ocr {
    /**
     *
     * @param actionUrl   http://222.128.127.248:10020/icr/recognize_table_multipage
     * @param file
     * @param time    0
     * @return
     */
    public static Document getOcrJSON(String actionUrl, File file, int time) {
        try {
            if (time > 3) {
                return null;
            }
            //生成唯一识别码
            String BOUNDARY = UUID.randomUUID().toString();
            String PREFIX = "--";//后缀
            String LINEND = "\r\n";//换行
            String MULTIPART_FROM_DATA = "multipart/form-data";
            String CHARSET = "UTF-8";


            URL uri = new URL(actionUrl);
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setReadTimeout(10 * 1000);
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false);
            conn.setRequestMethod("POST"); // Post方式
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charset", CHARSET);
            conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
                    + ";boundary=" + BOUNDARY);

            DataOutputStream outStream = new DataOutputStream(
                    conn.getOutputStream());

            StringBuilder sb1 = new StringBuilder();
            sb1.append(PREFIX);
            sb1.append(BOUNDARY);
            sb1.append(LINEND);
            sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + UUID.randomUUID() + "\"" + LINEND);
            sb1.append("Content-Type: application/octet-stream; charset="
                    + CHARSET + LINEND);
            sb1.append(LINEND);
            outStream.write(sb1.toString().getBytes());
            //读取pdf数据
            InputStream is = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }

            is.close();
            outStream.write(LINEND.getBytes());

            // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();




            // 得到响应码
            int res = conn.getResponseCode();
            if (res == 200) {
                InputStream in = conn.getInputStream();
                InputStreamReader isReader = new InputStreamReader(in, "utf-8");
                BufferedReader bufReader = new BufferedReader(isReader);
                String line = "";
                String data = "";
                while ((line = bufReader.readLine()) != null) {
                    data += line;
                }
                outStream.close();
                conn.disconnect();
                return jsonToHtml(data);
//                return data;
            }
            outStream.close();
            conn.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
            time++;
            //请求失败后,休眠3s再尝试
            try {
                Thread.sleep(1000 * 3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getOcrJSON(actionUrl, file, time);
        }
        return null;
    }

    public static Document jsonToHtml(String json) {

        if (json==null || "".equals(json)) {
            return null;
        }
        Document document = Document.createShell("image");
        Element head = document.head();
        head.appendElement("meta").attr("charset", "utf-8");
        Element body = document.getElementsByTag("body").get(0);
        JSONObject jsonObject = JSONObject.parseObject(json);
        if (!"0".equals(jsonObject.getString("error_code"))) {
            return null;
        }
        JSONArray pages = jsonObject.getJSONArray("pages");
        if (pages.size() == 0) {
            return null;
        }
        JSONObject page = pages.getJSONObject(0);
        JSONArray tables = page.getJSONArray("table");
        int idCount = 1;
        for (int i = 0; i < tables.size(); i++) {
            JSONObject table = tables.getJSONObject(i);
            Boolean type = table.getBoolean("type");
            if (type) {
                //表格处理
                Element tableDoc = document.createElement("table");
                tableDoc.attr("border", "1");
                Element tbody = document.createElement("tbody");
                body.appendChild(tableDoc);
                tableDoc.appendChild(tbody);
                for (int j = 0; j < table.getInteger("form_rows"); j++) {
                    Element tr = document.createElement("tr");
                    tr.attr("id", Integer.toString(idCount));
                    idCount++;
                    tbody.appendChild(tr);
                }
                JSONArray cells = table.getJSONArray("form_blocks");
                for (int j = 0; j < cells.size(); j++) {
                    JSONObject cell = cells.getJSONObject(j);
                    Integer start_row = cell.getInteger("start_row") + 1;
                    Integer end_row = cell.getInteger("end_row") + 1;
                    Integer start_column = cell.getInteger("start_column") + 1;
                    Integer end_column = cell.getInteger("end_column") + 1;
                    Elements trs = tbody.getElementsByTag("tr");
                    Element currentTr = trs.get(start_row - 1);
                    Element currentTd = document.createElement("td");
                    currentTd.text(cell.getString("data"));
                    currentTr.appendChild(currentTd);
                    if (end_column > start_column) {
                        int colspan = end_column - start_column + 1;
                        currentTd.attr("colspan", colspan + "");
                    }
                    if (end_row > start_row) {
                        int rowspan = end_row - start_row + 1;
                        currentTd.attr("rowspan", rowspan + "");
                    }
                }
            } else {
                //正文处理
                String[] lines = table.getString("data").split("\n");
                for (String line : lines) {
                    Element p = document.createElement("p");
                    p.attr("id", Integer.toString(idCount));
                    idCount++;
                    p.text(line);
                    body.appendChild(p);
                }
            }
        }
        return document;
    }



    public static Document getOneselfOcrJson(String actionUrl, File file, int time) {
        try {
            if (time > 3) {
                return null;
            }
            String CHARSET = "UTF-8";
            URL uri = new URL(actionUrl);
            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
            conn.setReadTimeout(60 * 1000);
            conn.setConnectTimeout(60 * 1000);
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false);// 忽略缓存
            conn.setRequestMethod("POST"); // Post方式
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charset", CHARSET);
            conn.setRequestProperty("Content-Type", "application/json");

            DataOutputStream outStream = new DataOutputStream(
                    conn.getOutputStream());
            String encodedData = FileToBase.getImageStr(file.getPath());
            JSONObject dataJson = new JSONObject();
            dataJson.put("imgdata", encodedData);
            outStream.writeBytes(dataJson.toString());
            outStream.flush();

            // 得到响应码
            int res = conn.getResponseCode();
            if (res == 200) {
                InputStream in = conn.getInputStream();
                InputStreamReader isReader = new InputStreamReader(in, "utf-8");
                BufferedReader bufReader = new BufferedReader(isReader);
                String line = "";
                String data = "";
                while ((line = bufReader.readLine()) != null) {
                    data += line;
                }
                outStream.close();
                conn.disconnect();
                return jsonToHtml(data);
            }
            outStream.close();
            conn.disconnect();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            time++;
            //请求失败后,休眠30s再尝试
            try {
                Thread.sleep(1000 * 20);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
            return getOneselfOcrJson(actionUrl, file, time);
        }
        return null;
    }
}
