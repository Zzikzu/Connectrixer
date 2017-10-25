package core;

class Patterns {
    final static String WWN = "([0-9A-Fa-f]{2}[:]){7}([0-9A-Fa-f]{2})";
    final static String IP = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    final static String WWN_ON_SW = "([[:xdigit:]]{1,2}[\\:]){7}[[:xdigit:]]{1,2}";
    final static String ZERO_TO_FOUR_FIGURE_NUMBER = "[0-9]{1,4}";
    final static String PORTINDEX = "portIndex:";
    final static String PORTNAME = "portName:";
    final static String PORTHEALTH = "portHealth:";
    final static String PORTSTATE = "portState:";
    final static String PORTWWN = "portWwn:";
    final static String PORTWWN_CONNECTED = "connected:";
    final static String SWITCHNAME = "switchName:";
    final static String PORTFLAGS = "portFlags:";
    final static String ACTIVE = "ACTIVE";
    final static String PORT = "_PORT";
    final static String E_PORT = "E_PORT";
    final static String F_PORT = "F_PORT";
    final static String SPACE = "\\s";
}
