import com.google.gson.Gson;

public class Film {

    private int imdbID;
    private String filmURL;
    private String posterURL;
    private String title;
    private int releaseYear;
    private float avgScore;
    private String[] genres;
    private String[] mainActors;
    private String plotSummary;
    private String[] plotKeywords;
    private String[] filmingLocations;
    private String originCountry;
    private String language;


    /**
     * Constructor that initializes the numeric attributes to -1 so that it can be known if the default value has been modified.
     * The rest of attributes are objects, thus initially they are pointing to null;
     */
    public Film() {
        this.imdbID = -1;
        this.releaseYear = -1;
        this.avgScore = -1F;
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

    public void setTitle(String title) {
        if (title.contains("(")) {
            //Separate the title and the year
            String[] sTitle = title.split("[(]");
            //Set the title
            title = sTitle[0].trim();
            //Remove white spaces, select and Set the year
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

    public String getPlotSummary() {
        return plotSummary;
    }

    public void setPlotSummary(String plotSummary) {
        this.plotSummary = plotSummary;
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

    public String toJson() {
        return new Gson().toJson(this, Film.class);

    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String[] getFilmingLocations() {
        return filmingLocations;
    }

    public void setFilmingLocations(String[] filmingLocations) {
        this.filmingLocations = filmingLocations;
    }
}
