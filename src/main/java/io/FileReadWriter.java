package io;

import java.io.*;

public class FileReadWriter {
    final static private String DIR = "files/";

    public static String read(String fileName) {
        String result = null;
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new  BufferedReader(new FileReader(DIR + fileName))) {
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
                sb.append(System.getProperty("line.separator"));
            }
            result = sb.toString();

        }catch (FileNotFoundException e){
            ErrorMessage.getInstance().fileNotFound(fileName);
            e.printStackTrace();

        } catch (IOException e) {
            ErrorMessage.getInstance().ioError(fileName);
            e.printStackTrace();
        }
        return result;

    }



    public static void write(String input, String fileName, boolean append) {

        try (PrintWriter printText = new PrintWriter(new FileWriter(DIR + fileName, append))) {    //append to file

            printText.print(input);    // printLine.printf("%s" + "%n", textLine);
            printText.close();

        }catch (FileNotFoundException e){
            ErrorMessage.getInstance().fileNotFound(fileName);
            e.printStackTrace();

        } catch (IOException e) {
            ErrorMessage.getInstance().ioError(fileName);
            e.printStackTrace();
        }
    }

    static InputStream getInputStream(String fileName) {
        InputStream stream = null;
        try {
            stream = new FileInputStream(fileName);

        } catch (FileNotFoundException e) {
            ErrorMessage.getInstance().fileNotFound(fileName);
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