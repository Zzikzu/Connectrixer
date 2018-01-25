package io;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ExcelWorkbook {

    private static ExcelWorkbook instance;
    private XSSFWorkbook workBook;
    private ExcelWorksheet worksheet;
    private boolean workbookLoaded = false;
    private boolean inFrozenState;
    private String filePath;

    public static ExcelWorkbook getInstance() {
        if (instance == null){
            instance = new ExcelWorkbook();
        }
        return instance;
    }

    private ExcelWorkbook(){
    }

    public void loadWorkbook() {
        System.out.println();
        System.out.println("..loading, please wait");

        int maxReservedCount = UserProperties.getInstance().getTabCount();

        try {
            workBook = new XSSFWorkbook(new FileInputStream(filePath));
            String reserved = "Reserved";

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String date = dateFormat.format(new Date());

            List<String> sheetNameList = new ArrayList<>();
            sheetNameList.clear();

            for (XSSFSheet sheet : workBook){
                String sheetName = workBook.getSheetName(workBook.getSheetIndex(sheet));
                if (sheetName.contains(reserved)){
                    sheetNameList.add(sheet.getSheetName());
                }
            }

            while (sheetNameList.size() >= maxReservedCount){
                Collections.sort(sheetNameList);
                workBook.removeSheetAt(workBook.getSheetIndex(sheetNameList.get(0)));
                sheetNameList.remove(0);
            }

            sheetNameList.sort(Collections.reverseOrder());

            XSSFSheet sheet = workBook.createSheet(reserved + "_" + date);

            String tabName = null;
            if  (!sheetNameList.isEmpty()){
                tabName = sheetNameList.get(0);
            }

            if (tabName != null){
                System.out.println("New tab: " + tabName);
                workBook.setSheetOrder(sheet.getSheetName(), workBook.getSheetIndex(tabName));
            }

            workBook.setSelectedTab(workBook.getSheetIndex(sheet.getSheetName()));

            worksheet = new ExcelWorksheet(sheet);
            if (worksheet.getWorksheet() != null){
                workbookLoaded = true;
            }else {
                Messages.getInstance().worksheetIssue(worksheet.getSheetName());
            }

            System.out.println();
            System.out.println("File loaded:");
            System.out.println(filePath);


        } catch (IOException e) {
            workbookLoaded = false;
            Messages.getInstance().ioError(filePath);
            e.printStackTrace();
        }
    }

    public void writeLineToReserved(String[] inputs){
        if (worksheet.getWorksheet() != null){
            worksheet.writeLine(inputs);
        }else {
            Messages.getInstance().worksheetIssue(worksheet.getSheetName());
        }
    }


    public void saveWorkbook() {
        if  (worksheet.getWorksheet() != null){
            worksheet.consolidateSheet();
            try {
                workBook.write(new FileOutputStream(filePath));
                System.out.println();
                System.out.println("File saved:");
                System.out.println(filePath);
                System.out.println("To continue please reload your workBook.");
            } catch (FileNotFoundException e){
                Messages.getInstance().fileNotFound(filePath);
                e.printStackTrace();
            } catch (IOException e) {
                Messages.getInstance().ioError(filePath);
                e.printStackTrace();
            }finally {
                workbookLoaded = false;
            }
        }  else {
            Messages.getInstance().worksheetIssue(worksheet.getSheetName());
        }



    }

    public boolean isWorkbookLoaded() {
        return workbookLoaded;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isInFrozenState() {
        return inFrozenState;
    }

    public void setInFrozenState(boolean inFrozenState, String switchName) {
        if (inFrozenState){
            System.out.println(switchName + " is freezing workbook");
        }

        if (!inFrozenState){
            System.out.println(switchName + " is unfreezing workbook");
        }

        this.inFrozenState = inFrozenState;
    }



    //===============================================================================================================

    private class ExcelWorksheet{

        private XSSFSheet sheet;
        private int rowCount;
        private XSSFRow headerRow;

        private ExcelWorksheet(XSSFSheet sheet) {
            this.sheet = sheet;
            rowCount = 0;
            setHeader();
        }

        private void setHeader(){
            String[] cells ={"Switch", "Index", "Slot", "Port", "WWN", "PortName", "Alias", "Comment"};
            writeLine(cells);
            headerRow = sheet.getRow(0);
        }


        private void writeLine(String[] cells){
            sheet.createRow(rowCount);
            for (int i = 0; i < cells.length; i++){
                sheet.getRow(rowCount).createCell(i).setCellValue(cells[i]);
            }
            rowCount++;
        }

        private void autoSizeColumns(){
            if (headerRow != null){
                for(int i = headerRow.getFirstCellNum(); i<= headerRow.getLastCellNum(); i++){
                    sheet.autoSizeColumn(i);
                }
            }
        }

        private void consolidateSheet(){
            autoSizeColumns();
            sheet.setAutoFilter(CellRangeAddress.valueOf("A1:H"+rowCount));
            sheet.createFreezePane(0,1);

            CellStyle style = workBook.createCellStyle();
            style.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setFillPattern(CellStyle.BIG_SPOTS);
            headerRow.setRowStyle(style);

            workBook.setActiveSheet(workBook.getSheetIndex(sheet.getSheetName()));
        }

        XSSFSheet getWorksheet() {
            return sheet;
        }

        String getSheetName(){
            return sheet.getSheetName();
        }
    }
}
