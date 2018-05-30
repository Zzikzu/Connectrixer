package core;

import core.BrocadeSwitch.SwitchPort;
import io.Messages;
import io.ExcelWorkbook;
import io.FileReadWriter;
import io.UserProperties;

import java.util.ArrayList;
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

    private ArrayList<String> succesfulResults;
    private ArrayList<String> unsuccessfulResults;

    private boolean createPortnames;


    private Connectrix() {
        succesfulResults = new ArrayList<>();
        unsuccessfulResults = new ArrayList<>();
    }

    public static Connectrix getInstance() {
        if (instance == null) {
            instance = new Connectrix();
        }
        return instance;
    }

    public void start() {
        if (hostListLoaded) {
            succesfulResults.clear();
            unsuccessfulResults.clear();


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

    //For stop functionality
//    public void end(){
//        connectrixIsRunning = false;
//    }

    private void tryToEnd(long startTime){
        if (swDoneCount == swCount){
            long endTime = System.currentTimeMillis();
            printResult();

            echo("Runtime: " + (endTime - startTime)/1000 + " sec", false, false);

            if (!createPortnames){
                echo("Don't forget to save your workbook.", false, true);
            }
            connectrixIsRunning = false;
        }
    }

    private void printResult(){
        System.out.println("****************************************************************");
        System.out.println("RESULT:");

        if (!succesfulResults.isEmpty()){
            System.out.println();

            System.out.println("Successfully completed:");
            for (String result : succesfulResults){
                System.out.println(result);
            }
        }

        if (!unsuccessfulResults.isEmpty()){
            System.out.println();
            System.out.println("Not completed:");
            for (String result : unsuccessfulResults){
                System.out.println(result);
            }
        }

        System.out.println("****************************************************************");
    }


    private void writeLineToWorkbook(String switchname, String fid, String index, String slot, String port, String wwn, String portname, String alias, String comment){
        String[] outputs ={switchname, fid, index, slot, port, wwn, portname, alias, comment};
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

    public boolean getCreatePortnames(){
        return createPortnames;
    }

    public void setCreatePortnames(boolean set){
        createPortnames = set;

        if (!createPortnames){
            Messages.getInstance().customInfoMessage("Mode set to create excel documentation");
        }

        if (createPortnames){
            Messages.getInstance().customInfoMessage("Mode set to create portnames on SAN switches."+ System.lineSeparator() +"WARNING: Please be careful as this option will overwrite the portnames on selected switches based on actual aliases");

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
        private String fid;




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

                String portLines[] = sw.getSwitchshowPortlines();

                Pattern numPattern = Pattern.compile(ZERO_TO_FOUR_FIGURE_NUMBER);


                if  (!createPortnames){
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
                }

                if  (!createPortnames){
                    System.out.println(swHostname + ": processing and writing");
                } else {
                    System.out.println(swHostname + ": setting portnames");
                }



                    int counter = 0;
                    for (String line : portLines) {
                        counter++;
                        int allLines = portLines.length;

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
                        fid = "";

                        if (counter == allLines / 2){
                            System.out.println(swHostname + ": 50% done");
                        }


                        Matcher numMatcher = numPattern.matcher(line);
                        if (numMatcher.find()) {
                            index = numMatcher.group(0).replace(" ", "");

                            switchPort = sw.getPort(index);

                            fid = switchPort.getFid();
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
                                if (!createPortnames){
                                    comment = "Offline";
                                    writeLineToWorkbook(switchname, fid, index, slot, port, wwn, portname, alias, comment);
                                }
                            }

                            if (online) {
                                //For lines with E_PORT or EX_PORT - switch, router connection
                                if (switchPort.getPortFlag().equals(E_PORT) || switchPort.getPortFlag().equals(EX_PORT)) {
                                    String portType = null;

                                    if (switchPort.getPortFlag().equals(E_PORT)) {
                                        line = line.replace("E-Port", E_PORT);
                                        portType = E_PORT;
                                    }

                                    if (switchPort.getPortFlag().equals(EX_PORT)) {
                                        line = line.replace("EX-Port", EX_PORT);
                                        portType = EX_PORT;
                                    }

                                    if (portType != null) {
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
                                                    .replace(" ", "")
                                                    .replace("FID"+fid, "");
                                        }
                                    }

                                    if (createPortnames){
                                        //will be implemented
//                                        sw.setPortname(fid, index, alias);
                                    }else {
                                        writeLineToWorkbook(switchname, fid, index, slot, port, wwn, portname, alias, comment);
                                    }

                                }

                                //For lines with F_PORT or EX_PORT - host, storage connection
                                if (switchPort.getPortFlag().equals(F_PORT)) {
                                    if (npiv) {
                                        if (createPortnames){
                                            int lastWwn = wwns.length -1;
                                            wwn = wwns[lastWwn];
                                            alias = sw.getAlias(wwn);
                                            sw.setPortname(fid, index, alias);
                                        }else {
                                            for (String w : wwns) {
                                                wwn = w;
                                                alias = sw.getAlias(wwn);
                                                writeLineToWorkbook(switchname, fid, index, slot, port, wwn, portname, alias, comment);
                                            }
                                        }

                                    } else {
                                        if (wwns.length == 1) {
                                            wwn = wwns[0];
                                        }
                                        alias = sw.getAlias(wwn);

                                        if (createPortnames){
                                            sw.setPortname(fid, index, alias);
                                        }else {
                                            writeLineToWorkbook(switchname, fid, index, slot, port, wwn, portname, alias, comment);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if  (!createPortnames){
                        ExcelWorkbook.getInstance().setInFrozenState(false, switchname);
                    }


                sw.disconnect();
                saveResult(swHostname, switchIp, true);

            } else {
                Messages.getInstance().customErrorMeassage("Unable to run the process for " + swHostname);
                Messages.getInstance().customErrorMeassage("Connection error for: " + swHostname + " - " + switchIp);
                saveResult(swHostname, switchIp, false);
            }

            swDoneCount++;
            runningSessions--;

        }

        private void saveResult(String swHostname, String switchIp, boolean successful){
            if (successful){
                succesfulResults.add(swHostname + " " + switchIp);
            }

            if (!successful){
                unsuccessfulResults.add(swHostname + " " + switchIp);
            }
        }
    }
}