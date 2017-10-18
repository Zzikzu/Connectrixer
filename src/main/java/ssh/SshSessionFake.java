package ssh;

import io.FileReadWriter;

public class SshSessionFake {
    private String host;
    private String user;


    public SshSessionFake(String host) {
        this.host = host;
        user = "a_sulak.marek";
        String password = "Pel7aaaaaa";
        int port = 22;
        System.out.println("Opening session");
        System.out.println("Session connected for: " + user + "@" + host);
    }


    public void close(){
            System.out.println("Session closed for: " + user +"@" + host);
    }

    public String execute(String command){
        String result = "";

        if (command.equals("switchshow")){
            result = FileReadWriter.getInstance().read("switchshow.txt");
        }

        if (command.equals("alishow *")){
            result = FileReadWriter.getInstance().read("alishow.txt");
        }

        if (command.contains("portshow -i")){
            result = FileReadWriter.getInstance().read("portshow.txt");
        }

        return result;
    }
}
