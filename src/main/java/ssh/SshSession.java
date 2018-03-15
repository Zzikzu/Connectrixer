package ssh;

import com.jcraft.jsch.*;
import io.Messages;
import io.UserProperties;

import java.io.IOException;
import java.io.InputStream;

public class SshSession {
    private Session session;
    private String ip;
    private String hostname;
    private String user;
    private JSch jsch;
    private boolean connected = false;


    public SshSession(String ip, String hostname) {
        jsch = new JSch();
        this.ip = ip;
        this.hostname = hostname;
        user = UserProperties.getInstance().getLogin();
        String password = UserProperties.getInstance().getPassword();
        if  (UserProperties.getInstance().getO2Selected()){
            password = password + hostname.substring(hostname.length() - 2, hostname.length() - 1).toLowerCase();
        }

        int port = 22;
        echo("Opening session for: " + ip + " - " + hostname);
        session = getSession(user, ip, port, password);
        if (session.isConnected()){
            echo("Session connected for: " + user +"@" + ip);
            connected = true;
        }
    }

    public void close(){
        session.disconnect();
        if (!session.isConnected()){
            echo("Session closed for: " + user +"@" + ip + " - " + hostname);
        }
    }

    public String execute(String command){
        StringBuilder sb = new StringBuilder();

        if (session.isConnected()){
            try {
                Channel channel = session.openChannel("exec");
                ((ChannelExec)channel).setCommand(command);

                try {
                    InputStream in = channel.getInputStream();
                    channel.connect();

                    byte[] tmp=new byte[1024];
                    while(true){
                        while(in.available()>0){
                            int i=in.read(tmp, 0, 1024);
                            if(i<0)break;

                            sb.append(new String(tmp, 0, i));
                        }
                        if(channel.isClosed()){
                            if(in.available()>0) continue;
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ee) {
                            //no action
                        }
                    }
                    channel.disconnect();

                }catch (IOException e){
                    Messages.getInstance().sshIoError(ip);
                    e.printStackTrace();
                }

            }catch (JSchException e){
                Messages.getInstance().sshChanelError(ip, command);
                e.printStackTrace();
            }
        } else  {
            Messages.getInstance().sshChanelError(ip, command);
            Messages.getInstance().customErrorMeassage("Session is not connected");
        }

        return sb.toString();
    }


    private Session getSession(String user, String host, int port, String password){
        Session session = null;
        try {

            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");

            try {
                session.connect(30000);
            }catch (JSchException e){
                Messages.getInstance().customWarninng("Connection to " + host + " failed");
                Messages.getInstance().customWarninng("Trying second attempt");
                session.connect(30000);
            }


        }catch (JSchException e){
            Messages.getInstance().sshSessionError(user, host);
            e.printStackTrace();
        }
        return session;
    }

    private void echo(String message){
        System.out.println("****************************************************************");
        System.out.println(message);
        System.out.println("****************************************************************");
    }

    public boolean isConnected() {
        return connected;
    }
}
