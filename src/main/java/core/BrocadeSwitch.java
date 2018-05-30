package core;

import org.unix4j.Unix4j;
import org.unix4j.unix.grep.GrepOption;
import ssh.SshSession;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static core.Patterns.*;

class BrocadeSwitch {
    private String ipAddres;
    private String hostname;
    private SshSession session;
    private String[] logicalSwitches;
    private String switchname;
    private String[] switchshowPortLines;
    private String startIndex;
    private String endIndex;
    private Map<String, String> alishow;
    private Map<String, SwitchPort> portMap;
    private static final String NOT_AVAIALABLE = "N/A";


    BrocadeSwitch(String ipAddres, String hostname) {
        this.ipAddres = ipAddres;
        this.hostname = hostname;
    }

    boolean connect() {
        session = new SshSession(ipAddres, hostname);
        return session.isConnected();
    }

    void disconnect() {
        echo("closing session");
        session.close();

    }


    void setInitailData() {
        echo("setting switch initial data");
            setSwitchName();
            echo("switchname done");

            setLogicalSwitches();
            echo("check for logical switches done");

            setSwitchshow();
            echo("switchshow done");

            setSwitchPorts(startIndex, endIndex);
            echo("portshow done");

            setAlishow();
            echo("alishow done");

            echo("initial data set");
    }


    String getSwitchname() {
        return switchname;
    }

    String[] getSwitchshowPortlines() {
        return switchshowPortLines;
    }

    String getAlias(String wwn) {
        String alias = alishow.get(wwn);
        if (alias == null) {
            alias = "";
        }
        return alias;
    }

    private void setLogicalSwitches(){
        String stringResult = Unix4j.fromString(session.execute(Commands.LSCFGSHOW))
                .grep("FID:")
                .toStringResult()
                .replace("FID:", "")
                .replace("\t", "")
                .replace(" ", "");

        if (stringResult.isEmpty()){
            logicalSwitches = new String[0];
        } else {
            logicalSwitches = stringResult.split("\n");
        }

    }

    private void setSwitchName(){
        switchname = session.execute(Commands.SWITCHNAME).replace("\n", "");

        if (!hostname.toUpperCase().equals(switchname.toUpperCase())){
            echo("WARNING!");
            echo("hostname " + hostname + " do not match switchname " + switchname);
            echo("please check your host list");
            hostname = String.valueOf(switchname);
        }
    }


    private void setSwitchshow() {
        HashMap<String, String> switchshowMap = new HashMap<>();

        if (logicalSwitches.length != 0){
            StringBuilder switchshowBuilder = new StringBuilder();
            for (String logicalSwitch : logicalSwitches){
                switchshowBuilder.append(session.execute(Commands.FOSEXEC
                                                                    + " "
                                                                    + logicalSwitch
                                                                    + " -cmd "
                                                                    + "\""
                                                                    + Commands.SWITCHSHOW
                                                                    + "\""));
                switchshowMap.put(logicalSwitch, switchshowBuilder.toString());
            }

        }else {
            switchshowMap.put("0", session.execute(Commands.SWITCHSHOW));
        }

        if (!switchshowMap.isEmpty()) {
            ArrayList<String> resultList = new ArrayList<>();
            String[] tmpPortLines;

            if (logicalSwitches.length != 0){
                for (String logicalSwitch : logicalSwitches){

                    tmpPortLines = Unix4j.fromString(switchshowMap.get(logicalSwitch))
                            .grep("FC")
                            .grep(GrepOption.invertMatch, "Router")
                            .grep(GrepOption.invertMatch, "No_Module")
                            .grep(GrepOption.invertMatch, "FCIP")
                            .toStringResult()
                            .split("\n");

                    for (String portLine : tmpPortLines){
                        if (!portLine.isEmpty()){   //add FID to valid lines
                            resultList.add(portLine + " " + "FID"+logicalSwitch);
                        }
                    }
                }
                switchshowPortLines = resultList.toArray(new String[0]);


            }else {
                switchshowPortLines = Unix4j.fromString(switchshowMap.get("0"))
                        .grep("FC")
                        .grep(GrepOption.invertMatch, "Router")
                        .grep(GrepOption.invertMatch, "No_Module")
                        .grep(GrepOption.invertMatch, "FCIP")
                        .toStringResult()
                        .split("\n");
            }



            ArrayList<Integer> indexes = new ArrayList<>();
            Pattern numPattern = Pattern.compile(ZERO_TO_FOUR_FIGURE_NUMBER);

            for (String line : switchshowPortLines){
                Matcher numMatcher = numPattern.matcher(line);
                if (numMatcher.find()) {
                    indexes.add(Integer.parseInt(numMatcher.group(0).replace(" ", "")));
                }
            }
            Collections.sort(indexes);

            startIndex = indexes.get(0).toString();
            endIndex = indexes.get(indexes.size() - 1).toString();
        }
    }

