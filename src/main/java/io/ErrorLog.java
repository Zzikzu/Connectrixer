package io;

public class ErrorLog {

    private static ErrorLog instance;
    private String errorLogText;
    private final String fileName = "error.log";

    private ErrorLog(){
//		readFile();
    }

    public static ErrorLog getInstance() {
        if(instance == null){
            instance = new ErrorLog();
        }
        return instance;
    }

    private void readFile() {
        setErrorLogText(FileReadWriter.read(fileName));
    }

    void writeToFile(String text, boolean append) {


        FileReadWriter.write(text, fileName, append);
    }

    public void clearLog(){
        writeToFile("", false);
    }


    public String getErrorLogText() {
        readFile();
        return errorLogText;
    }

    private void setErrorLogText(String errorLogText) {
        this.errorLogText = errorLogText;
    }


}
