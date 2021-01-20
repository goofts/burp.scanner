package burp;

import config.Config;
import ui.GUI;
import utils.HttpAndHttpsProxy;
import utils.LogEntry;
import utils.Utils;

import java.awt.Component;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class BurpExtender implements IBurpExtender,ITab,IProxyListener {
    public final static String extensionName = "scanner";
    public final static String version ="1.1.1";
    public static IBurpExtenderCallbacks callbacks;
    public static IExtensionHelpers helpers;
    public static PrintWriter stdout;
    public static PrintWriter stderr;
    public static GUI gui;
    public static final List<LogEntry> log = new ArrayList<LogEntry>();
    public static BurpExtender burpExtender;
    private ExecutorService executorService;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.burpExtender = this;
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        this.stdout = new PrintWriter(callbacks.getStdout(),true);
        this.stderr = new PrintWriter(callbacks.getStderr(),true);

        callbacks.registerContextMenuFactory(new BurpMenu());
        callbacks.setExtensionName(String.format("%s",extensionName));
        stdout.println(getBanner());

        BurpExtender.this.gui = new GUI();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BurpExtender.this.callbacks.addSuiteTab(BurpExtender.this);
                BurpExtender.this.callbacks.registerProxyListener(BurpExtender.this);
            }
        });

        executorService = Executors.newSingleThreadExecutor();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                float[] columnWidthPercentage = {5.0f, 5.0f, 55.0f, 20.0f, 15.0f};
                int tW = GUI.logTable.getWidth();
                TableColumn column;
                TableColumnModel jTableColumnModel = GUI.logTable.getColumnModel();
                int cantCols = jTableColumnModel.getColumnCount();
                for (int i = 0; i < cantCols; i++) {
                    column = jTableColumnModel.getColumn(i);
                    int pWidth = Math.round(columnWidthPercentage[i] * tW);
                    column.setPreferredWidth(pWidth);
                }
            }
        });
    }

    @Override
    public String getTabCaption() {
        return extensionName;
    }

    @Override
    public Component getUiComponent() {
        return gui.getComponet();
    }

    public void processProxyMessage(boolean messageIsRequest, final IInterceptedProxyMessage iInterceptedProxyMessage) {
        if (!messageIsRequest && Config.IS_RUNNING) {
            IHttpRequestResponse reprsp = iInterceptedProxyMessage.getMessageInfo();
            IHttpService httpService = reprsp.getHttpService();
            String host = reprsp.getHttpService().getHost();
            if(!Utils.isMathch(Config.DOMAIN_REGX,host)){
                return ;
            }

            String  url = helpers.analyzeRequest(httpService,reprsp.getRequest()).getUrl().toString();
            url = url.indexOf("?") > 0 ? url.substring(0, url.indexOf("?")) : url;
            if(Utils.isMathch(Config.SUFFIX_REGX,url)){
                return ;
            }

            final IHttpRequestResponse resrsp = iInterceptedProxyMessage.getMessageInfo();

            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    synchronized(log) {
                        int row = log.size();

                        String method = helpers.analyzeRequest(resrsp).getMethod();
                        Map<String, String> mapResult = null;
                        try {
                            mapResult = HttpAndHttpsProxy.Proxy(resrsp);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        log.add(new LogEntry(iInterceptedProxyMessage.getMessageReference(),
                                callbacks.saveBuffersToTempFiles(resrsp),
                                helpers.analyzeRequest(resrsp).getUrl(),
                                method,
                                mapResult)
                        );
                        // 通知所有侦听器，已插入范围在 [row, row] 行
                        GUI.logTable.getHttpLogTableModel().fireTableRowsInserted(row, row);
                    }
                }
            });
        }
    }

    public static String getBanner(){
        String bannerInfo =
                "[+] " + extensionName + " is loaded\n"
                        + "[+]\n"
                        + "[+] ###########################################################\n"
                        + "[+]    " + extensionName + " v" + version +"\n"
                        + "[+]    anthor:   c0ny1\n"
                        + "[+]    email:    root@gv7.me\n"
                        + "[+]    github:   http://github.com/c0ny1/passive-scan-client\n"
                        + "[+]    modifier: goofts\n"
                        + "[+]    date:     2021/1/14\n"
                        + "[+] ###########################################################\n"
                        + "[+] Please enjoy it";
        return bannerInfo;
    }
}