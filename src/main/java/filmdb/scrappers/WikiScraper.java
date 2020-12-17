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
    private static final String WIKI_ESP_WORDS = "https://es.wiktionary.org/wiki/Ap%C3%A9ndice:1000_palabras_b%C3%A1sicas_en_espa%C3%B1ol";
    private static final String WIKI_ESP_HISTORY = "https://es.wikipedia.org/wiki/Historia_de_Espa%C3%B1a";
    private static final String WIKI_LATIN_HISTORY = "https://es.wikipedia.org/wiki/Historia_de_Sudam%C3%A9rica";
    private static final String DICTIONARY_NAME = "..//mm-IMDb-database//output//spanish-word-dic.json";

    /**
     * Generates an spanish-word dictionary scrapping the predefined Wikipedia URLs
     *
     * @return 0 if the dictionary could be generated. 1 otherwise
     */
    public static int generateSpanishDictionary() {
        int errno = 1;
        ArrayList<String> spanishWords = new ArrayList<>();
        try {
            //Scrap the wiki page: 1000 basic spanish words
            Element mainSection = Jsoup.connect(WIKI_ESP_WORDS).get().selectFirst("div[id=mw-content-text]");
            Elements elements = mainSection.select("a[href~=/wiki/[\\w\\d\\W]]");
            for (Element elem : elements) {
                spanishWords.add(removeSpecialCharacters(elem.text()));
            }
            //Scrap the wiki page: History of Spain
            spanishWords.addAll(WikiScraper.scrapComplexWikiPage(WIKI_ESP_HISTORY));
            //Scrap the wiki page: History of South America
            spanishWords.addAll(WikiScraper.scrapComplexWikiPage(WIKI_LATIN_HISTORY));
            WikiScraper.generateJsonDictionary(spanishWords);
            System.out.println("Dictionary successfully created: " + spanishWords.size() + " words");
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
    private static List<String> scrapComplexWikiPage(String wikiURL) {
        ArrayList<String> words = new ArrayList<>();
        try {
            Element mainSection = Jsoup.connect(wikiURL).get().selectFirst("div[id=mw-content-text]");
            Elements elements = mainSection.select("a[href~=/wiki/[\\w\\d\\W]]");
            for (Element elem : elements) {
                if (WikiScraper.checkElementAttributes(elem) && (!words.contains(elem.text()))) {
                    words.add(removeSpecialCharacters(elem.text()));
                }
            }
        } catch (IOException e) {
            System.out.println("UNSUCCESSFUL function 'scrapComplexWikiPage' (ref: " + e + ")");
        }
        return words;
    }

    /**
     * Generates a JSON-dictionary from the specified word list
     *
     * @param wordList List of words to be written in the file
     */
    private static void generateJsonDictionary(List<String> wordList) {
        try {
            File dictionaryFile = new File(DICTIONARY_NAME);
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(dictionaryFile.getCanonicalPath())));
            out.write((new Gson().toJson(wordList, ArrayList.class)).getBytes());
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
    private static boolean checkElementAttributes(Element elem) {
        return elem.attr("title").length() > 1 && !elem.attr("title").equals("ISBN") && !elem.attr("title").equals("ISSN") && !elem.parent().hasClass("citation") && !elem.text().matches("[0-9\\-]*") && !elem.text().matches("[A-Z]{1}[0-9]*") && elem.text().length() != 0;

    }

    /**
     * Removes any special character (tildes, ñ, ç, parenthesis, brackets,colons, semicolons, dashes) present in the provided string
     * @param text String whose tildes whose special characters will be removed
     * @return A new string without special characters
     */
    private static String removeSpecialCharacters(String text){
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
}
