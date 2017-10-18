package sample;

import io.CustomOutputStream;
import javafx.scene.control.TextArea;
import java.io.*;

class Console {

    private PrintStream printStream;
    private final CustomOutputStream customOutputStream;


    Console(TextArea textArea) {

        customOutputStream = new CustomOutputStream(textArea);
        try {
            this.printStream = new PrintStream(customOutputStream, true, customOutputStream.getCharset().name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    PrintStream getPrintStream() {
        return printStream;
    }

    public void clear() throws IOException {
        this.customOutputStream.clear();
    }

}
