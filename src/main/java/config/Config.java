package config;

import burp.BurpExtender;

public class Config {
    public static boolean IS_RUNNING = false;
    public static String PROXY_HOST = "localhost";
    public static Integer PROXY_PORT = 8888;
    public static String PROXY_USERNAME = null;
    public static String PROXY_PASSWORD = null;
    public static Integer PROXY_TIMEOUT = 5000;
    public static Integer INTERVAL_TIME = 5000;
    public static String DOMAIN_REGX = "";
    public static String SUFFIX_REGX = "js|css|jpeg|gif|jpg|png|pdf|rar|zip|docx|doc|svg|jpeg|ico|woff|woff2|ttf|otf";

    public static Integer REQUEST_TOTAL = 0;
    public static Integer SUCCESS_TOTAL = 0;
    public static Integer FAIL_TOTAL = 0;

    private static String PYTHON_NAME = "python";
    private static String SQLMAP_PATH = "sqlmap";
    private static String REQUST_FILE_PATH = "";
    private static String SQLMAP_OPTIONS_COMMAND = "";
    private static String OS_TYPE;
    private static boolean IS_INJECT = false;

    public static String getPythonName() {
        try {
            String val = BurpExtender.callbacks.loadExtensionSetting("PYTHON_NAME");
            if(val == null){
                return Config.PYTHON_NAME;
            }else{
                return val;
            }
        }catch(Exception e){
            return Config.PYTHON_NAME;
        }
    }

    public static void setPythonName(String pythonName) {
        BurpExtender.callbacks.saveExtensionSetting("PYTHON_NAME", String.valueOf(pythonName));
        Config.SQLMAP_PATH = pythonName;
    }

    public static String getSqlmapPath() {
        try {
            String val = BurpExtender.callbacks.loadExtensionSetting("SQLMAP_PATH");
            if(val == null){
                return Config.SQLMAP_PATH;
            }else{
                return val;
            }
        }catch(Exception e){
            return Config.SQLMAP_PATH;
        }
    }

    public static void setSqlmapPath(String sqlmapPath) {
        BurpExtender.callbacks.saveExtensionSetting("SQLMAP_PATH", String.valueOf(sqlmapPath));
        Config.SQLMAP_PATH = sqlmapPath;
    }

    public static String getRequstFilePath() {
        return REQUST_FILE_PATH;
    }

    public static void setRequstFilePath(String requstFilePath) {
        REQUST_FILE_PATH = requstFilePath;
    }

    public static String getSqlmapOptionsCommand() {
        try {
            String val = BurpExtender.callbacks.loadExtensionSetting("SQLMAP_OPTIONS_COMMAND");
            if(val == null){
                return Config.SQLMAP_OPTIONS_COMMAND;
            }else{
                return val;
            }
        }catch(Exception e){
            return Config.SQLMAP_OPTIONS_COMMAND;
        }
    }

    public static void setSqlmapOptionsCommand(String sqlmapOptionsCommand) {
        BurpExtender.callbacks.saveExtensionSetting("SQLMAP_OPTIONS_COMMAND", String.valueOf(sqlmapOptionsCommand));
        Config.SQLMAP_OPTIONS_COMMAND = sqlmapOptionsCommand;
    }

    public static String getOsType() {
        return OS_TYPE;
    }

    public static void setOsType(String osType) {
        OS_TYPE = osType;
    }

    public static boolean isIsInject() {
        return IS_INJECT;
    }

    public static void setIsInject(boolean isInject) {
        IS_INJECT = isInject;
    }
}