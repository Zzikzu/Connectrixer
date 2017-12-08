package io;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.io.*;
import java.util.Properties;

public class UserProperties {

    private static UserProperties instance = null;
    private String encryptedPassword = null;
    private String passwordPropertyKey = "password";
    private final File fileName = new File("files/user.properties");
    private Properties prop = new Properties();
    private String login = null;
    private File workDir = null;
    private Integer sessionCount = null;
    private Integer tabCount = null;


    private UserProperties(){
    }

    public static UserProperties getInstance() {
        if(instance == null){
            instance = new UserProperties();
        }

        return instance;

    }

    public void initialize(){
        getProperties();

        if (!credentialsSet()){
            System.out.println();
            System.out.println("Login credentials not set.");
            System.out.println("Please run: Edit => User settings");
        }
    }

    private void getProperties() {

        try {
            InputStream stream = FileReadWriter.getInputStream(fileName.toString());

            if (stream == null){
                System.out.println("Creating new property file: " + fileName);
                saveProperties();

                setSessionCount(1);
                setTabCount(1);
            }

            if (stream != null) {
                prop.load(stream);
                setLogin(prop.getProperty("login"));

                String workdir = prop.getProperty("workdir");

                if (workdir != null) {
                    setWorkDir(new File(workdir));
                }

                String sessionCount = prop.getProperty("sessionCount");
                if (sessionCount != null){
                    this.sessionCount = Integer.parseInt(sessionCount);
                }else {
                    this.sessionCount = 0;
                }

                String tabCount = prop.getProperty("tabCount");
                if (tabCount != null){
                    this.tabCount = Integer.parseInt(tabCount);
                }else {
                    this.tabCount = 0;
                }

                stream.close();
            } else {
                ErrorMessage.getInstance().customMeassage("Error: Issue with " + fileName + "occurred!");
                saveProperties();
            }

        } catch (FileNotFoundException ex){
            ErrorMessage.getInstance().fileNotFound(fileName.toString());
            ex.printStackTrace();

        } catch (IOException e) {

            ErrorMessage.getInstance().ioError(fileName.toString());
            e.printStackTrace();

        }
    }

    private void saveProperties(){
        try {
            OutputStream stream = FileReadWriter.getOutputStream(fileName.toString());

            if (stream != null){
                if(login != null){
                    prop.setProperty("login", login);
                }

                if(workDir != null){
                    prop.setProperty("workdir", workDir.toString());
                }

                if (sessionCount != null){
                    prop.setProperty("sessionCount", sessionCount.toString());
                }

                if (tabCount != null){
                    prop.setProperty("tabCount", tabCount.toString());
                }

                if (encryptedPassword != null){
                    prop.setProperty("password", encryptedPassword);
                }

                // save properties to project root folder
                prop.store(stream, null);
                stream.close();

//                setLogin(login);
            }else {
                ErrorMessage.getInstance().customMeassage("Error: Issue with " + fileName + "occurred!");
            }

        } catch (IOException e) {
            ErrorMessage.getInstance().ioError(fileName.toString());
            e.printStackTrace();
        }
    }


    private void encryptPassword(String password){

        PropertiesConfiguration config = null;
        try {
            config = new PropertiesConfiguration(fileName);
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            e.printStackTrace();
        }

        if (config != null){
            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword("jasypt");

            encryptedPassword = encryptor.encrypt(password);
            config.setProperty(passwordPropertyKey, encryptedPassword);

            try {
                config.save();
            } catch (org.apache.commons.configuration.ConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    private String decriptPassword(){
        PropertiesConfiguration config = null;
        String result = "";

        try {
            config = new PropertiesConfiguration(fileName);
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            e.printStackTrace();
        }

        if (config != null){
            if (encryptedPassword == null){
                encryptedPassword = config.getString(passwordPropertyKey);
            }

            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword("jasypt");
            result = encryptor.decrypt(encryptedPassword);
        }
        return result;
    }

    public boolean credentialsSet(){
        getLogin();
        getPassword();
        return login != null && encryptedPassword != null;
    }

    public void setCredentials(String login, String password){

        if (login != null && !login.isEmpty()){
            setLogin(login);
        }

        if (password != null && !password.isEmpty()){
            setPassword(password);
        }

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

    public int getSessionCount() {
        if (sessionCount == null){
            getProperties();
        }
        return sessionCount;
    }

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
        saveProperties();
    }

    public int getTabCount() {
        if (tabCount == null){
            getProperties();
        }
        return tabCount;
    }

    public void setTabCount(int tabCount) {
        this.tabCount = tabCount;
        saveProperties();
    }

    public String getPassword() {
        return decriptPassword();
    }

    private void setPassword(String password) {
        encryptPassword(password);
    }
}
