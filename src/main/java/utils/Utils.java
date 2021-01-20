package utils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import burp.BurpExtender;
import config.Config;
import ui.GUI;

public class Utils {
    public static final int OS_WIN = 1;
    public static final int OS_MAC = 2;
    public static final int OS_LINUX = 3;
    public static final int OS_UNKOWN = 4;

    public static String getOSName(){
        return System.getProperties().getProperty("os.name").toUpperCase();
    }

    public static int getOSType(){
        String OS_NAME = getOSName();
        if(OS_NAME.contains("WINDOW")){
            return OS_WIN;
        }else if(OS_NAME.contains("MAC")){
            return OS_MAC;
        }else if(OS_NAME.contains("LINUX")){
            return OS_LINUX;
        }else {
            return OS_UNKOWN;
        }
    }

    public static void writeFile(byte[] bytes,String filepath){
        try {
            // writePath 为最终文件路径名 如：D://test.txt
            FileOutputStream fos = new FileOutputStream(filepath);
            fos.write(bytes);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getTempReqName(String filename) {
        Properties properties = System.getProperties();
        String tempDir = (String) properties.get("java.io.tmpdir");
        Config.setRequstFilePath(tempDir + File.separator + filename);
        return Config.getRequstFilePath();
    }

    public static String makeBatFile(String filename,String content){
        Properties properties = System.getProperties();
        String tempDir = (String) properties.get("java.io.tmpdir");
        String batFile = (tempDir + File.separator + filename);
        String sysEncoding = System.getProperty("file.encoding");
        try {
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(batFile),sysEncoding);
            BufferedWriter writer=new BufferedWriter(write);
            writer.write(content);
            writer.close();
            return batFile;
        } catch (Exception e) {
            BurpExtender.stderr.println("[*] "+e.getMessage());
            return "Fail";
        }
    }

    public static void setSysClipboardText(String str) {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(str);
        clip.setContents(tText, null);
    }

    public static boolean isMathch(String regx,String str){
        Pattern pat = Pattern.compile("[\\w]+[\\.]("+regx+")",Pattern.CASE_INSENSITIVE); // 正则判断
        Matcher mc= pat.matcher(str); // 条件匹配
        if(mc.find()){
            return true;
        }else{
            return false;
        }
    }

    public static void updateSuccessCount(){
        synchronized(Config.FAIL_TOTAL){
            Config.REQUEST_TOTAL++;
            Config.SUCCESS_TOTAL++;
            GUI.lbRequestCount.setText(String.valueOf(Config.REQUEST_TOTAL));
            GUI.lbSuccesCount.setText(String.valueOf(Config.SUCCESS_TOTAL));
        }
    }

    public static void updateFailCount(){
        synchronized(Config.SUCCESS_TOTAL){
            Config.REQUEST_TOTAL++;
            Config.FAIL_TOTAL++;
            GUI.lbRequestCount.setText(String.valueOf(Config.REQUEST_TOTAL));
            GUI.lbFailCount.setText(String.valueOf(Config.FAIL_TOTAL));
        }
    }
}