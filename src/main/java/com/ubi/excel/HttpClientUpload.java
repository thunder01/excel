package com.ubi.excel;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.PictureData;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author fzy
 */
@SuppressWarnings("deprecation")
public class HttpClientUpload {

    /**
     * 无惨私有构造函数
     */
    private HttpClientUpload() {

    }

    public static String httpClientUploadFile(String fileName) throws FileNotFoundException {
        String filePath="E:\\data\\ubiexcel\\file"+fileName;
        System.out.println(filePath);
        File file=new File(filePath);
        InputStream inputStream=new FileInputStream(file);

        /*加载配置文件*/
        Configuration config=null;
        try {
            config=new PropertiesConfiguration("classpath:file/fileupload.properties");
        } catch (ConfigurationException e1) {
            e1.printStackTrace();
        }

    	/*获取文件服务器的地址*/
        final String remote_url = config.getString("remote_url")+fileName;
        System.out.println(remote_url);
        /*创建HttpClient对象*/
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String result = "";
        try {
            /*以post方式请求服务器*/
            HttpPost httpPost = new HttpPost(remote_url);
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();

            /*设置请求编码方式，否则中文会乱码*/
            builder.setCharset(Charset.forName(HTTP.UTF_8));
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);//设置浏览器兼容模式
            builder.addBinaryBody("file",inputStream, ContentType.create("multipart/form-data"),fileName);


            /**类似浏览器表单提交，把文件分类信息传给文件服务器*/
            ContentType contentType= ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8);
            //builder.addTextBody("fileType",fileType, contentType);

            /*设置请求实体*/
            HttpEntity entity = builder.build();
            httpPost.setEntity(entity);

            /*执行提交*/
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                /*将响应内容转换为字符串*/
                result = EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));
                System.out.println(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * 返回文件路径信息
         */
        return result;
    }

    public static void httpGet() throws IOException {
        /*创建HttpClient对象*/
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet=new HttpGet("http://192.168.155.3:8085/user/list/0");
        CloseableHttpResponse execute = httpClient.execute(httpGet);
        HttpEntity entity = execute.getEntity();
        System.out.println(EntityUtils.toString(entity, Charset.forName("UTF-8")));
    }

    public static void main(String[] args) throws IOException {
        httpGet();
    }
}
