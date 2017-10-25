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
    private Map<String, String> hostList;
    private String file = "hostlist.txt";
    private String switchname;
    private String index;
    private String slot;
    private String port;
    private String wwn;
    private String portname;
    private String alias;
    private String comment;



    private Connectrix() {

    }

    public static Connectrix getInstance() {
        if (instance == null) {
            instance = new Connectrix();
        }
        return instance;
    }

    public void start() {
        long startTime;
        long endTime;

        startTime = System.currentTimeMillis();

        System.out.println();
        System.out.println("Process started..");

        readHostList();

        String switchIp = "10.250.76.16";
        BrocadeSwitch sw = new BrocadeSwitch(switchIp);
        sw.connect();
        sw.setInitailData();

        switchname = sw.getSwitchname();

        String portLines[] = sw.getPortlines();

        Pattern numPattern = Pattern.compile(ZERO_TO_FOUR_FIGURE_NUMBER);


        for (String line : portLines){

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


                if (online){
                    if (switchPort.getPortFlag().equals(E_PORT)){
                        line = line.replace("E-Port", E_PORT);

                        Pattern eportPattern = Pattern.compile(E_PORT);
                        Pattern wwnPattern = Pattern.compile(WWN);

                        Matcher eportMatcher = eportPattern.matcher(line);
                        if (eportMatcher.find()){
                            line = line.substring(eportMatcher.end());

                            Matcher wwnMatcher = wwnPattern.matcher(line);
                            if (wwnMatcher.find()){
                                wwn = wwnMatcher.group();
                                comment = line.substring(wwnMatcher.end());
                            }else {
                                comment = line;
                            }
                            comment = comment
                                    .replace("master", "MASTER")
                                    .replace(" ", "");
                        }
                    }

                    if (switchPort.getPortFlag().equals(F_PORT)){
                        if (npiv) {
                            for (String w : wwns) {
                                wwn = w;
                                alias = sw.getAlias(wwn);
                            }
                        }else {
                            if (wwns.length == 1) {
                                wwn = wwns[0];
                            }
                            alias = sw.getAlias(wwn);
                        }
                    }

                }
                output();
            }
        }


//        ExcelWorkbook.getInstance().saveWorkbook();
        sw.disconnect();


        endTime = System.currentTimeMillis();
        System.out.println("Runtime: " + (endTime - startTime)/1000 + " sec");

        System.out.println();
        System.out.println("Don't forget to save your workbook.");

    }

    private void output(){
        //todo change to excel output
        String[] outputs ={switchname, index, slot, port, wwn, portname, alias, comment};
        ExcelWorkbook.getInstance().writeLineToReserved(outputs);

//     System.out.println(switchname + " " + index + " " + slot + " " + port + " " + wwn + " " + portname + " " + alias + " " + comment);
    }

    private void readHostList(){
        String[] hostListLines = FileReadWriter.read(file).split(System.getProperty("line.separator"));
        Pattern ipPattern = Pattern.compile(IP);
        hostList = new HashMap<>();

         for (String line : hostListLines){
             Matcher ipMatcher = ipPattern.matcher(line);
             if (ipMatcher.find()){
                 String ip = ipMatcher.group();
                 String hostName = line.substring(ipMatcher.end()).
                         replace("\t", "").
                         replace(" ", "");
                 hostList.put(ip, hostName);
             }
         }

         if (!hostList.isEmpty()){
             System.out.println();
             System.out.println("Host list loaded");
         }else {
             ErrorMessage.getInstance().customMeassage("Host list not loaded, probably empty file: " + file);
         }
    }

}

