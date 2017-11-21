package io;

import java.util.Date;

public class ErrorMessage {

    private static ErrorMessage instance;

    private ErrorMessage(){
    }

    public static ErrorMessage getInstance() {
        if(instance == null){
            instance = new ErrorMessage();
        }
        return instance;
    }

    void ioError(String fileName){
        String message = "ERROR: IO error occurred!";
        String file = "File: \"" + fileName + "\"";

        System.out.println();
        System.out.println(message);
        System.out.println(file);
        System.err.println();
        System.err.println("Error date: " + new Date());
        System.err.println(message);
        System.err.println(file);
    }

    void fileNotFound(String fileName){
        String message = "ERROR: File not found!";
        String file = "File: \"" + fileName + "\"";

        System.out.println();
        System.out.println(message);
        System.out.println(file);
        System.err.println();
        System.err.println("Error date: " + new Date());
        System.err.println(message);
        System.err.println(file);
    }

    public void sshIoError(String host){
        String date = "Error date: " + new Date();
        String message = "ERROR: IO error occurred during ssh connection!";
        String hostname = "Hostname: " + host;

        System.out.println();
        System.out.println(message);
        System.out.println(hostname);
        System.err.println();
        System.err.println(date);
        System.err.println(message);
        System.err.println(hostname);
    }

    public void sshSessionError(String user, String host){
        String date = "Error date: " + new Date();
        String message = "ERROR: Session error occurred during ssh connection!";
        String usr = "User: " + user;
        String hostname = "Hostname: " + host;

        System.out.println();
        System.out.println(message);
        System.out.println(user);
        System.out.println(hostname);
        System.err.println();
        System.err.println(date);
        System.err.println(message);
        System.err.println(usr);
        System.err.println(hostname);
    }

    public void sshChanelError(String host, String command){
        String date = "Error date: " + new Date();
        String message = "ERROR: Chanel error occurred during ssh connection!";
        String Hostname = "Hostname: " + host;
        String cmd = " Command: " + command;

        System.out.println();
        System.out.println(message);
        System.out.println(Hostname);
        System.out.println(cmd);
        System.err.println();
        System.err.println(date);
        System.err.println(message);
        System.err.println(Hostname);
        System.err.println(cmd);
    }


    void worksheetIssue(String sheetName){
        String date = "Error date: " + new Date();
        String message = "ERROR: Worksheet issue has occurred!";
        String workbook = "Please check your workbook";
        String worksheet = "Worksheet name: " + sheetName;

        System.out.println();
        System.out.println(message);
        System.out.println(worksheet);
        System.out.println(workbook);
        System.err.println();
        System.err.println(date);
        System.err.println(message);
        System.err.println(worksheet);
    }

    public void workbookIssue(){
        String date = "Error date: " + new Date();
        String message = "ERROR: Workbook issue has occurred!";
        String workbook = "Please check your workbook";
        String errorLog = "Please check your error log";

        System.out.println();
        System.out.println(message);
        System.out.println(workbook);
        System.out.println(errorLog);
        System.err.println();
        System.err.println(date);
        System.err.println(message);
    }

    public void customMeassage(String message){
        String date = "Error date: " + new Date();
        String msg = "ERROR: " + message;

        System.out.println();
        System.out.println(msg);
        System.err.println();
        System.err.println(date);
        System.err.println(msg);
    }

    public void customWarninng(String message){
        String date = "Warning date: " + new Date();
        String msg = "Warning: " + message;

        System.out.println();
        System.out.println(msg);
        System.err.println();
        System.err.println(date);
        System.err.println(msg);
    }

}
