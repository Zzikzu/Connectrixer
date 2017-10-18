package io;

import java.io.IOException;
import java.io.OutputStream;


public class ErrorOutputStream extends OutputStream {
    private ErrorLog errorLog;

    public ErrorOutputStream(ErrorLog errorLog) {
        this.errorLog = errorLog;
    }

    @Override
    public void write(int b) throws IOException {
        // redirects data to log handling class
        errorLog.writeToFile(String.valueOf((char)b), true);
    }
}