import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebScraper {
    private final static String IMDb_ROOT = "https://www.imdb.com";

    private String url;
    private Document doc;

    public WebScraper(String url) throws IOException {
        this.url = url;
        this.doc = Jsoup.connect(url).get();

    }

    /**
     * Extracts the year contained within a String
     * @param date String containing some type of date information
     * @return the year contained in the String date
     */
    private static int parseYear(String date){
        String year = "";
        if (date.contains("-")){
            //The date is similar to  "TV Series (2014-)"
            year = date.split("[(]")[0].substring(0, 4);
        }else{
            //The date is similar to  "2 July 1999 (Spain)"
            for (String str: date.split("[ ]")) {
                if (str.matches("[0-9]{4}")){
                    year = str;
                }
            }
        }
        //verify that the year format is OK
        if (year.matches("[0-9]{4}")){
            return Integer.parseInt(year);
        }else{
            return -1;
        }

    }

    /**
     * Finds the first URL that contains the specified keyword
     * @param keyword Substring contained in the target link
     * @return The first complete URL that contains the specified keyword
     */
    public String getUrlByKeyword(String keyword){
        return IMDb_ROOT + doc.select("a[href~=[\\w\\d\\W]"+keyword+"[\\w\\d\\W]]").attr("href");
    }

    /**
     * Retrieves the title of a web page
     * @return The String representation of the page title
     */
    public String getPageTitle() {
        try {
            return (this.url + ":\t" + this.doc.title() + "\n");
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Scraps the release year of a film
     * @return An int representing the year when the film was released
     */
    public int getReleaseYear(){
        try {
            String completeDate = this.doc.select("a[title='See more release dates']").text();
            return WebScraper.parseYear(completeDate);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public String[] getMainActors(){
        List<String> mainActors = null;
        try {
            WebScraper actorScraper =  new WebScraper(this.getUrlByKeyword("fullcredits"));
            Element table = actorScraper.doc.selectFirst("table[class='cast_list']");

            Elements elements = table.select("a[href~=/name/[\\w\\d\\W]]");
            mainActors = new ArrayList<String>();

            for (Element elem: elements){
                String actorName = elem.text();
                elem.html();
                if (!actorName.equals("")){
                    mainActors.add(actorName);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            mainActors = null;
        }
        return mainActors.toArray(new String[mainActors.size()]);
    }

    public String getSynopsis(){
        String synopsis = null;
        try {
            WebScraper synopsisSrcaper = new WebScraper(this.getUrlByKeyword("plotsummary"));
            synopsis = synopsisSrcaper.doc.selectFirst("li[id~=synopsis-[\\w\\d\\W]]").text();
        } catch (IOException e) {
            e.printStackTrace();
            synopsis = null;
        }
        return synopsis;
    }

    public String[] getPlotKeywords(){
        ArrayList<String> plotKeywords = new ArrayList<String>();
        try {
            WebScraper keywordSrcaper = new WebScraper(this.getUrlByKeyword("keywords"));
            //Elements elements = keywordSrcaper.doc.select("a[href~=/search/keyword[\\w\\d\\W]]");
            Elements elements = keywordSrcaper.getElementsLinkStartingBy("/search/keyword");
            for (Element elem : elements){
                plotKeywords.add(elem.text());
            }
        }catch (Exception e){
            e.printStackTrace();
            plotKeywords = null;
        }
        return plotKeywords.toArray(new String[plotKeywords.size()]);
    }

    public String getCountryOfOrigin(){
        return getElemLinkContaining("country_of_origin").text();
    }

    public String getPrimaryLanguage(){
        return this.getElemLinkContaining("primary_language").text();
    }

    public String[] getFilmingLocations(){
        ArrayList<String> locations = new ArrayList<String>();
        try {
            WebScraper locationScraper = new WebScraper(this.getUrlByKeyword("locations"));
            Elements elements = locationScraper.getElementsLinkContaining("locations=");
            for (Element elem : elements){
                locations.add(elem.text());
            }
        } catch (IOException e) {
            e.printStackTrace();
            locations = null;
        }
        return locations.toArray(new String[locations.size()]);
    }

    private Element getElemLinkContaining(String keyword){
        Element linkElem;
        try {
            linkElem = this.doc.selectFirst("a[href~=[\\w\\d\\W]"+keyword+ "[\\w\\d\\W]]");
        }catch (Exception e){
            e.printStackTrace();
            linkElem = null;
        }
        return linkElem;
    }

    private Elements getElementsLinkContaining(String keyword){
        Elements linkElemList;
        try {
            linkElemList = this.doc.select("a[href~=[\\w\\d\\W]"+keyword+ "[\\w\\d\\W]]");
        }catch (Exception e){
            e.printStackTrace();
            linkElemList = null;
        }
        return linkElemList;
    }

    private Element getElemLinkStartingBy(String substring){
        Element linkElem;
        try {
            linkElem = this.doc.selectFirst(substring + "[\\w\\d\\W]]");
        }catch (Exception e){
            e.printStackTrace();
            linkElem = null;
        }
        return linkElem;
    }

    private Elements getElementsLinkStartingBy(String substring){
        Elements linkElemList;
        try {
            linkElemList = this.doc.select("a[href~=" + substring+"[\\w\\d\\W]]");
        }catch (Exception e){
            e.printStackTrace();
            linkElemList = null;
        }
        return linkElemList;
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
}