package io;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import static io.Paths.*;

public class ExcelWorkbook {

    private static ExcelWorkbook instance;
    private XSSFWorkbook workBook;
    private ExcelWorksheet reserved;
    private boolean workbookLoaded = false;
    private String fileName;



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
//            workBook = new XSSFWorkbook(new FileInputStream(DIR + fileName));
            workBook = new XSSFWorkbook(new FileInputStream(filePath));
            System.out.println();
            System.out.println("File loaded:");
            System.out.println(filePath);
//            System.out.println(DIR + fileName);

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
//            workBook.write(new FileOutputStream(DIR + fileName));
            workBook.write(new FileOutputStream(filePath));
            System.out.println();
            System.out.println("File saved:");
//            System.out.println(fileName);
            System.out.println(filePath);
            System.out.println("To continue please reload your workBook.");

        } catch (IOException e) {
            ErrorMessage.getInstance().ioError(filePath);
            e.printStackTrace();
        }finally {
            workbookLoaded = false;
        }
    }

    public void seveWorkbookAs(File file){
        filePath = file.getPath();
        saveWorkbook();
    }

    public boolean isWorkbookLoaded() {
        return workbookLoaded;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    //===============================================================================================================

    private class ExcelWorksheet extends XSSFSheet{

        private XSSFSheet sheet;
        private int rowCount;
        private XSSFRow headerRow;
        private String sheetName;

        private ExcelWorksheet(String name) {
            sheet = workBook.getSheet(name);
            if(sheet == null){
                sheet = workBook.createSheet(name);
            }
            sheetName = name;
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
