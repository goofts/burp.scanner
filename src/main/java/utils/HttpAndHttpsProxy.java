package utils;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.IRequestInfo;
import java.util.Base64;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.Proxy.Type;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import config.Config;

public class HttpAndHttpsProxy {
    public static Map<String,String> Proxy(IHttpRequestResponse requestResponse) throws InterruptedException{
        byte[] req = requestResponse.getRequest();
        String url = null;
        byte[] reqbody = null;
        List<String> headers = null;

        IHttpService httpService = requestResponse.getHttpService();
        IRequestInfo reqInfo = BurpExtender.helpers.analyzeRequest(httpService,req);

        if(reqInfo.getMethod().equals("POST")){
            int bodyOffset = reqInfo.getBodyOffset(); // 获取 body 偏移量
            String body = null;
            try {
                body = new String(req, bodyOffset, req.length - bodyOffset, "UTF-8");
                reqbody = body.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        headers = reqInfo.getHeaders();
        url = reqInfo.getUrl().toString();
        Thread.sleep(Config.INTERVAL_TIME); // 间隔时间
        if(httpService.getProtocol().equals("https")){
            return HttpsProxy(url, headers, reqbody, Config.PROXY_HOST, Config.PROXY_PORT,Config.PROXY_USERNAME,Config.PROXY_PASSWORD);
        }else {
            return HttpProxy(url, headers, reqbody, Config.PROXY_HOST, Config.PROXY_PORT,Config.PROXY_USERNAME,Config.PROXY_PASSWORD);
        }
    }

    public static Map<String,String> HttpsProxy(String url, List<String> headers,byte[] body, String proxy, int port,String username,String password){
        Map<String,String> mapResult = new HashMap<String,String>();
        String status = "";
        String rspHeader = "";
        String result = "";

        HttpsURLConnection httpsConn = null;
        PrintWriter out = null;
        BufferedReader in = null;

        BufferedReader reader = null;

        try {
            URL urlClient = new URL(url);
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());
            Proxy httpProxy=new Proxy(Type.HTTP, new InetSocketAddress(proxy, port));
            httpsConn = (HttpsURLConnection) urlClient.openConnection(httpProxy);

            setUsernameAndPassword(username, password, httpsConn);

            httpsConn.setSSLSocketFactory(sc.getSocketFactory());
            httpsConn.setHostnameVerifier(new TrustAnyHostnameVerifier());

            for(String header:headers){
                if(header.startsWith("GET")||header.startsWith("POST")||header.startsWith("PUT")){
                    continue;
                }

                String[] h = header.split(":");
                String header_key = h[0].trim();
                String header_value = h[1].trim();
                httpsConn.setRequestProperty(header_key, header_value);
            }
            // 发送POST请求必须设置如下两行
            httpsConn.setDoOutput(true);
            httpsConn.setDoInput(true);

            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(httpsConn.getOutputStream());

            if(body != null) {
                out.print(new String(body));
            }
            out.flush();

            in = new BufferedReader(new InputStreamReader(httpsConn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
                result += "\r\n";
            }
            // 断开连接
            httpsConn.disconnect();
            // 获取响应头
            Map<String, List<String>> mapHeaders = httpsConn.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : mapHeaders.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                String value = "";
                for(String v:values){
                    value += v;
                }

                if(key == null) {
                    String header_line = String.format("%s\r\n",value);
                    rspHeader += header_line;
                }else{
                    String header_line = String.format("%s: %s\r\n", key, value);
                    rspHeader += header_line;
                }
            }

            status = String.valueOf(httpsConn.getResponseCode());
            Utils.updateSuccessCount();
        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
            Utils.updateFailCount();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (out != null) {
                out.close();
            }
        }

        try {
            status = String.valueOf(httpsConn.getResponseCode());
        } catch (IOException e) {
            status = e.getMessage();
        }

        mapResult.put("status",status);
        mapResult.put("header",rspHeader);
        mapResult.put("result",result);

        return mapResult;
    }

    public static Map<String,String> HttpProxy(String url,List<String> headers,byte[] body, String proxy, int port,String username,String password) {
        Map<String,String> mapResult = new HashMap<String,String>();
        String status = "";
        String rspHeader = "";
        String result = "";

        HttpURLConnection httpConn = null;
        PrintWriter out = null;
        BufferedReader in = null;
        BufferedReader reader = null;
        try {
            URL urlClient = new URL(url);
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());
            Proxy httpProxy=new Proxy(Type.HTTP, new InetSocketAddress(proxy, port));
            httpConn = (HttpURLConnection) urlClient.openConnection(httpProxy);

            setUsernameAndPassword(username, password, httpConn);

            for(String header:headers){
                if(header.startsWith("GET") ||
                        header.startsWith("POST") ||
                        header.startsWith("PUT")){
                    continue;
                }
                String[] h = header.split(":");
                String header_key = h[0].trim();
                String header_value = h[1].trim();
                httpConn.setRequestProperty(header_key, header_value);
            }

            // 发送POST请求必须设置如下两行
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);

            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(httpConn.getOutputStream());

            if(body != null) {
                out.print(new String(body));
            }
            out.flush();

            in = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
                result += "\r\n";
            }

            httpConn.disconnect();

            Map<String, List<String>> mapHeaders = httpConn.getHeaderFields();
            for (Map.Entry<String, List<String>> entry : mapHeaders.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                String value = "";
                for(String v:values){
                    value += v;
                }

                if(key == null) {
                    String header_line = String.format("%s\r\n",value);
                    rspHeader += header_line;
                }else{
                    String header_line = String.format("%s: %s\r\n", key, value);
                    rspHeader += header_line;
                }
            }

            status = String.valueOf(httpConn.getResponseCode());
            Utils.updateSuccessCount();
        } catch (Exception e) {
            e.printStackTrace();
            result = e.getMessage();
            Utils.updateFailCount();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (out != null) {
                out.close();
            }
        }

        try {
            status = String.valueOf(httpConn.getResponseCode());
        } catch (IOException e) {
            status = e.getMessage();
        }

        mapResult.put("status",status);
        mapResult.put("header",rspHeader);
        mapResult.put("result",result);

        return mapResult;
    }

    private static void setUsernameAndPassword(String username, String password, HttpURLConnection httpConn) {
        if (username != null && username != "" && password != null && password != "") {
            String user_pass = String.format("%s:%s", username, password);
            String headerKey = "Proxy-Authorization";
            String headerValue = "Basic " + Base64.getEncoder().encodeToString(user_pass.getBytes());
            httpConn.setRequestProperty(headerKey, headerValue);
        }
    }

    private static class TrustAnyTrustManager implements X509TrustManager {
        // 检查客户端证书
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
        // 检查服务器端证书
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
        // 返回受信任的X509证书数组
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        // 检查服务器主机名的合法性
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}