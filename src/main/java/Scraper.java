import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Scraper {
    private String url;
    private Document doc;
    //Constant used for testing purposes
    public static final String URL_WIKI = "https://www.imdb.com/title/tt0111161";
    public static final String IMDb_DATA = "C:\\Users\\Elir Ray\\IdeaProjects\\mm-IMDb-database\\src\\imbd-data\\MovieGenreIGC_v3.xlsx";
    public Scraper() {}

    public Scraper(String url) throws IOException {
        this.url = url;
        this.doc = Jsoup.connect(url).get();
    }
    public static void main(String[] args) throws IOException {
        Scraper scraper = new Scraper(URL_WIKI);
//        System.out.println("Title: " + scraper.getTitle());
//        System.out.println("Links: " +scraper.getLinks());
//        System.out.println("\n");
//        System.out.println("Image links: " + scraper.getImageLinks());
//        System.out.println("\n");
//        System.out.println("Headings h4: " + scraper.selectElements("h4"));
//        System.out.println("\n");
//        System.out.println("Divs summary_item: " + scraper.selectElements("div.credit_summary_item"));
//        System.out.println("\n");
//        System.out.println("Divs summary_text: " + scraper.selectElements("div.summary_text"));
//        System.out.println("\n");
        try {
            scraper.readExcel();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Impossible to read the excel");
        }
    }

    /**
     * Retrieves the title of a web page
     * @return The String representation of the page title
     */
    public String getTitle() {
        try {
            return (this.url + ":\t" + this.doc.title() + "\n");
        }catch (Exception e){
            return "Error reading the web page";
        }

    }

    /**
     * Retrieves all the links contained in a web page
     * @return An ArrayList containing all the links contained in the specified page
     */
    public List<String> getLinks() {
        ArrayList<String> links = new ArrayList<>();

        Elements elements = this.doc.select("a[href]");
        for (Element elem: elements) {
            links.add(elem.attr("href"));
        }
        return links;
    }

    /**
     * Retrieve those images' links accepted by the internal regex
     * @return An ArrayList of Strings with all the images' links accepted by the regex
     */
    public List<String> getImageLinks() {
        ArrayList<String> links = new ArrayList<>();

        //Elements elements = this.doc.select("img[src$=.jpg]");
        Elements elements = this.doc.select("img[src~=[\\w\\d\\W]log[\\w\\d\\W]]");
        for (Element elem: elements) {
                links.add(elem.attr("src"));
        }
        return links;
    }

    /**
     * Retrieves the content of the elements matching the specified selector
     * @param selector String to be used as selector
     * @return An ArrayList of Strings, containing the inner text of all the elements found
     */
    public List<String> selectElements(String selector) {
        ArrayList<String> list = new ArrayList<>();

        Elements elements = this.doc.select(selector);

        for (Element elem: elements) {
            list.add(elem.text());
        }
        return list;
    }

    private void readExcel() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(IMDb_DATA));

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
            There are 4 different data types contained in the excel
            Integers, Doubles, Strings, and Hyperlinks
             */
                Integer id = (int) idCell.getNumericCellValue();
                Hyperlink filmURL = urlCell.getHyperlink();
                String title = titleCell.getStringCellValue();
                Double avgScore = scoreCell.getNumericCellValue();
                String genres = genreCell.getStringCellValue();
                Hyperlink posterURL = posterCell.getHyperlink();

//            BigDecimal price = new BigDecimal(priceCell.getNumericCellValue()).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                System.out.println(i + ": " + title);
                successfulReads++;
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("******Impossible to read the content of the cell");
            }
        }
        System.out.println("Successful reads = " + successfulReads + "/" + totalRows);
    }
}