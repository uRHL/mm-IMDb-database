package filmdb;

import com.google.gson.Gson;
import filmdb.scrappers.FilmScraper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;

public class ScrappingApp {

    // Required External references
    private static final String IMDb_DATA_EXCEL = "C:\\Users\\Elir Ray\\IdeaProjects\\mm-IMDb-database\\src\\imbd-data\\MovieGenreIGC_v3.xlsx";
    private static final String WIKI_ESP_WORDS = "https://es.wiktionary.org/wiki/Ap%C3%A9ndice:1000_palabras_b%C3%A1sicas_en_espa%C3%B1ol";
    private static final String WIKI_ESP_HISTORY = "https://es.wikipedia.org/wiki/Historia_de_Espa%C3%B1a";
    private static final String WIKI_LATIN_HISTORY = "https://es.wikipedia.org/wiki/Historia_de_Sudam%C3%A9rica";

    // Application parameters
    private static final int START_INDEX = 36000;
    private static final int TOTAL_SCRAPS = 9000;
    // Single-scrap execution mode parameters
    private static final int SINGLE_SCRAP = 1;
    private static final int FILM_TO_SCRAP = 39016;
    // Set-scrap execution mode parameters
    private static final int SET_SCRAP = 2;
    private static final String FILMS_TO_SCRAP = "[3901628, 3145026, 3163080]";
    // Full-scrap execution mode parameters
    private static final int FULL_SCRAP = 3;




    public static void main(String[] args) {

        ScrappingApp scrappingApp = new ScrappingApp();
        System.exit(scrappingApp.startFilmScrappingRoutine(FULL_SCRAP));

    }
    private int startFilmScrappingRoutine(int mode){
        int errno = 1;
        try {
            //Scrap all the needed information about the films from the Excel file and the film's url
            FilmScraper filmScraper = new FilmScraper(IMDb_DATA_EXCEL, START_INDEX, TOTAL_SCRAPS);
            switch (mode){
                case SINGLE_SCRAP:
                    //1st mode: scrap a single film
                    filmScraper.scrapFilmByImdbID(ScrappingApp.FILM_TO_SCRAP);
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
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            return errno;
        }
    }

    private int generateSpanishDictionary() {
        int errno = 1;
        ArrayList<String> spanishWords = new ArrayList<>();
        try {
            //Scrap the wiki page: 1000 basic spanish words
            Element mainSection = Jsoup.connect(WIKI_ESP_WORDS).get().selectFirst("div[id=mw-content-text]");
            Elements elements = mainSection.select("a[href~=/wiki/[\\w\\d\\W]]");
            for (Element elem : elements) {
                spanishWords.add(elem.text());
            }
            //Scrap the wiki page: History of Spain
            mainSection = Jsoup.connect(WIKI_ESP_HISTORY).get().selectFirst("div[id=mw-content-text]");
            elements = mainSection.select("a[href~=/wiki/[\\w\\d\\W]]");
            for (Element elem : elements) {
                if (ScrappingApp.checkElementAttributes(elem) && (!spanishWords.contains(elem.text()))) {
                    spanishWords.add(elem.text());
                }
            }
            //Scrap the wiki page: History of South America
            mainSection = Jsoup.connect(WIKI_LATIN_HISTORY).get().selectFirst("div[id=mw-content-text]");
            elements = mainSection.select("a[href~=/wiki/[\\w\\d\\W]]");
            for (Element elem : elements) {
                if (ScrappingApp.checkElementAttributes(elem) && (!spanishWords.contains(elem.text()))) {
                    spanishWords.add(elem.text());
                }
            }
            ScrappingApp.generateJsonFile("latin-spanish-words-dic.json", spanishWords);
            System.out.println("Dictionary successfully created: " + spanishWords.size() + " words");
            errno = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return errno;
        }
    }

    private static List<Integer> parseFilmList(String filmList) {
        ArrayList<Integer> result = new ArrayList<>();
        String[] filmArray = filmList.toString().replace("[", "").replace("]", "").split(", ");
        for (String string : filmArray) {
            result.add(Integer.parseInt(string));
        }
        return result;
    }

    private static void generateJsonFile(String fileName, Object obj) {
        try {
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(fileName)));
            out.writeUTF(new Gson().toJson(obj));
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkElementAttributes(Element elem) {
        if (elem.attr("title").length() <= 1 || elem.attr("title").equals("ISBN") || elem.attr("title").equals("ISSN") || elem.parent().hasClass("citation") || elem.text().matches("[0-9\\-]*") || elem.text().length() == 0) {
            return false;
        } else {
            return true;
        }

    }
}
