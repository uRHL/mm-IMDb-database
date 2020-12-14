import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.sun.istack.internal.NotNull;

import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.InvalidPropertiesFormatException;
import java.util.Objects;

public class Film {
    /**
     * constant used with numeric attributes to indicate that its default value has not been changed yet
     */
    private static final int DEFAULT_VALUE = -1;
    private static final float DEFAULT_VALUE_F = -1F;
    /**
     * The year was obtained from this article: https://www.history.com/this-day-in-history/first-commercial-movie-screened
     */
    private static final int FIRST_FILM_RELEASE_YEAR = 1895;

    private String url;

    private final ScrapStatus status;

    @Expose
    @NotNull
    private final int imdbID;

    @Expose
    @NotNull
    private String title;

    @Expose
    @NotNull
    private String synopsis;

    // This attribute is not always scrapable, thus is optional
    @Expose
    private String originCountry;

    @Expose
    @NotNull
    private int releaseYear;

    @Expose
    @NotNull
    private float avgScore;

    @Expose
    @NotNull
    private String[] genres;

    // This attribute is not always scrapable, thus is optional
    @Expose
    private String[] mainActors;

    @Expose
    @NotNull
    private String[] plotKeywords;

    // This attribute is not always scrapable, thus is optional
    @Expose
    private String[] filmingLocations;

    // This attribute is not always scrapable, thus is optional
    @Expose
    private String[] primaryLanguages;


    /**
     * Constructor that initializes the numeric attributes to -1 so that it can be known if the default value has been modified.
     * The rest of attributes are objects, thus initially they are pointing to null;
     */
    public Film(int imdbID) throws InvalidPropertiesFormatException {
        this.status = new ScrapStatus();
        if (imdbID <= 0){
            throw new InvalidPropertiesFormatException("Invalid Film.imdbID value (negative)");
        }else {
            this.imdbID = imdbID;
        }
        this.releaseYear = DEFAULT_VALUE;
        this.avgScore = DEFAULT_VALUE_F;
    }

    public ScrapStatus getStatus() {
        return status;
    }

    public int getImdbID() {
        return imdbID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if (url == null){
            throw new NullPointerException("Impossible to set Film.url to null");
        }else {
            this.url = url;
        }

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
    public void setTitle(String title) throws InvalidPropertiesFormatException {
        if (title.contains("(")) {
            //Separate the title and the year
            String[] sTitle = title.split("[(]");
            //Get the title
            title = sTitle[0].trim();
            //Remove white spaces and get the year
            this.setReleaseYear(Integer.parseInt(sTitle[1].replace(" ", "").substring(0, 4)));
        }
        this.title = title;
    }

    public float getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(float avgScore) throws InvalidPropertiesFormatException {
        if (avgScore < 0 || avgScore > 10){
            throw new InvalidPropertiesFormatException("Invalid Film.avgScore value (out of bounds)");
        }else {
            this.avgScore = avgScore;
        }
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        if (genres == null){
            throw new NullPointerException("Impossible to set Film.genres to null");
        }else {
            this.genres = genres;
        }
    }

    public String[] getMainActors() {
        return mainActors;
    }

    public void setMainActors(String[] mainActors) {
        //mainActors attribute may not be found. But it is an optional attribute
        if (mainActors == null){
            this.mainActors = new String[]{"None"};
        }else{
            this.mainActors = mainActors;
        }
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        if (synopsis == null){
            throw new NullPointerException("Impossible to set Film.synopsis to null");
        }else {
            this.synopsis = synopsis;
        }
    }

    public String[] getPlotKeywords() {
        return plotKeywords;
    }

    public void setPlotKeywords(String[] plotKeywords) {
        if (plotKeywords == null || plotKeywords.length == 0){
            throw new NullPointerException("Impossible to set Film.plotKeywords to null");
        }else {
            this.plotKeywords = plotKeywords;
        }
    }

    public String getOriginCountry() {
        return originCountry;
    }

    public void setOriginCountry(String originCountry) {
        //OriginCountry attribute may not be found. But it is an optional attribute
        if (originCountry == null){
            this.originCountry = "";
        }else {
            this.originCountry = originCountry;
        }
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) throws InvalidPropertiesFormatException {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        if (releaseYear < Film.FIRST_FILM_RELEASE_YEAR || releaseYear > cal.get(Calendar.YEAR)){
            throw new InvalidPropertiesFormatException("Invalid Film.releaseYear value (out of bounds)");
        }else {
            this.releaseYear = releaseYear;
        }
    }

    public String[] getPrimaryLanguages() {
        return primaryLanguages;
    }

    public void setPrimaryLanguages(String[] primaryLanguages) {
        //PrimaryLanguages attribute may not be found. But it is an optional attribute
        if (primaryLanguages == null){
            this.primaryLanguages = new String[]{""};
        }else {
            this.primaryLanguages = primaryLanguages;
        }
    }

    public String[] getFilmingLocations() {
        return filmingLocations;
    }

    public void setFilmingLocations(String[] filmingLocations) {
        //FilmingLocations attribute may not be found. But it is an optional attribute
        if (filmingLocations == null){
            this.filmingLocations = new String[]{""};
        }else {
            this.filmingLocations = filmingLocations;
        }
    }

    // Class methods
    public String toJson() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this, Film.class);

    }

    /**
     * Tries to scrap from the web site (filmURL) the attributes of the film that could not be scrapped from the excel file.
     * Those attributes must be initialized, because they cannot be obtained from the {@link WebScraper}
     *
     * @see Film#checkExcelValues()
     */
    public void initializeUnsetAttributes() {
        // First check the pre-required attributes of the Film
        if (checkExcelValues()) {
            try {
                //If the pre-required attributes are OK, then scrap the remaining attributes from the film's web site.
                WebScraper webScraper = new WebScraper(this.url);

            /*Sometimes the title (obtained from the excel) contains also the release year.
            Check if the year of this film could be extracted from the excel*/
                if (releaseYear == DEFAULT_VALUE) {
                    this.releaseYear = webScraper.getReleaseYear();
                }

                //The rest of the attributes have to be scraped always
                this.setMainActors(webScraper.getMainActors());
                this.setSynopsis(webScraper.getSynopsis());
                this.setPlotKeywords(webScraper.getPlotKeywords());
                this.setFilmingLocations(webScraper.getFilmingLocations());
                this.setOriginCountry(webScraper.getCountryOfOrigin());
                this.setPrimaryLanguages(webScraper.getPrimaryLanguages());

                //All the fields could be correctly set
                this.status.setStatusCompleted();
            } catch (Exception e) {
                if ((e instanceof UnknownHostException)){
                    this.status.setServerError();
                }else{
                    this.status.setStatusError(e.toString());
                }
                e.printStackTrace();
            }
        }
    }

    /**
     * Check that the attributes, found in the IMDb excel file, has been correctly set.
     * The attributes that can be found in the excel are {@link Film#imdbID}, {@link Film#url},
     *  {@link Film#title}, {@link Film#avgScore}, {@link Film#genres}
     *
     * @return True if all the corresponding attributes are properly initialized. False otherwise
     */
    private boolean checkExcelValues() {
        return ((this.imdbID != DEFAULT_VALUE) && (!this.url.isEmpty()) && (!this.title.isEmpty()) && (this.avgScore != DEFAULT_VALUE_F) && (this.genres.length >= 1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return imdbID == film.imdbID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(imdbID);
    }
}
