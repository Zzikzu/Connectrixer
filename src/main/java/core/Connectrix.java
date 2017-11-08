package core;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.BrocadeSwitch.SwitchPort;
import io.ErrorMessage;
import io.ExcelWorkbook;
import io.FileReadWriter;

import static core.Patterns.*;

public class Connectrix {

    private static Connectrix instance;
    private String hostList;
    private Map<String, String> hostListMap;
    private String file = "hostlist.txt";
    private long startTime;
    private long endTime;
    private int swCount;
    private boolean connectrixIsRunning;
    private static int swDoneCount = 0;


    private Connectrix() {

    }

    public static Connectrix getInstance() {
        if (instance == null) {
            instance = new Connectrix();
        }
        return instance;
    }

    public void start() {
        startTime = System.currentTimeMillis();
        connectrixIsRunning = true;
        int processId = 0;

        echo("Process started..", true, false);

        processHostList();
        swDoneCount = 0;

        for (Object o : hostListMap.entrySet()) {
            Map.Entry pair = (Map.Entry) o;

            processId++;
            final int pid = processId;
            String switchIp = pair.getKey().toString();
            String swHostname = pair.getValue().toString();

            Thread thread = new Thread(() -> {

                MainProcess mainProcess = new MainProcess(pid, switchIp, swHostname);
                mainProcess.run();
            });
            thread.start();

            try {
                Thread.sleep(500);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        do {
            try{
                Thread.sleep(3000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            tryToEnd();
        } while (connectrixIsRunning);
    }

    private void tryToEnd(){
        if (swDoneCount == swCount){
            endTime = System.currentTimeMillis();
            echo("Runtime: " + (endTime - startTime)/1000 + " sec", true, false);
            echo("Don't forget to save your workbook.", false, true);
            connectrixIsRunning = false;
        }
    }


    private void writeLineToWorkbook(int id, String switchname, String index, String slot, String port, String wwn, String portname, String alias, String comment){
        //todo change to excel writeLineToWorkbook
        String[] outputs ={switchname, index, slot, port, wwn, portname, alias, comment};
        ExcelWorkbook.getInstance().writeLineToReserved(outputs);
        System.out.println("Process: " + id +" " + switchname + ": Writing line Slot: " + slot + " Port: " + port);
    }

    public void readHostList(){
        hostList = FileReadWriter.read(file);
    }


    private void processHostList(){
        String[] hostListLines = hostList.split(System.getProperty("line.separator"));
        Pattern ipPattern = Pattern.compile(IP);
        hostListMap = new HashMap<>();

         for (String line : hostListLines){
             Matcher ipMatcher = ipPattern.matcher(line);
             if (ipMatcher.find()){
                 String ip = ipMatcher.group();
                 String hostName = line.substring(ipMatcher.end()).
                         replace("\t", "").
                         replace(" ", "");
                 hostListMap.put(ip, hostName);
             }
         }

         if (!hostListMap.isEmpty()){
             echo("Host list loaded", false, true);
             swCount = hostListMap.size();
         }else {
             ErrorMessage.getInstance().customMeassage("Host list not loaded, probably empty file: " + file);
         }
    }

    private void echo(String message, boolean lineBefore, boolean lineAfter){
        if (lineBefore){
            System.out.println();
        }
        System.out.println(message);
        if (lineAfter){
            System.out.println();
        }
    }

    private class MainProcess{
        private int id;
        private String switchIp;
        private String swHostname;

        private String switchname;
        private String index;
        private String slot;
        private String port;
        private String wwn;
        private String portname;
        private String alias;
        private String comment;


        MainProcess(int id, String switchIp, String swHostname) {
            this.id = id;
            this.switchIp = switchIp;
            this.swHostname = swHostname;

            System.out.println("Process created: ID " + id + " - " + switchIp + " - " + swHostname);
        }

        private void run(){
            BrocadeSwitch sw = new BrocadeSwitch(switchIp, swHostname);
            sw.connect();
            sw.setInitailData();

            switchname = sw.getSwitchname();

            String portLines[] = sw.getPortlines();

            Pattern numPattern = Pattern.compile(ZERO_TO_FOUR_FIGURE_NUMBER);

            while (ExcelWorkbook.getInstance().isInFrozenState()){
                try {
                    System.out.println(swHostname + ": workbook frozen, waiting");
                    Thread.sleep(1000);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

            if (!ExcelWorkbook.getInstance().isInFrozenState()){
                ExcelWorkbook.getInstance().setInFrozenState(true, switchname);
            }

            System.out.println(swHostname + ": processing and writing");

            for (String line : portLines) {

                SwitchPort switchPort;
                Boolean online = false;
                Boolean npiv = false;
                index = "";
                slot = "";
                port = "";
                wwn = "";
                portname = "";
                alias = "";
                comment = "";


                Matcher numMatcher = numPattern.matcher(line);
                if (numMatcher.find()) {
                    index = numMatcher.group(0).replace(" ", "");

                    switchPort = sw.getPort(index);
                    slot = switchPort.getSlot();
                    port = switchPort.getPort();
                    String[] wwns = switchPort.getWwnsConnected();
                    portname = switchPort.getName();

                    if (switchPort.getState().equals("Online")) {
                        online = true;
                    }

                    if (line.contains("NPIV")) {
                        npiv = true;
                    }


                    if (online) {
                        if (switchPort.getPortFlag().equals(E_PORT)) {
                            line = line.replace("E-Port", E_PORT);

                            Pattern eportPattern = Pattern.compile(E_PORT);
                            Pattern wwnPattern = Pattern.compile(WWN);

                            Matcher eportMatcher = eportPattern.matcher(line);
                            if (eportMatcher.find()) {
                                line = line.substring(eportMatcher.end());

                                Matcher wwnMatcher = wwnPattern.matcher(line);
                                if (wwnMatcher.find()) {
                                    wwn = wwnMatcher.group();
                                    comment = line.substring(wwnMatcher.end());
                                } else {
                                    comment = line;
                                }
                                comment = comment
                                        .replace("master", "MASTER")
                                        .replace(" ", "");
                            }
                        }

                        if (switchPort.getPortFlag().equals(F_PORT)) {
                            if (npiv) {
                                for (String w : wwns) {
                                    wwn = w;
                                    alias = sw.getAlias(wwn);
                                    writeLineToWorkbook(id, switchname, index, slot, port, wwn, portname, alias, comment);
                                }
                            } else {
                                if (wwns.length == 1) {
                                    wwn = wwns[0];
                                }
                                alias = sw.getAlias(wwn);
                                writeLineToWorkbook(id, switchname, index, slot, port, wwn, portname, alias, comment);
                            }
                        }
                    }
                    if (!npiv){
                        writeLineToWorkbook(id, switchname, index, slot, port, wwn, portname, alias, comment);
                    }
                }
            }

            ExcelWorkbook.getInstance().setInFrozenState(false, switchname);
            sw.disconnect();
            swDoneCount++;
        }
    }
}