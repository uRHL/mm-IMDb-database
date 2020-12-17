package filmdb.scrappers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class WebScraper {
    private final static String IMDb_ROOT = "https://www.imdb.com";
    /**
     * To see more about this regex visit https://regex101.com/library/yL3zV3
     */
    private final static String NON_ASCII_REGEX = "[^\\x00-\\x7F|\\s]";

    private final String url;
    private final Document doc;

    public WebScraper(String url) throws Exception {
        this.url = url;
        this.doc = Jsoup.connect(url).get();

    }

    /**
     * Extracts the year contained within a String
     *
     * @param date String containing some type of date information
     * @return the year contained in the String date
     */
    public static int parseYear(String date) {
        //String year = "";
        int year = -1;
        for (int i = 0; i < date.length() - 3; i += 1) {
            //verify if the substring has a valid year format
            if (date.substring(i, i + 4).matches("[0-9]{4}")) {
                year = Integer.parseInt(date.substring(i, i + 4));
                break;
            }
        }
        return year;
    }

    /**
     * Removes the non-ascii chars present in the String
     * These characters are matched by the regex {@link WebScraper#NON_ASCII_REGEX}
     *
     * @param text String whose non-Ascii chars will be removed
     * @return a new String without non-ascii chars
     */
    private static String removeNonAsciiChars(String text) {
        try {
            /*
            TODO: replace each non-ascii char by its corresponding readable representation
            ie: \u0027 --> '
            */
            return text.replaceAll(NON_ASCII_REGEX, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

    /**
     * Finds the first URL that contains the specified keyword
     *
     * @param keyword Substring contained in the target link
     * @return The first complete URL that contains the specified keyword
     */
    public String getUrlByKeyword(String keyword) {
        String url = null;
        try {
            String link = doc.select("a[href~=[\\w\\d\\W]" + keyword + "[\\w\\d\\W]]").attr("href");
            if (link != null) {
                url = IMDb_ROOT + link;
            }
        } catch (Exception e) {
            System.out.println("UNSUCCESSFUL function 'getUrlByKeyword' (ref: " + e + ")");
        } finally {
            return url;
        }
    }

    /**
     * Retrieves the title of a web page
     *
     * @return The String representation of the page title
     */
    public String getPageTitle() {
        try {
            return (this.url + ":\t" + this.doc.title() + "\n");
        } catch (Exception e) {
            System.out.println("UNSUCCESSFUL function 'getPageTitle' (ref: " + e + ")");
            return null;
        }
    }

    /**
     * Scraps, from the film's web site, the release year of the film
     *
     * @return An int representing the year when the film was released. -1 if the year could not be scrapped
     */
    public int getReleaseYear() {
        try {
            String completeDate = this.doc.select("a[title='See more release dates']").text();
            return WebScraper.parseYear(completeDate);
        } catch (Exception e) {
            System.out.println("UNSUCCESSFUL function 'getReleaseYear' (ref: " + e + ")");
            return -1;
        }
    }

    /**
     * Scraps, from film's web site in IMDb, the list of main actors of the film
     *
     * @return A String Array with the names of the actors
     */
    public String[] getMainActors() {
        try {
            WebScraper actorScraper = new WebScraper(this.getUrlByKeyword("fullcredits"));
            Element table = actorScraper.doc.selectFirst("table[class='cast_list']");

            Elements elements = table.select("a[href~=/name/[\\w\\d\\W]]");
            List<String> mainActors = new ArrayList<>();

            for (Element elem : elements) {
                String actorName = removeNonAsciiChars(elem.text());
                elem.html();
                if (!actorName.equals("")) {
                    mainActors.add(actorName);
                }
            }
            return mainActors.toArray(new String[0]);
        } catch (Exception e) {
            System.out.println("UNSUCCESSFUL function 'getMainActors' (ref: " + e + ")");
            return null;
        }

    }

    /**
     * Scraps, from film's web site in IMDb, the synopsis of the film
     *
     * @return A String with the synopsis of the film
     */
    public String getSynopsis() {
        String synopsis;
        try {
            WebScraper synopsisScraper = new WebScraper(this.getUrlByKeyword("plotsummary"));
            synopsis = synopsisScraper.doc.selectFirst("li[id~=synopsis-[\\w\\d\\W]]").text();
            if (synopsis.contains("It looks like we don't have a Synopsis for this title yet")) {
                //The synopsis for that film has not been written yet
                //Obtain the first short summary in available for the film
                synopsis = synopsisScraper.doc.selectFirst("li[id~=summary-[\\w\\d\\W]]").child(0).text();
            }
        } catch (Exception e) {
            System.out.println("UNSUCCESSFUL function 'getSynopsis' (ref: " + e + ")");
            synopsis = null;
        }
        return WebScraper.removeNonAsciiChars(synopsis);
    }

    /**
     * Scraps, from film's web site in IMDb, the plot keywords
     *
     * @return A String Array with the plot keywords
     */
    public String[] getPlotKeywords() {
        ArrayList<String> plotKeywords = new ArrayList<>();
        try {
            WebScraper keywordScraper = new WebScraper(this.getUrlByKeyword("keywords"));
            Elements elements = keywordScraper.getElementsLinkStartingBy("/search/keyword");
            for (Element elem : elements) {
                plotKeywords.add(removeNonAsciiChars(elem.text()));
            }
            return plotKeywords.toArray(new String[0]);
        } catch (Exception e) {
            System.out.println("UNSUCCESSFUL function 'getPlotKeywords' (ref: " + e + ")");
            return null;
        }
    }

    /**
     * Scraps, from film's web site in IMDb, the country of origin of the film
     *
     * @return A String containing the film's country of origin
     */
    public String getCountryOfOrigin() {
        try {
            return removeNonAsciiChars(getElemLinkContaining("country_of_origin").text());
        } catch (Exception e) {
            System.out.println("UNSUCCESSFUL function 'getOriginCountry' (ref: " + e + ")");
            return null;
        }
    }

    /**
     * Scraps, from film's web site in IMDb, the primary languages of the film
     *
     * @return A String Array containing the film's primary languages
     */
    public String[] getPrimaryLanguages() {
        try {
            ArrayList<String> primaryLanguages = new ArrayList<>();
            Elements elements = this.getElementsLinkContaining("primary_language");
            for (Element elem : elements) {
                primaryLanguages.add(removeNonAsciiChars(elem.text().trim()));
            }
            return primaryLanguages.toArray(new String[0]);
        } catch (Exception e) {
            System.out.println("UNSUCCESSFUL function 'getPrimaryLanguages' (ref: " + e + ")");
            return null;
        }
    }

    /**
     * Scraps, from film's web site in IMDb, the filming locations of the film
     *
     * @return A String Array containing the name of the locations where the filming took place
     */
    public String[] getFilmingLocations() {
        ArrayList<String> locations = new ArrayList<>();
        try {
            WebScraper locationScraper = new WebScraper(this.getUrlByKeyword("locations"));
            Elements elements = locationScraper.getElementsLinkContaining("locations=");

            for (Element elem : elements) {
                locations.add(removeNonAsciiChars(elem.text()));
            }
            if (locations.isEmpty()) {
                throw new Exception("No locations found");
            }
            return locations.toArray(new String[0]);
        } catch (Exception e) {
            System.out.println("UNSUCCESSFUL function 'getFilmingLocations' (ref: " + e + ")");
            return null;
        }
    }

    /**
     * Scraps the first <a> {@link Elements} whose href attribute contains the specified string
     *
     * @param keyword Substring contained within the href attribute
     * @return an {@link Element} containing the <a> matching the query
     * @see WebScraper#getElementsLinkStartingBy(String)
     * @see WebScraper#getElementsLinkContaining(String)
     */
    private Element getElemLinkContaining(String keyword) {
        Element linkElem;
        try {
            linkElem = this.doc.selectFirst("a[href~=[\\w\\d\\W]" + keyword + "[\\w\\d\\W]]");
        } catch (Exception e) {
            System.out.println("UNSUCCESSFUL function 'getElemLinkContaining' (ref: " + e + ")");
            linkElem = null;
        }
        return linkElem;
    }

    /**
     * Scraps all <a> {@link Elements} whose href attribute contains the specified string
     *
     * @param keyword Substring contained within the href attribute
     * @return an {@link Elements} containing <a> {@link Element}
     * @see WebScraper#getElementsLinkStartingBy(String)
     */
    private Elements getElementsLinkContaining(String keyword) {
        Elements linkElemList;
        try {
            linkElemList = this.doc.select("a[href~=[\\w\\d\\W]" + keyword + "[\\w\\d\\W]]");
        } catch (Exception e) {
            System.out.println("UNSUCCESSFUL function 'getElementsLinkContaining' (ref: " + e + ")");
            linkElemList = null;
        }
        return linkElemList;
    }

    /**
     * Scraps all <a> {@link Elements} whose href attribute starts by the specified string
     *
     * @param substring Substring that matches the first part of the href attribute
     * @return an {@link Elements} containing <a> {@link Element}
     */
    private Elements getElementsLinkStartingBy(String substring) {
        Elements linkElemList;
        try {
            linkElemList = this.doc.select("a[href~=" + substring + "[\\w\\d\\W]]");
        } catch (Exception e) {
            System.out.println("UNSUCCESSFUL function 'getElementsLinkStartingBy' (ref: " + e + ")");
            linkElemList = null;
        }
        return linkElemList;
    }

    /**
     * Retrieves all the valid links contained inside {@link WebScraper#doc}
     *
     * @return An ArrayList containing all the links contained in the specified page
     */
    public List<String> getLinks() {
        ArrayList<String> links = new ArrayList<>();

        Elements elements = this.doc.select("a[href]");
        for (Element elem : elements) {
            links.add(elem.attr("href"));
        }
        return links;
    }

    /**
     * Retrieve those images' links containing the word "log"
     *
     * @return An ArrayList of Strings with all the images' links matching the regex
     */
    public List<String> getImageLinks() {
        ArrayList<String> links = new ArrayList<>();

        Elements elements = this.doc.select("img[src~=[\\w\\d\\W]log[\\w\\d\\W]]");
        for (Element elem : elements) {
            links.add(elem.attr("src"));
        }
        return links;
    }

    /**
     * Retrieves the content of the elements matching the specified selector
     * For example
     * webScraper.selectElements("h4") returns the inner text of all the h4 headings
     * webScraper.selectElements("div.credit_summary_item") returns the inner text of all the div
     * containing the CSS class "credit_summary_item"
     *
     * @param selector String to be used as selector
     * @return An ArrayList of Strings, containing the inner text of all the elements found
     */
    public List<String> selectElementsText(String selector) {
        ArrayList<String> list = new ArrayList<>();

        Elements elements = this.doc.select(selector);

        for (Element elem : elements) {
            list.add(removeNonAsciiChars(elem.text()));
        }
        return list;
    }
}