    private void setSwitchPorts(String startIndex, String endIndex) {
        String portIndex;
        String portSlot = null;
        String portPort = null;
        String portName;
        String portHealth;
        String portFlag;
        String portState;
        String portWwn;
        String portFid = null;
        ArrayList<String> portConnectedWNs;

        Pattern portIndexPattern = Pattern.compile(PORTINDEX);
        Pattern portNamePattern = Pattern.compile(PORTNAME);
        Pattern portHealthPattern = Pattern.compile(PORTHEALTH);
        Pattern portFlagsPattern = Pattern.compile(PORTFLAGS);
        Pattern portStatePattern = Pattern.compile(PORTSTATE);
        Pattern portWwnPattern = Pattern.compile(PORTWWN);
        Pattern portWwnConnectedPattern = Pattern.compile(PORTWWN_CONNECTED);
        Pattern wwnPattern = Pattern.compile(WWN);

        portMap = new HashMap<>();
        String portshow;

        if (logicalSwitches.length != 0){
            StringBuilder portshowBuilder = new StringBuilder();
            for (String logicalSwitch : logicalSwitches){
                portshowBuilder.append(session.execute(Commands.FOSEXEC
                        + " "
                        + logicalSwitch
                        + " -cmd "
                        + "\""
                        + Commands.PORTSHOW + " " + startIndex + "-" + endIndex + " -f"
                        +"\""));
            }
            portshow = portshowBuilder.toString();
        }else {
            portshow = session.execute(Commands.PORTSHOW + " " + startIndex + "-" + endIndex + " -f");
        }

        String[] portshowArray = portshow.split("\n");


        Map<Pattern, String> portPatternMap = new HashMap<>();
        ArrayList<Pattern> patterns = new ArrayList<>();

        patterns.add(portIndexPattern);
        patterns.add(portNamePattern);
        patterns.add(portHealthPattern);
        patterns.add(portFlagsPattern);
        patterns.add(portStatePattern);
        patterns.add(portWwnPattern);
        patterns.add(portWwnConnectedPattern);


        boolean wwnsSearching = false;
        StringBuilder wwns = new StringBuilder();

        for (int i = 0; i < portshowArray.length;) {
            for (int j = 0; j < patterns.size(); ) {
                if (i >= portshowArray.length) {
                    break;
                }

                String line = portshowArray[i];
                Pattern pattern = patterns.get(j);

                if (wwnsSearching) {
                    pattern = wwnPattern;
                }

                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {

                    //special case need to start search multiple lines in next step
                    if (pattern.equals(portWwnConnectedPattern)) {
                        wwnsSearching = true;
                    }

                    if (wwnsSearching) {
                        if (pattern.equals(wwnPattern)) {
                            wwns.append(matcher.group()).append("\n");
                        }
                        i++;

                    } else {
                        portPatternMap.put(pattern, line.substring(matcher.end()));
                        i++;
                        j++;
                    }
                } else if (wwnsSearching) {
                    portPatternMap.put(portWwnConnectedPattern, wwns.toString());
                    wwns.setLength(0);
                    wwnsSearching = false;

                } else {
                    i++;
                }
                if (patterns.size() == portPatternMap.size()) {
                    portIndex = portPatternMap.get(portIndexPattern).replace(" ", "");
                    portName = portPatternMap.get(portNamePattern).replace(" ", "");
                    portHealth = portPatternMap.get(portHealthPattern).replace(" ", "");
                    portFlag = portPatternMap.get(portFlagsPattern);

                    Pattern activePattern = Pattern.compile(ACTIVE);
                    Pattern portPattern = Pattern.compile(PORT);

                    Matcher activeMatcher = activePattern.matcher(portFlag);
                    if (activeMatcher.find()){
                        portFlag = portFlag.substring(activeMatcher.end());
                        Matcher portMatcher = portPattern.matcher(portFlag);
                        if (portMatcher.find()){


                            Pattern ePortPatern = Pattern.compile(E_PORT);
                            Matcher ePortFlagMatcher = ePortPatern.matcher(portFlag);
                            if (ePortFlagMatcher.find()){
                                portFlag = E_PORT;
                            }

                            Pattern fPortPatern = Pattern.compile(F_PORT);
                            Matcher fPortFlagMatcher = fPortPatern.matcher(portFlag);
                            if (fPortFlagMatcher.find()){
                                portFlag = F_PORT;
                            }

                            Pattern exPortPatern = Pattern.compile(EX_PORT);
                            Matcher exPortFlagMatcher = exPortPatern.matcher(portFlag);
                            if (exPortFlagMatcher.find()){
                                portFlag = EX_PORT;
                            }

                        }
                    }

                    portState = portPatternMap.get(portStatePattern).replaceAll("[^A-Za-z]", "");
                    portWwn = portPatternMap.get(portWwnPattern).replace(" ", "");

                    portConnectedWNs = new ArrayList<>(Arrays.asList(portPatternMap.get(portWwnConnectedPattern).split("\n")));
                    portPatternMap.clear();
                    j = 0;

                    boolean match = false;
                    for (String portLine : switchshowPortLines) {
                        if (match) {
                            break;
                        }

                        Pattern portFidPattern = Pattern.compile(FID);
                        Matcher portFidMatcher = portFidPattern.matcher(portLine);

                        if (portFidMatcher.find()){
//                            System.out.println("portline: " + portLine);
                            portFid = portFidMatcher.group(0).replace("FID", "");
                        }else  {
                            portFid = NOT_AVAIALABLE;
                        }


                        Pattern numPattern = Pattern.compile(ZERO_TO_FOUR_FIGURE_NUMBER);
                        Matcher numMatcher = numPattern.matcher(portLine);

                        int numCounter = 0;
                        String index = "";
                        portSlot = "";
                        portPort = "";

                        //goes for first 3 numeric data in a portline (from switchshow)
                        while (numMatcher.find()) {
                            int max = 1;

                            switch (numCounter) {
                                case 0:
                                    index = numMatcher.group();
                                    break;

                                case 1:
                                    portSlot = numMatcher.group();
                                    if (portSlot.length() == 1) {
                                        portSlot = "0" + portSlot;
                                    }
                                    break;

                                case 2:
                                    portPort = numMatcher.group();
                                    if (portPort.length() == 1) {
                                        portPort = "0" + portPort;
                                    }
                                    break;
                            }
                            numCounter++;

                            if (index.equals(portIndex)) {
                                max = 2;
                                match = true;
                            }

                            if (numCounter > max) {
                                break;
                            }
                        }
                    }

                    if (portSlot == null) {
                        portSlot = "";
                    }

                    if (portPort == null) {
                        portPort = "";
                    }


                    SwitchPort port = new SwitchPort(portFid, portIndex, portSlot, portPort, portName, portHealth, portFlag, portState, portWwn, portConnectedWNs.toArray(new String[0]));
                    portMap.put(port.getIndex(), port);

                    portSlot = null;
                    portPort = null;
                    portConnectedWNs.clear();
                }
            }
        }
    }


