import java.util.ArrayList;
import java.util.List;

public class ScrappingApp {

    //Application parameters
    private static final String IMDb_DATA_EXCEL = "C:\\Users\\Elir Ray\\IdeaProjects\\mm-IMDb-database\\src\\imbd-data\\MovieGenreIGC_v3.xlsx";
    private static final int START_INDEX = 18000;
    private static final int TOTAL_SCRAPS = 9000;
    private static final String FILMS_TO_SCRAP = "";

    public static void main(String[] args) {

        try {
            FilmScraper filmScraper = new FilmScraper(IMDb_DATA_EXCEL, START_INDEX, TOTAL_SCRAPS);

            //Scrap all the needed information about the films from the Excel file and the film's url

            //1st mode: scrap a single film
            //filmScraper.scrapFilmByImdbID(118114);

            //2nd mode: scrap a set of films
            //filmScraper.scrapFilmSet(ScrappingApp.parseFilmList(FILMS_TO_SCRAP));

            //3rd mode: scrap all available films
            filmScraper.scrapAllFilms();

            //Generate the Bulk JSON file to insert the documents in Elastic Search
            filmScraper.generateBulkTasksFile();
            filmScraper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Integer> parseFilmList(String filmList){
        ArrayList<Integer> result = new ArrayList<>();
        String[] filmArray = filmList.toString().replace("[", "").replace("]", "").split(", ");
        for (String string : filmArray) {
            result.add(Integer.parseInt(string));
        }
        return result;
    }
}
