package filmdb;

import filmdb.scrappers.FilmScraper;

import java.util.ArrayList;
import java.util.List;

public class ScrappingApp {

    // Required External references
    private static final String IMDb_DATA_EXCEL = "..//mm-IMDb-database//imbd-data//MovieGenreIGC_v3.xlsx";

    // Execution parameters
    private static final int START_INDEX = 36000;
    private static final int TOTAL_SCRAPS = 9000;
    // Single-scrap execution mode parameters
    private static final int SINGLE_SCRAP = 1;
    private static final int SINGLE_FILM_TO_SCRAP = 173714;
    // Set-scrap execution mode parameters
    private static final int SET_SCRAP = 2;
    private static final String FILMS_TO_SCRAP = "[1132193, 2316325, 1022603, 1811293]";
    // Full-scrap execution mode parameters
    private static final int FULL_SCRAP = 3;


    public static void main(String[] args) {

        ScrappingApp scrappingApp = new ScrappingApp();
        System.exit(scrappingApp.startFilmScrappingRoutine(SINGLE_SCRAP));

    }

    /**
     * Creates {@link FilmScraper} and starts the scrapping process in one of the three different modes.
     *
     * @param mode Scrapping mode
     * @return 0 if the routine ended successfully. 1 otherwise
     * @see ScrappingApp#SINGLE_SCRAP
     * @see ScrappingApp#SET_SCRAP
     * @see ScrappingApp#FULL_SCRAP
     */
    private int startFilmScrappingRoutine(int mode) {
        int errno = 1;
        try {
            //Scrap all the needed information about the films from the Excel file and the film's url
            FilmScraper filmScraper = new FilmScraper(IMDb_DATA_EXCEL, START_INDEX, TOTAL_SCRAPS);
            switch (mode) {
                case SINGLE_SCRAP:
                    //1st mode: scrap a single film
                    filmScraper.scrapSingleFilm(SINGLE_FILM_TO_SCRAP);
                    break;
                case SET_SCRAP:
                    //2nd mode: scrap a set of films
                    filmScraper.scrapFilmSet(ScrappingApp.parseFilmList(FILMS_TO_SCRAP));
                    break;
                case FULL_SCRAP:
                    //3rd mode: scrap all available films
                    filmScraper.scrapAllFilms();
                    break;
                default:
                    throw new Exception("Unrecognized mode ");
            }
            //Generate the Bulk JSON file to insert the documents in Elastic Search
            filmScraper.generateBulkTasksFile();
            filmScraper.close();
            errno = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errno;
    }

    /**
     * Parses the given String, containing film's IDs, to a List of integers, with those IDs
     *
     * @param filmList An String containing a list of imdbIDs
     * @return An ArrayList of Integers containing the same film IDs as the input String
     */
    private static List<Integer> parseFilmList(String filmList) {
        ArrayList<Integer> result = new ArrayList<>();
        String[] filmArray = filmList.replace("[", "").replace("]", "").split(", ");
        for (String string : filmArray) {
            result.add(Integer.parseInt(string));
        }
        return result;
    }
}