    SwitchPort getPort(String index){
        return portMap.get(index);
    }


    private void setAlishow(){
        String alishowAll;

        if (logicalSwitches.length != 0){
            StringBuilder alishowAllBuilder = new StringBuilder();
            for (String logicalSwitch : logicalSwitches){
                alishowAllBuilder.append(session.execute(Commands.FOSEXEC
                        + " "
                        + logicalSwitch
                        + " -cmd "
                        + "\""
                        + Commands.ALISHOW
                        +"\""));
            }
            alishowAll = alishowAllBuilder.toString();

        }else {
            alishowAll = session.execute(Commands.ALISHOW);
        }


        String[] alishow = alishowAll.split("\n");
        this.alishow = new HashMap<>();

        String alias = null;
        String wwn = null;
        for (String line : alishow){
            boolean aliasFound = false;
            boolean wwnFound = false;

            Pattern aliPattern = Pattern.compile("alias:\t");
            Matcher aliMatcher = aliPattern.matcher(line);

            Pattern wwnPattern = Pattern.compile(WWN);
            Matcher wwnMatcher = wwnPattern.matcher(line);

            if (aliMatcher.find()){
                aliasFound = true;
            }

            if (wwnMatcher.find()){
                wwnFound = true;
            }

            if (aliasFound && wwnFound) {
                wwn = wwnMatcher.group();
                alias = line.substring(aliMatcher.end()).replace(wwn, "").replace("\t", "");
            } else {
                if (aliasFound) {
                    alias = line.substring(aliMatcher.end()).replace("\t", "");
                }

                if (wwnFound) {
                    wwn = wwnMatcher.group();
                }
            }

            if (wwn != null && alias != null){
                this.alishow.put(wwn, alias);
                alias = null;
                wwn = null;
            }
        }
    }

