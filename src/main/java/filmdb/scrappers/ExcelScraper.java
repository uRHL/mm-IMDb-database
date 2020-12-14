package filmdb.scrappers;

import filmdb.entities.Film;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ExcelScraper {
    private final String path;

    public ExcelScraper(String path) {
        this.path = path;
    }

    /**
     * Scraps an excel file containing films' data extracted from IMDb web page
     *
     * @param start          first row of the excel to scrap
     * @param quantumToScrap Quantity of rows to be scrapped
     * @param logStream      {@link FileOutputStream} to print the scrap statistical data
     * @return An {@link ArrayList} containing the {@link Film} objects that could be scrapped from the file
     * @throws Exception Raised when some any of the cells is empty or corrupted
     */
    public ArrayList<Film> scrapExcel(int start, int quantumToScrap, FileOutputStream logStream) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(this.path));
        ArrayList<Film> list = new ArrayList<>();

        //Get the first sheet from the Excel workbook
        XSSFSheet sheet = wb.getSheetAt(0);

        int totalRows = sheet.getLastRowNum();
        int successfulReads = 0;
        for (int i = start; i < totalRows && i < (start + quantumToScrap); ++i) {
            //Get a single row from the excel file
            XSSFRow row = sheet.getRow(i);

            //Reading the data contained in the row
            XSSFCell idCell = row.getCell(0);
            XSSFCell urlCell = row.getCell(1);
            XSSFCell titleCell = row.getCell(2);
            XSSFCell scoreCell = row.getCell(3);
            XSSFCell genreCell = row.getCell(4);
            //XSSFCell posterCell = row.getCell(5);

            try {
            /*
            There are 3 different data types contained in the excel
            Integers, Doubles and Strings
             */
                int id = (int) idCell.getNumericCellValue();
                String filmURL = urlCell.getStringCellValue();
                String title = titleCell.getStringCellValue();
                float avgScore = (float) scoreCell.getNumericCellValue();
                String genres = genreCell.getStringCellValue();

                //If the values are not corrupt then create the filmdb.entities.Film object
                Film film = new Film(id);
                film.setUrl(filmURL);
                film.setTitle(title);
                film.setAvgScore(avgScore);
                //Some films have several genres separated by the char "|"
                film.setGenres(genres.trim().split("[|]"));

                successfulReads++;

                //Finally add the film to the list
                list.add(film);
            } catch (Exception e) {
                System.out.println("ERROR scraping excel at row " + i + " (ref:" + e + ")");
            }
        }

        // Try to write the statistic into the log
        try {
            logStream.write(("-----|EXCEL SCRAPPING STATISTICS|----\r\n").getBytes(StandardCharsets.UTF_8));
            byte[] strToBytes = ("Scrapping rows from " + start + " to " + (start+quantumToScrap) + " [total rows = " + totalRows + "]\r\n").getBytes(StandardCharsets.UTF_8);
            logStream.write(strToBytes);
            strToBytes = ("Successful Excel scraps: " + successfulReads + "/" + quantumToScrap +"\r\n").getBytes(StandardCharsets.UTF_8);
            logStream.write(strToBytes);
            logStream.write(("-------------------------------------\r\n").getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
