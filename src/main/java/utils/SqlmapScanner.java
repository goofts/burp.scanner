package utils;

import burp.BurpExtender;
import config.Config;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * SqlmapScanner
 * Implements Ansible Playbook entry by CLI
 * <p>
 * :author:    goofts <goofts@zl.com>
 * :homepage:  https://github.com/goofts
 * :license:   LGPL, see LICENSE for more details.
 * :copyright: Copyright (c) 2019 Goofts. All rights reserved
 */
public class SqlmapScanner implements Runnable {
    @Override
    public void run() {
        try {
            String command = String.format("%s \"%s\" -r \"%s\" %s", Config.getPythonName(),Config.getSqlmapPath(),Config.getRequstFilePath(),Config.getSqlmapOptionsCommand());
            List<String> cmds = new ArrayList();
            int osType = Utils.getOSType();
            if(osType == Utils.OS_WIN){
                cmds.add("cmd.exe");
                cmds.add("/c");
                cmds.add("start");
                String batFilePath = Utils.makeBatFile("sqlmap4burp.bat",command);
                if(!batFilePath.equals("Fail")){
                    cmds.add(batFilePath);
                }else{
                    String eMsg = "make sqlmap4burp.bat fail!";
                    JOptionPane.showMessageDialog(null,eMsg,"sqlmap4burp++ alert",JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }else if(osType == Utils.OS_MAC){
                String optionCommand = Config.getSqlmapOptionsCommand();
                //将参数数中的"转译为\"
                optionCommand = optionCommand.replace("\"","\\\"");
                command = String.format("%s \\\"%s\\\" -r \\\"%s\\\" %s",Config.getPythonName(),Config.getSqlmapPath(),Config.getRequstFilePath(),optionCommand);
                cmds.add("osascript");
                cmds.add("-e");
                String cmd = "tell application \"Terminal\" \n" +
                        "        activate\n" +
                        "        do script \"%s\"\n" +
                        "end tell";
                cmds.add(String.format(cmd,command));
                //BurpExtender.stdout.println(String.format(cmd,command));
            }else if(osType == Utils.OS_LINUX){
                cmds.add("/bin/sh");
                cmds.add("-c");
                cmds.add("gnome-terminal");
                Utils.setSysClipboardText(command);
                JOptionPane.showMessageDialog(null,"The command has been copied to the clipboard. Please paste it into Terminal for execution","sqlmap4burp++ alert",JOptionPane.OK_OPTION);
            }else{
                cmds.add("/bin/bash");
                cmds.add("-c");
                cmds.add(command);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(cmds);
            Process process = processBuilder.start();
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            BufferedReader input = new BufferedReader(ir);
            String line;
            while ((line = input.readLine()) != null) {
                BurpExtender.stdout.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        System.getProperties();
    }
}