    void setPortname(String fid, String portIndex, String name){
        if (!name.isEmpty()){
            if (fid.equals(NOT_AVAIALABLE)){
                session.execute(Commands.PORTNAME + " " + portIndex + " -n " + name);

            }else{
                session.execute(Commands.FOSEXEC
                        + " "
                        + fid
                        + " -cmd "
                        + "\""
                        + Commands.PORTNAME + " " + portIndex + " -n " + name
                        + "\"");
            }
        }
    }


    private void echo(String meassage){
        System.out.println(hostname + ": " + meassage);
    }


    static class SwitchPort {

        private String fid;
        private String index;
        private String slot;
        private String port;
        private String name;
        private String health;
        private String portFlag;
        private String state;
        private String wwn;
        private String [] wwnsConnected;



        SwitchPort(String fid, String index, String slot, String port, String name, String health, String portFlag, String state, String wwn, String[] wwnsConnected) {
            this.fid = fid;
            this.index = index;
            this.slot = slot;
            this.port = port;
            this.name = name;
            this.health = health;
            this.portFlag = portFlag;
            this.state = state;
            this.wwn = wwn;
            this.wwnsConnected = wwnsConnected;
        }

        String getFid(){
            return fid;
        }

        String getIndex() {
            return index;
        }

        String getSlot() {
            return slot;
        }

        String getPort() {
            return port;
        }

        String getName() {
            return name;
        }

        String getPortFlag() {
            return portFlag;
        }

        String getState() {
            return state;
        }

        String[] getWwnsConnected() {
            return wwnsConnected;
        }
    }

    private static class Commands {
        static final String LSCFGSHOW = "lscfg --show -n";
        static final String FOSEXEC = "fosexec --fid";
        static final String SWITCHNAME = "switchname";
        static final String SWITCHSHOW = "switchshow";
        static final String ALISHOW = "alishow *";
        static final String PORTSHOW = "portshow -i";
        static final String PORTNAME = "portname -i";
    }
}
