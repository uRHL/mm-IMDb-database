import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.ArrayList;

public class ExcelScraper {

    public static ArrayList<Film> scrapExcel(String excelFile) throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(excelFile));
        ArrayList<Film> list = new ArrayList<Film>();

        //Get the first sheet from the Excel workbook
        XSSFSheet sheet = wb.getSheetAt(0);

        int totalRows = sheet.getLastRowNum();
        int successfulReads = 0;
        int titlesWithYear = 0;
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
            There are 4 different data types contained in the excel
            Integers, Doubles, Strings, and Hyperlinks
             */
                Integer id = (int) idCell.getNumericCellValue();
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
                film.setGenres(genres.trim().split("[|]"));
                film.setPosterURL(posterURL);
                successfulReads++;

                //Finally add the film to the list
                list.add(film);
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("******Impossible to read the content of the cell");
            }
        }
        System.out.println("Successful reads = " + successfulReads + "/" + totalRows);
        System.out.println("Titles with year = " + titlesWithYear + "/" + totalRows);
        return list;
    }
}
