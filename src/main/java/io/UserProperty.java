package io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class UserProperty {

    private static UserProperty instance = null;
    private final File fileName = new File("files/user.properties");
    private Properties prop = new Properties();
    private String login = null;
    private String initials = null;
    private File workDir = null;


    private UserProperty(){
    }

    public static UserProperty getInstance() {
        if(instance == null){
            instance = new UserProperty();
        }

        return instance;

    }


    private void getProperties() {

        try {
            InputStream stream = FileReadWriter.getInputStream(fileName.toString());
            if (stream != null){
                prop.load(stream);
                setLogin(prop.getProperty("login"));
                setInitials(prop.getProperty("initials"));
                String workdir = prop.getProperty("workdir");

                if (workdir != null){
                    setWorkDir(new File(workdir));
                }
                stream.close();

            }else {
                ErrorMessage.getInstance().customMeassage("Error: Issue with " + fileName + "occurred!");
            }

        } catch (IOException e) {
            ErrorMessage.getInstance().ioError(fileName.toString());
            e.printStackTrace();

        }
    }

    private void saveProperties(){
        try {
            OutputStream stream = FileReadWriter.getOutputStream(fileName.toString());

            if (stream != null){
                // set the properties value
                if(login != null){
                    prop.setProperty("login", login);
                }

                if(initials != null){
                    prop.setProperty("initials", initials);
                }

                if(workDir != null){
                    prop.setProperty("workdir", workDir.toString());
                }

                // save properties to project root folder
                prop.store(stream, null);
                stream.close();

                setLogin(login);
                setInitials(initials);
            }else {
                ErrorMessage.getInstance().customMeassage("Error: Issue with " + fileName + "occurred!");
            }

        } catch (IOException e) {
            ErrorMessage.getInstance().ioError(fileName.toString());
            e.printStackTrace();
        }
    }

    public void setCredentials(String login, String initials){
        setLogin(login);
        setInitials(initials);
        saveProperties();
    }


    public String getLogin() {
        if(login == null){
            getProperties();
        }
        return login;
    }

    private void setLogin(String login) {
        this.login = login;
    }

    public String getInitials() {
        if(initials == null){
            getProperties();
        }
        return initials;
    }

    private void setInitials(String initials) {
        this.initials = initials;
    }

    public File getWorkDir() {

        if(workDir == null){
            getProperties();
        }

        return workDir;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
        saveProperties();
    }
}
