package burp;

import config.Config;
import ui.SqlmapDlg;
import utils.SqlmapScanner;
import utils.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Menu
 * Implements Ansible Playbook entry by CLI
 * <p>
 * :author:    goofts <goofts@zl.com>
 * :homepage:  https://github.com/goofts
 * :license:   LGPL, see LICENSE for more details.
 * :copyright: Copyright (c) 2019 Goofts. All rights reserved
 */
public class BurpMenu implements IContextMenuFactory {

    @Override
    public List<JMenuItem> createMenuItems(final IContextMenuInvocation invocation) {
        List<JMenuItem> list = new ArrayList<JMenuItem>();

        JMenuItem jMenuItem = new JMenuItem("Send to sqlmap");
        list.add(jMenuItem);
        jMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SqlmapDlg cfd = new SqlmapDlg();
                cfd.show();

                if(Config.isIsInject()) {
                    IHttpRequestResponse[] messages = invocation.getSelectedMessages();
                    byte[] req = messages[0].getRequest();
                    IHttpService httpService = messages[0].getHttpService();
                    String host = httpService.getHost().replace(".", "_");
                    int port = httpService.getPort();

                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                    String data = df.format(new Date());
                    String requstFilename = String.format("%s_%s_%s.req", host, port, data);
                    String reqFilePath = Utils.getTempReqName(requstFilename);
                    Utils.writeFile(req, reqFilePath);
                    new Thread(new SqlmapScanner()).start();
                    Config.setIsInject(false);
                }
            }
        });
        return list;
    }
}