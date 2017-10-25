package ssh;

import com.jcraft.jsch.*;
import io.ErrorMessage;

import java.io.IOException;
import java.io.InputStream;

public class SshSession {
    private Session session;
    private String host;
    private String user;
    private JSch jsch;
//    private String password;
//    private int port;


    public SshSession(String host) {
        jsch = new JSch();
        this.host = host;
        user = "a_sulak.marek";
        String password = "Pel7aaaaaa";
        int port = 22;
        System.out.println();
        System.out.println("Opening session");
        session = getSession(user, host, port, password);
        if (session.isConnected()){
            System.out.println();
            System.out.println("Session connected for: " + user +"@" + host);
        }
    }


    public void close(){
        session.disconnect();
        if (!session.isConnected()){
            System.out.println("Session closed for: " + user +"@" + host);
        }
    }

    public String execute(String command){
        StringBuilder sb = new StringBuilder();
        try {
            Channel channel = session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
//            channel.setInputStream(null);

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
                        //System.out.println("exit-status: "+channel.getExitStatus());
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
                ErrorMessage.getInstance().sshIoError(host);
                e.printStackTrace();
            }

        }catch (JSchException e){
            ErrorMessage.getInstance().sshChanelError(host, command);
            e.printStackTrace();
        }

        return sb.toString();
    }


    private Session getSession(String user, String host, int port, String password){
        Session session = null;
        try {
            session = jsch.getSession(user, host, port);
            session.setPassword(password);

            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(30000);


        }catch (JSchException e){
            ErrorMessage.getInstance().sshSessionError(user, host);
            e.printStackTrace();
        }
        return session;
    }
}
