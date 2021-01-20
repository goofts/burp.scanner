package utils;

import burp.IHttpRequestResponsePersisted;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class LogEntry {
    public final int id;
    public final IHttpRequestResponsePersisted requestResponse;
    public final URL url;
    public final String method;
    public final String status;
    public final String proxyResponse;
    public String requestTime;

    public LogEntry(int id, IHttpRequestResponsePersisted requestResponse, URL url, String method, Map<String,String> mapResult) {
        this.id = id;
        this.requestResponse = requestResponse;
        this.url = url;
        this.method = method;
        this.status = mapResult.get("status");
        this.proxyResponse = mapResult.get("header") + "\r\n" + mapResult.get("result");
        this.requestTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    }
}