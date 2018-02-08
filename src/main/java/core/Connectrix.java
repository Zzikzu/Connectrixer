package core;

import core.BrocadeSwitch.SwitchPort;
import io.Messages;
import io.ExcelWorkbook;
import io.FileReadWriter;
import io.UserProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static core.Patterns.*;

public class Connectrix {

    private static Connectrix instance;
    private String hostList;
    private Map<String, String> hostListMap;
    private String file = "hostlist.txt";
    private int swCount;
    private boolean connectrixIsRunning;
    private static int swDoneCount;
    private int runningSessions;
    private boolean hostListLoaded;


    private Connectrix() {

    }

    public static Connectrix getInstance() {
        if (instance == null) {
            instance = new Connectrix();
        }
        return instance;
    }

    public void start() {
        if (hostListLoaded) {


            long startTime = System.currentTimeMillis();
            connectrixIsRunning = true;
            int processId = 0;
            runningSessions = 0;
            int waitTime = 30;

            echo("Process started..", true, false);

            int sessionMaxCount = UserProperties.getInstance().getSessionCount();
            swDoneCount = 0;

            for (Object o : hostListMap.entrySet()) {
                Map.Entry pair = (Map.Entry) o;


                while (runningSessions >= sessionMaxCount) {
                    System.out.println("Max session count reached, waiting " + waitTime + " sec");
                    try {
                        Thread.sleep(waitTime * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Trying to start new session");
                }

                processId++;
                runningSessions++;

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
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            do {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tryToEnd(startTime);
            } while (connectrixIsRunning);
        }else {
            Messages.getInstance().customWarninng("Host list not loaded, probably empty file");
            Messages.getInstance().customInfoMessage("Please run: Edit => Host list");
        }
    }

    private void tryToEnd(long startTime){
        if (swDoneCount == swCount){
            long endTime = System.currentTimeMillis();
            echo("Runtime: " + (endTime - startTime)/1000 + " sec", true, false);
            echo("Don't forget to save your workbook.", false, true);
            connectrixIsRunning = false;
        }
    }


    private void writeLineToWorkbook(String switchname, String index, String slot, String port, String wwn, String portname, String alias, String comment){
        String[] outputs ={switchname, index, slot, port, wwn, portname, alias, comment};
        ExcelWorkbook.getInstance().writeLineToReserved(outputs);
    }

    public void readHostList(){
        hostList = FileReadWriter.read(file);
        if (hostList == null){
            Messages.getInstance().customWarninng("First attempt to read " + file + " failed");
            Messages.getInstance().customWarninng("Trying second attempt to read " + file);
            hostList = FileReadWriter.read(file);
        }

        if (hostList != null){
            Messages.getInstance().customInfoMessage("Reading " + file);
            processHostList();
        }
    }


    private void processHostList(){
        String[] hostListLines = hostList.split(System.getProperty("line.separator"));
        Pattern ipPattern = Pattern.compile(IP);
        hostListMap = new HashMap<>();



         for (String line : hostListLines){
             if (line.contains("#")){
                 StringBuilder sb = new StringBuilder();

                 for (char c : line.toCharArray()){
                     if (c == '#'){
                         break;
                     }
                     sb.append(c);
                 }
                 line = sb.toString();
             }

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
             echo("Host list loaded", true, false);
             hostListLoaded = true;
             swCount = hostListMap.size();
         }else {
             Messages.getInstance().customWarninng("Host list not loaded, probably empty file");
             Messages.getInstance().customWarninng("Filename: " + file);
             Messages.getInstance().customInfoMessage("Please run: Edit => Host list");
             hostListLoaded = false;
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

    private class MainProcess {
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
            this.switchIp = switchIp;
            this.swHostname = swHostname;

            System.out.println("Process created: ID " + id + " - " + switchIp + " - " + swHostname);
        }

        private void run() {
            BrocadeSwitch sw = new BrocadeSwitch(switchIp, swHostname);

            if (sw.connect()) {
                sw.setInitailData();

                switchname = sw.getSwitchname();

                String portLines[] = sw.getPortlines();

                Pattern numPattern = Pattern.compile(ZERO_TO_FOUR_FIGURE_NUMBER);

                while (ExcelWorkbook.getInstance().isInFrozenState()) {
                    try {
                        System.out.println(swHostname + ": workbook frozen, waiting");
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (!ExcelWorkbook.getInstance().isInFrozenState()) {
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


                        if (!online) {
                            comment = "Offline";
                            writeLineToWorkbook(switchname, index, slot, port, wwn, portname, alias, comment);
                        }

                        if (online) {
                            if (switchPort.getPortFlag().equals(E_PORT) || switchPort.getPortFlag().equals(EX_PORT)){
                                String portType = null;

                                if (switchPort.getPortFlag().equals(E_PORT)) {
                                    line = line.replace("E-Port", E_PORT);
                                    portType = E_PORT;
                                }

                                if (switchPort.getPortFlag().equals(EX_PORT)) {
                                    line = line.replace("EX-Port", EX_PORT);
                                    portType = EX_PORT;
                                }

                                if (portType != null){
                                    Pattern wwnPattern = Pattern.compile(WWN);
                                    Pattern portPattern = Pattern.compile(portType);
                                    Matcher portMatcher = portPattern.matcher(line);

                                    if (portMatcher.find()) {
                                        line = line.substring(portMatcher.end());

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
                                writeLineToWorkbook(switchname, index, slot, port, wwn, portname, alias, comment);
                            }

                            if (switchPort.getPortFlag().equals(F_PORT)) {
                                if (npiv) {
                                    for (String w : wwns) {
                                        wwn = w;
                                        alias = sw.getAlias(wwn);
                                        writeLineToWorkbook(switchname, index, slot, port, wwn, portname, alias, comment);
                                    }
                                } else {
                                    if (wwns.length == 1) {
                                        wwn = wwns[0];
                                    }
                                    alias = sw.getAlias(wwn);
                                    writeLineToWorkbook(switchname, index, slot, port, wwn, portname, alias, comment);
                                }
                            }
                        }
                    }
                }

                ExcelWorkbook.getInstance().setInFrozenState(false, switchname);
                sw.disconnect();

            } else {
                Messages.getInstance().customErrorMeassage("Unable to run the process for " + swHostname);
                Messages.getInstance().customErrorMeassage("Connection error for: " + switchIp);
            }

            swDoneCount++;
            runningSessions--;

        }
    }
}