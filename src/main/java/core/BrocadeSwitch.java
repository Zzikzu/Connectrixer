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
    private String switchname;
    private String[] portLines;
    private String startIndex;
    private String endIndex;
    private Map<String, String> alishow;

    private Map<String, SwitchPort> portMap;


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

    String[] getPortlines() {
        return portLines;
    }

    String getAlias(String wwn) {
        String alias = alishow.get(wwn);
        if (alias == null) {
            alias = "";
        }
        return alias;
    }

    private void setSwitchshow() {
        String switchshow = session.execute(Commands.SWITCHSHOW);
        switchname = Unix4j.fromString(switchshow)
                .grep(SWITCHNAME)
                .toStringResult()
                .replace(SWITCHNAME, "")
                .replace("\t", "");

        if (!hostname.toUpperCase().equals(switchname.toUpperCase())){
            echo("WARNING!");
            echo("hostname " + hostname + " do not match switchname " + switchname);
            echo("please check your host list");
            hostname = String.valueOf(switchname);
        }

        if (switchshow != null) {
            portLines = Unix4j.fromString(switchshow)
                    .grep("FC")
                    .grep(GrepOption.invertMatch, "Router")
                    .grep(GrepOption.invertMatch, "No_Module")
                    .grep(GrepOption.invertMatch, "FCIP")
                    .toStringResult()
                    .split("\n");

            ArrayList<Integer> indexes = new ArrayList<>();
            Pattern numPattern = Pattern.compile(ZERO_TO_FOUR_FIGURE_NUMBER);

            for (String line : portLines){
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
        String portshow = session.execute(Commands.PORTSHOW + " " + startIndex + "-" + endIndex + " -f");
        String[] portshowArray = portshow.split("\n");


        Map<Pattern, String> patternMap = new HashMap<>();
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
                        patternMap.put(pattern, line.substring(matcher.end()));
                        i++;
                        j++;
                    }
                } else if (wwnsSearching) {
                    patternMap.put(portWwnConnectedPattern, wwns.toString());
                    wwns.setLength(0);
                    wwnsSearching = false;

                } else {
                    i++;
                }
                if (patterns.size() == patternMap.size()) {
                    portIndex = patternMap.get(portIndexPattern).replace(" ", "");
                    portName = patternMap.get(portNamePattern).replace(" ", "");
                    portHealth = patternMap.get(portHealthPattern).replace(" ", "");

                    portFlag = patternMap.get(portFlagsPattern);

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

                    portState = patternMap.get(portStatePattern).replaceAll("[^A-Za-z]", "");
                    portWwn = patternMap.get(portWwnPattern).replace(" ", "");

                    portConnectedWNs = new ArrayList<>(Arrays.asList(patternMap.get(portWwnConnectedPattern).split("\n")));
                    patternMap.clear();
                    j = 0;

                    boolean match = false;
                    for (String portLine : portLines) {
                        if (match) {
                            break;
                        }

                        Pattern numPattern = Pattern.compile(ZERO_TO_FOUR_FIGURE_NUMBER);
                        Matcher numMatcher = numPattern.matcher(portLine);

                        int numCounter = 0;
                        String index = "";
                        portSlot = "";
                        portPort = "";

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


                    SwitchPort port = new SwitchPort(portIndex, portSlot, portPort, portName, portHealth, portFlag, portState, portWwn, portConnectedWNs.toArray(new String[portConnectedWNs.size()]));
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
        String alishowAll = session.execute(Commands.ALISHOW);

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

    private void echo(String meassage){
        System.out.println(hostname + ": " + meassage);
    }


    static class SwitchPort {


        private String index;
        private String slot;
        private String port;
        private String name;
        private String health;
        private String portFlag;
        private String state;
        private String wwn;
        private String [] wwnsConnected;



        SwitchPort(String index, String slot, String port, String name, String health, String portFlag, String state, String wwn, String[] wwnsConnected) {
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
        static final String SWITCHSHOW = "switchshow";
        static final String ALISHOW = "alishow *";
        static final String PORTSHOW = "portshow -i";
    }
}
