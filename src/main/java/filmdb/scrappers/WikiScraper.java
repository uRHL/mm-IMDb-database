package filmdb.scrappers;

import com.google.gson.Gson;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WikiScraper {
    // Execution parameters
    private static final String WIKI_ESP_HISTORY = "https://es.wikipedia.org/wiki/Historia_de_Espa%C3%B1a";
    private static final String WIKI_LATIN_HISTORY = "https://es.wikipedia.org/wiki/Historia_de_Sudam%C3%A9rica";
    private static final String DICTIONARY_NAME = "..//mm-IMDb-database//output//spanish-word-dic.json";
    /**
     * Maximum number of words included in the dictionary. This number if set by the attribute
     * 'indices.query.bool.max_clause_count' of the Elastic Search node
     */
    private static final int MAX_DICTIONARY_SIZE = 1024;

    // Attributes
    private final List<String> words;

    public WikiScraper(){
         this.words = new ArrayList<>();
    }

    /**
     * Generates an spanish-word dictionary scrapping the predefined Wikipedia URLs
     *
     * @return 0 if the dictionary could be generated. 1 otherwise
     */
    public int generateSpanishDictionary() {
        int errno = 1;
        try {

            //Scrap the wiki page: History of Spain
            this.words.addAll(this.scrapComplexWikiPage(WIKI_ESP_HISTORY));
            //Scrap the wiki page: History of South America
            this.words.addAll(this.scrapComplexWikiPage(WIKI_LATIN_HISTORY));

            this.generateJsonDictionary();

            System.out.println("Dictionary successfully created: " + this.words.size() + " words");
            errno = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errno;
    }

    /**
     * Scraps a Wikipedia page looking for remarkable words. Remarkable words are considered those that are enclosed in an <a> element
     *
     * @param wikiURL URL of the Wikipedia page to be scrapped
     * @return an ArrayList of Strings with all the remarkable words found
     */
    private List<String> scrapComplexWikiPage(String wikiURL) {
        ArrayList<String> scrappedWords = new ArrayList<>();
        try {
            Element mainSection = Jsoup.connect(wikiURL).get().selectFirst("div[id=mw-content-text]");
            Elements elements = mainSection.select("a[href~=/wiki/[\\w\\d\\W]]");
            for (Element elem : elements) {
                String word = removeSpecialCharacters(elem.text());
                if (this.checkElementAttributes(elem) && !scrappedWords.contains(word) && !isSimilarToADate(word)) {
                    scrappedWords.add(word);
                }
            }
        } catch (IOException e) {
            System.out.println("UNSUCCESSFUL function 'scrapComplexWikiPage' (ref: " + e + ")");
        }

        //Add all the words if there is enough space
        if (scrappedWords.size() + this.words.size() > MAX_DICTIONARY_SIZE){
            return scrappedWords.subList(0, MAX_DICTIONARY_SIZE - this.words.size());
        }else {
            return scrappedWords;
        }
    }

    /**
     * Generates a JSON-dictionary from the words scrapped from the web
     *
     */
    private void generateJsonDictionary() {
        try {
            File dictionaryFile = new File(DICTIONARY_NAME);
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(dictionaryFile.getCanonicalPath())));
            out.write((new Gson().toJson(this.words, ArrayList.class)).getBytes());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks the attributes of an <a> {@link Element} to verify that the element contains useful information to scrap
     *
     * @param elem {@link Element} whose attributes are checked
     * @return True if the element matches all the attribute requirements. False otherwise
     */
    private boolean checkElementAttributes(Element elem) {
        return elem.attr("title").length() > 1 && !elem.attr("title").equals("ISBN") && !elem.attr("title").equals("ISSN") && !elem.parent().hasClass("citation") && !elem.text().matches("[0-9\\-]*") && !elem.text().matches("[A-Z][0-9]*") && elem.text().length() != 0;

    }

    /**
     * Removes any special character (tildes, ñ, ç, parenthesis, brackets,colons, semicolons, dashes) present in the provided string
     * @param text String whose tildes whose special characters will be removed
     * @return A new string without special characters
     */
    private String removeSpecialCharacters(String text){
        text = text.replace("Á", "A").replace("á", "a");
        text = text.replace("É", "E").replace("é", "e");
        text = text.replace("Í", "I").replace("í", "i");
        text = text.replace("Ó", "O").replace("ó", "o");
        text = text.replace("Ú", "U").replace("ú", "u");
        text = text.replace("Ñ", "N").replace("ñ", "n");
        text = text.replace("Ç", "C").replace("ç", "c");
        // Remove the remaining brackets, colons, semicolons, etc
        text = text.replaceAll("[()\\[\\]}{.,:;]", "").replaceAll("[_\\-]", " ");
        // Finally remove any non-ascii character that appears in the text
        text = WebScraper.removeNonAsciiChars(text);
        return text;
    }

    /**
     * Checks if the given text is similar to a date or a time event. For example "world cup 2020", "24 December 2020"
     *
     * @param text Text to check
     * @return True if the given text is similar to a date. False otherwise
     */
    private boolean isSimilarToADate(String text) {
        return (text.matches("[a-z\\s]*[0-9]+[a-zA-Z\\s]*") || text.matches("[0-9]+[a-z\\s]*[0-9]+[a-zA-Z\\s]*") || text.matches("[IVXLCDM]*"));
    }
}
