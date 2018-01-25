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
            Messages.getInstance().fileNotFound(fileName);
            e.printStackTrace();

            write("",fileName, false);

        } catch (IOException e) {
            Messages.getInstance().ioError(fileName);
            e.printStackTrace();
        }
        return result;

    }



    public static void write(String input, String fileName, boolean append) {
        if (createEmptyFileIfDontExists(fileName)){
            try (PrintWriter printText = new PrintWriter(new FileWriter(DIR + fileName, append))) {    //append to file

                printText.print(input);    // printLine.printf("%s" + "%n", textLine);
                printText.close();

            }catch (FileNotFoundException e){
                Messages.getInstance().fileNotFound(fileName);
                e.printStackTrace();

            } catch (IOException e) {
                Messages.getInstance().ioError(fileName);
                e.printStackTrace();
            }
        }else {
            Messages.getInstance().customErrorMeassage("Unable to write to or create file: " + fileName);
        }


    }

    public static boolean createEmptyFileIfDontExists(String fileName){

        File file = new File(DIR + fileName);
        if (!file.exists()){
            Messages.getInstance().customWarninng("File " + file + " do not exists");
            Messages.getInstance().customInfoMessage("Creating new " + fileName);
            try {
                return file.createNewFile();
            } catch (IOException e) {
                Messages.getInstance().ioError(file.getName());
                e.printStackTrace();
                return false;
            }
        }else {
            return true;
        }
    }

    static InputStream getInputStream(String fileName) {
        InputStream stream;
        try {
             stream = new FileInputStream(fileName);

        } catch (FileNotFoundException e) {
            stream = null;
            Messages.getInstance().fileNotFound(fileName);
            e.printStackTrace();
        }

        return stream;
    }

    static OutputStream getOutputStream(String fileName){
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(fileName);
        }catch (IOException e){
            Messages.getInstance().ioError(fileName);
            e.printStackTrace();
        }
        return stream;
    }
}