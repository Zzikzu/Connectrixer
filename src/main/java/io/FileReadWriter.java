package io;

import java.io.*;
import static io.Paths.*;

public class FileReadWriter {

    private static FileReadWriter instance;

    private FileReadWriter() {

    }

    public static FileReadWriter getInstance() {
        if (instance == null) {
            instance = new FileReadWriter();
        }
        return instance;
    }

    public static String read(String fileName) {
        StringBuilder result = new StringBuilder();

        try (BufferedReader br = new  BufferedReader(new FileReader(DIR + fileName))) {
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                result.append(inputLine);
                result.append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            ErrorMessage.getInstance().ioError(fileName);
            e.printStackTrace();
        }
        return result.toString();

    }



    static void write(String input, String fileName, boolean append) {

        try (PrintWriter printText = new PrintWriter(new FileWriter(DIR + fileName, append))) {    //append to file

            printText.print(input);    // printLine.printf("%s" + "%n", textLine);
            printText.close();

        } catch (IOException e) {
            ErrorMessage.getInstance().ioError(fileName);
            e.printStackTrace();
        }
    }

    static InputStream getInputStream(String fileName) {
        InputStream stream = null;
        try {
            stream = new FileInputStream(fileName);
        } catch (IOException e) {
            ErrorMessage.getInstance().ioError(fileName);
            e.printStackTrace();
        }
        return stream;
    }


    static OutputStream getOutputStream(String fileName){
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(fileName);
        }catch (IOException e){
            ErrorMessage.getInstance().ioError(fileName);
            e.printStackTrace();
        }
        return stream;
    }
}