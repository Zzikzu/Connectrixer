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
    private ExcelWorksheet reserved;
    private boolean workbookLoaded = false;
    private boolean inFrozenState;
    private int maxReservedCount = 5;
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

        try {
            workBook = new XSSFWorkbook(new FileInputStream(filePath));
            System.out.println();
            System.out.println("File loaded:");
            System.out.println(filePath);

            reserved = new ExcelWorksheet("Reserved");
            workbookLoaded = true;

        } catch (IOException e) {
            workbookLoaded = false;
            ErrorMessage.getInstance().ioError(filePath);
            e.printStackTrace();
        }
    }

    public void writeLineToReserved(String[] inputs){
        if (reserved.getWorksheet() != null){
            reserved.writeLine(inputs);
        }else {
            ErrorMessage.getInstance().worksheetIssue(reserved.getSheetName());
        }
    }


    public void saveWorkbook() {
        reserved.consolidateSheet();

        try {
            workBook.write(new FileOutputStream(filePath));
            System.out.println();
            System.out.println("File saved:");
            System.out.println(filePath);
            System.out.println("To continue please reload your workBook.");
        } catch (FileNotFoundException e){
            ErrorMessage.getInstance().fileNotFound(filePath);
            e.printStackTrace();
        } catch (IOException e) {
            ErrorMessage.getInstance().ioError(filePath);
            e.printStackTrace();
        }finally {
            workbookLoaded = false;
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

    public int getMaxReservedCount() {
        return maxReservedCount;
    }

    public void setMaxReservedCount(int maxReservedCount) {
        this.maxReservedCount = maxReservedCount;
    }


    //===============================================================================================================

    private class ExcelWorksheet extends XSSFSheet{

        private XSSFSheet sheet;
        private int rowCount;
        private XSSFRow headerRow;
        private String sheetName;
        private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        private List<String> sheetNameList;

        private ExcelWorksheet(String name) {
            String date = dateFormat.format(new Date());

            int lastReservedSheetIndex;

            sheetNameList = new ArrayList<>();
            sheetNameList.clear();

            for (XSSFSheet sheet : workBook){
                String sheetName = workBook.getSheetName(workBook.getSheetIndex(sheet));
                if (sheetName.contains(name)){
                    sheetNameList.add(sheet.getSheetName());
                }
            }

            sheet = workBook.createSheet(name + "_" + date);

            if (!sheetNameList.isEmpty()){
                sheetNameList.sort(Collections.reverseOrder());
                lastReservedSheetIndex = workBook.getSheetIndex(sheetNameList.get(0));

                if (sheetNameList.size() >= maxReservedCount){
                    workBook.removeSheetAt(lastReservedSheetIndex);
                }

                workBook.setSheetOrder(sheet.getSheetName(), lastReservedSheetIndex);
            }

            sheetName = sheet.getSheetName();
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

            workBook.setActiveSheet(workBook.getSheetIndex(sheetName));
        }

        XSSFSheet getWorksheet() {
            return sheet;
        }
    }
}
