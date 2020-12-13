import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.IOException;

public class Film {
    /**
     * constant used with numeric attributes to indicate that its default value has not been changed yet
     */
    private static final int DEFAULT_VALUE = -1;
    private static final float DEFAULT_VALUE_F = -1F;

    private String filmURL;
    private String posterURL;
    @Expose
    private int imdbID;
    @Expose
    private String title;
    @Expose
    private String synopsis;
    @Expose
    private String originCountry;
    @Expose
    private int releaseYear;
    @Expose
    private float avgScore;
    @Expose
    private String[] genres;
    @Expose
    private String[] mainActors;
    @Expose
    private String[] plotKeywords;
    @Expose
    private String[] filmingLocations;
    @Expose
    private String[] primaryLanguages;


    /**
     * Constructor that initializes the numeric attributes to -1 so that it can be known if the default value has been modified.
     * The rest of attributes are objects, thus initially they are pointing to null;
     */
    public Film() {
        this.imdbID = DEFAULT_VALUE;
        this.releaseYear = DEFAULT_VALUE;
        this.avgScore = DEFAULT_VALUE_F;
    }

    public int getImdbID() {
        return imdbID;
    }

    public void setImdbID(int imdbID) {
        this.imdbID = imdbID;
    }

    public String getFilmURL() {
        return filmURL;
    }

    public void setFilmURL(String filmURL) {
        this.filmURL = filmURL;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public void setPosterURL(String posterURL) {
        this.posterURL = posterURL;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Sets the field {@link Film#title} to a new value. Occasionally, the title scraped from the web also contains
     * the release year. If so, the year is subtracted from the title
     * and the field {@link Film#releaseYear} is set accordingly
     *
     * @param title String containing the new title
     */
    public void setTitle(String title) {
        if (title.contains("(")) {
            //Separate the title and the year
            String[] sTitle = title.split("[(]");
            //Get the title
            title = sTitle[0].trim();
            //Remove white spaces and get the year
            this.releaseYear = Integer.parseInt(sTitle[1].replace(" ", "").substring(0, 4));
        }
        this.title = title;
    }

    public float getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(float avgScore) {
        this.avgScore = avgScore;
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public String[] getMainActors() {
        return mainActors;
    }

    public void setMainActors(String[] mainActors) {
        this.mainActors = mainActors;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String[] getPlotKeywords() {
        return plotKeywords;
    }

    public void setPlotKeywords(String[] plotKeywords) {
        this.plotKeywords = plotKeywords;
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        this.originCountry = originCountry;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String[] getPrimaryLanguages() {
        return primaryLanguages;
    }

    public void setPrimaryLanguages(String[] primaryLanguages) {
        this.primaryLanguages = primaryLanguages;
    }

    public String[] getFilmingLocations() {
        return filmingLocations;
    }

    public void setFilmingLocations(String[] filmingLocations) {
        this.filmingLocations = filmingLocations;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this, Film.class);

    }

    /**
     * Tries to scrap from the web site (filmURL) the attributes of the film that could not be scrapped from the excel file.
     * Those attributes must be initialized, because they cannot be obtained from the {@link WebScraper}
     *
     * @return True if all the fields could be initialized. False otherwise.
     * @see Film#checkExcelValues()
     */
    public boolean initializeUnsetAttributes() {
        boolean result = false;

        // First check the pre-required attributes of the Film
        if (checkExcelValues()) {
            try {
                //If the pre-required attributes are OK, then scrap the remaining attributes from the film's web site.
                WebScraper webScraper = new WebScraper(this.filmURL);

            /*Sometimes the title (obtained from the excel) contains also the release year.
            Check if the year of this film could be extracted from the excel*/
                if (releaseYear == DEFAULT_VALUE) {
                    this.releaseYear = webScraper.getReleaseYear();
                }

                //The rest of the attributes have to be scraped always
                this.mainActors = webScraper.getMainActors();
                this.synopsis = webScraper.getSynopsis();
                this.plotKeywords = webScraper.getPlotKeywords();
                this.filmingLocations = webScraper.getFilmingLocations();
                this.originCountry = webScraper.getCountryOfOrigin();
                this.primaryLanguages = webScraper.getPrimaryLanguages();

                //All the fields could be correctly set
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Check that the attributes, found in the IMDb excel file, has been correctly set.
     * The attributes that can be found in the excel are {@link Film#imdbID}, {@link Film#filmURL},
     * {@link Film#posterURL}, {@link Film#title}, {@link Film#avgScore}, {@link Film#genres}
     *
     * @return True if all the corresponding attributes are properly initialized. False otherwise
     */
    private boolean checkExcelValues() {
        return ((this.imdbID != DEFAULT_VALUE) && (!this.filmURL.isEmpty()) && (!this.posterURL.isEmpty()) && (!this.title.isEmpty()) && (this.avgScore != DEFAULT_VALUE_F) && (this.genres.length >= 1));
    }
}
