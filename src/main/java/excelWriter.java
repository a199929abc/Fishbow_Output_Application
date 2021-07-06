
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.time.LocalDate;
import java.io.*;
import java.util.ArrayList;

public class excelWriter {
    private ArrayList<String> header = new ArrayList<String>();

    private String title;
    private String sheetName;
    private XSSFSheet sheet;
    private XSSFWorkbook workbook;
    private String path;
    private OutputStream out;

    public excelWriter(String path, String title) {
        this.title = title;
        this.path=path;
        File file = new File(path);

        try {
            this.workbook = new XSSFWorkbook();
            this.sheet = workbook.createSheet("sheet1");
        } catch (Exception e) {
            e.fillInStackTrace();
        }

    }

    /**
     * Write single row of the sheet
     * @param writeStrings
     * @param rowNum
     */

    public void write_row(ArrayList writeStrings, int rowNum) {
        XSSFRow row = this.sheet.createRow(rowNum);
        for (int i = 0; i < writeStrings.size(); i++) {
            XSSFCell cell = row.createCell(i);
            cell.setCellValue( writeStrings.get(i).toString());
        }

    }

    /**
     * Call after write row and create excel File
     * @param outPath
     */
    public void create_excel(String outPath) {
        try {
            this.out = new FileOutputStream(outPath);

            this.workbook.write(this.out);
            this.out.flush();

            this.out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Write header to default
     */
    public void write_header(){
        this.header.add("Instrument");
        this.header.add("Serial Number");
        this.header.add("Device Id");
        this.header.add("Inventory Number");
        //this.header.add("ABCCode");
        this.header.add("StandardCost");
        // this.header.add("Len");
        // this.header.add("Height");
        // this.header.add("Width");
        // this.header.add("PartID");

        this.header.add("Quantity");
        //this.header.add("LocationGroupName");
        this.header.add("location");//name
        this.header.add(("location Description"));
        this.header.add("Details");

        XSSFRow row = this.sheet.createRow(0);
        for (int i = 0; i < this.header.size(); i++) {
            XSSFCell cell = row.createCell(i);
            cell.setCellValue(this.header.get(i));
        }
        //combine column E and Column F
        //this.sheet.addMergedRegion(new CellRangeAddress(0,0,4,5));
        //合并两个单元格

    }
}
