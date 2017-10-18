package core;

import ssh.SshSession;
import ssh.SshSessionFake;
import org.unix4j.Unix4j;
import org.unix4j.unix.grep.GrepOption;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static core.Patterns.*;

class BrocadeSwitch {
    private String ipAddres;
//    private SshSession session;
    private SshSessionFake session;
    private String switchname;
    private String[] portLines;
    private String startIndex;
    private String endIndex;
    private Map<String, String> alishow;

    private Map<String, SwitchPort> portMap;


    BrocadeSwitch(String ipAddres) {
        this.ipAddres = ipAddres;
    }

    void connect() {
//        session = new SshSession(ipAddres);
        session = new SshSessionFake(ipAddres);
    }

    void disconnect() {
        session.close();
    }

    void setInitailData() {
        System.out.println("Setting Switch initial data");

        setSwitchshow();
        System.out.println("switchshow done");

        setSwitchPorts(startIndex, endIndex);
        System.out.println("portshow done");

        setAlishow();
        System.out.println("alishow done");
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

        if (switchshow != null) {
            portLines = Unix4j.fromString(switchshow)
                    .grep("FC")
                    .grep(GrepOption.invertMatch, "Router")
                    .grep(GrepOption.invertMatch, "No_Module")
                    .toStringResult()
                    .split(System.getProperty("line.separator"));

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



//        FileReadWriter.getInstance().write(switchshow, "switchshow.txt");
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
        String[] portshowArray = portshow.split(System.getProperty("line.separator"));


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
                            wwns.append(matcher.group()).append(System.getProperty("line.separator"));
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
                            portFlag = (portFlag.substring(0, portFlag.indexOf(PORT)) + PORT).replace(" ", "");
                        }
                    }

                    portState = patternMap.get(portStatePattern).replaceAll("[^A-Za-z]", "");
                    portWwn = patternMap.get(portWwnPattern).replace(" ", "");

                    portConnectedWNs = new ArrayList<>(Arrays.asList(patternMap.get(portWwnConnectedPattern).split(System.getProperty("line.separator"))));
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

//                            FileReadWriter.getInstance().write(portshow, "portshow.txt");
                }
            }
        }
    }


    SwitchPort getPort(String index){
        return portMap.get(index);
    }


    private void setAlishow(){
        String alishowAll = session.execute(Commands.ALISHOW);
//        FileReadWriter.getInstance().write(alishowAll, "alishow.txt");

        String[] alishow = alishowAll.split(System.getProperty("line.separator"));
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

        String getHealth() {
            return health;
        }

        String getPortFlag() {
            return portFlag;
        }

        String getState() {
            return state;
        }

        String getWwn() {
            return wwn;
        }

        String[] getWwnsConnected() {
            return wwnsConnected;
        }
    }

    private static class Commands {
        static final String SWITCHSHOW = "switchshow";
        static final String SWITCHNAME = "switchname";
        static final String ALISHOW = "alishow *";
        static final String PORTSHOW = "portshow -i";
    }
}
