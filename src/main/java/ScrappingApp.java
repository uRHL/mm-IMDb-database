import java.util.ArrayList;

public class ScrappingApp {
    //Constant used for testing purposes
    public static final String URL_WIKI = "https://www.imdb.com/title/tt0111161";
    public static final String IMDb_DATA = "C:\\Users\\Elir Ray\\IdeaProjects\\mm-IMDb-database\\src\\imbd-data\\MovieGenreIGC_v3.xlsx";

    public static void main(String[] args){
        try {
            WebScraper webScraper = new WebScraper(URL_WIKI);
            //System.out.println(webScraper.getReleaseYear());
//            String[] temp = webScraper.getMainActors();
//            for (String str: temp){
//                System.out.println(str);
//            }
            //System.out.println(webScraper.getSynopsis());
//            String[] temp = webScraper.getPlotKeywords();
//            for (String str: temp){
//                System.out.println(str);
//            }
//            System.out.println(webScraper.getCountryOfOrigin());
//            System.out.println(webScraper.getPrimaryLanguage());
            String[] temp = webScraper.getFilmingLocations();
            for (String str: temp){
                System.out.println(str);
                str.isEmpty();
            }

//        System.out.println("Title: " + webScraper.getPageTitle());
//        System.out.println("Links: " +webScraper.getLinks());
//        System.out.println("\n");
//        System.out.println("Image links: " + webScraper.getImageLinks());
//        System.out.println("\n");
//        System.out.println("Headings h4: " + webScraper.selectElements("h4"));
//        System.out.println("\n");
//        System.out.println("Divs summary_item: " + webScraper.selectElements("div.credit_summary_item"));
//        System.out.println("\n");
//        System.out.println("Divs summary_text: " + webScraper.selectElements("div.summary_text"));
//        System.out.println("\n");

            ArrayList<Film> filmList = new ArrayList<Film>();
            //filmList.addAll(ExcelScraper.scrapExcel(IMDb_DATA));
//            for (Film film: filmList) {
//                System.out.println(film.toJson());
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
