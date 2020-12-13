import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ExcelScraper {
    /**
     * Scraps an excel file containing films' data extracted from IMDb web page
     *
     * @param excelFile route to the excel file
     * @return An {@link ArrayList} containing the {@link Film} objects that could be scrapped from the file
     * @throws Exception Raised when some any of the cells is empty or corrupted
     */
    public ArrayList<Film> scrapExcel(String excelFile) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(excelFile));
        ArrayList<Film> list = new ArrayList<>();

        //Get the first sheet from the Excel workbook
        XSSFSheet sheet = wb.getSheetAt(0);

        int totalRows = sheet.getLastRowNum();
        int successfulReads = 0;
        for (int i = 1; i < totalRows; ++i) {
            //Get a single row from the excel file
            XSSFRow row = sheet.getRow(i);

            //Reading the data contained in the row
            XSSFCell idCell = row.getCell(0);
            XSSFCell urlCell = row.getCell(1);
            XSSFCell titleCell = row.getCell(2);
            XSSFCell scoreCell = row.getCell(3);
            XSSFCell genreCell = row.getCell(4);
            XSSFCell posterCell = row.getCell(5);

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
                String posterURL = posterCell.getStringCellValue();

                //If the any cell was corrupt then create the Film object
                Film film = new Film();
                film.setImdbID(id);
                film.setFilmURL(filmURL);
                film.setTitle(title);
                film.setAvgScore(avgScore);
                //Some films have several genres separated by the char "|"
                film.setGenres(genres.trim().split("[|]"));
                film.setPosterURL(posterURL);
                successfulReads++;

                //Finally add the film to the list
                list.add(film);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Try to write the statistic into the log
        try {
            FileOutputStream outputStream = new FileOutputStream(ScrappingApp.SCRAP_LOG, true);
            outputStream.write(("-----|EXCEL SCRAPPING STATISTICS|----\r\n").getBytes(StandardCharsets.UTF_8));
            byte[] strToBytes = ("Successful Excel scraps: " + successfulReads + "/" + totalRows + "\r\n").getBytes(StandardCharsets.UTF_8);
            outputStream.write(strToBytes);
            outputStream.write(("-------------------------------------\r\n").getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }
}
