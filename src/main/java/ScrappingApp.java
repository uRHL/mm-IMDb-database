import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

public class ScrappingApp {
    //Constant used for testing purposes
    private static final String IMDb_DATA_EXCEL = "C:\\Users\\Elir Ray\\IdeaProjects\\mm-IMDb-database\\src\\imbd-data\\MovieGenreIGC_v3.xlsx";
    private static final String BULK_TASKS_FILE = "bulkTasks.json";
    public static final String SCRAP_LOG = "scrap_log.txt";

    // Attributes
    private int successfulScraps;
    private int totalScraps;
    private final ArrayList<Film> filmList;
    private final FileOutputStream outputStream = new FileOutputStream(ScrappingApp.SCRAP_LOG, true);

    /**
     * Constructor to initialize a new {@link ScrappingApp} that will scrap the films contained in the specified excel file
     *
     * @param imdbDataExcelFile Route to the excel file containing imdb film data
     * @throws Exception Raised if the initial data load could not be completed
     */
    public ScrappingApp(String imdbDataExcelFile) throws Exception {
        this.successfulScraps = 0;
        this.totalScraps = 0;
        this.filmList = new ArrayList<>();
        this.writeInitialStats(imdbDataExcelFile);
        this.scrapExcelData(imdbDataExcelFile);
        if (this.filmList.isEmpty()) {
            throw new Exception("The excel format is not correct");
        }
    }

    public static void main(String[] args) {
        try {
            ScrappingApp myScrappingApp = new ScrappingApp(IMDb_DATA_EXCEL);
            Film film = new Film();
            System.out.println(film.toJson());
            if (myScrappingApp.scrapAllFilms()) {
                myScrappingApp.generateBulkTasksFile();
                myScrappingApp.close();
            } else {
                throw new Exception("Error scrapping films data");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Frees the unused resources of the instance
     *
     * @throws IOException Raised if the {@link FileOutputStream} could not be closed
     */
    private void close() throws IOException {
        this.outputStream.close();
    }

    /**
     * Creates an {@link ExcelScraper} to obtain the film's data contained in the IMDb films excel file
     *
     * @throws Exception Raised when the scrapping could not end properly
     */
    private void scrapExcelData(String imdbDataExcel) throws Exception {
        this.filmList.addAll(new ExcelScraper().scrapExcel(imdbDataExcel));
        this.totalScraps = this.filmList.size();
    }

    /**
     * Scraps the remaining {@link Film} attributes that the excel could not set
     *
     * @param film {@link Film} initialized with IMDb Excel data
     * @return True if the scrapping was successful. False otherwise
     */
    private boolean scrapRemainingAttr(Film film) {
        boolean result = false;
        long start = System.currentTimeMillis();
        if (film.initializeUnsetAttributes()) {
            this.successfulScraps++;
            this.writeFilmScrappingStats(film.getImdbID(), System.currentTimeMillis() - start);
            result = true;
        }
        return result;
    }

    /**
     * Provides the statistical information provided by {@link ScrappingApp#successfulScraps} VS {@link ScrappingApp#totalScraps}
     *
     * @return A String containing the stats of the {@link ScrappingApp} with the format "#successful/#total"
     */
    public String getScrappingStats() {
        return this.successfulScraps + "/" + this.totalScraps;
    }

    /**
     * Scraps all the information of the films created from the IMDb data excel
     *
     * @return True if the operation was successful. False otherwise
     */
    public boolean scrapAllFilms() {
        boolean result = false;
        try {
            long start = System.currentTimeMillis();
            this.outputStream.write(("-----|STARTING WEB SCRAPPING|-----\r\n").getBytes(StandardCharsets.UTF_8));
            this.filmList.parallelStream().forEach((film) -> {
                if (this.scrapRemainingAttr(film)) {
                        /*The films attributes could be scrapped
                         Delete the film from the list*/
                    this.filmList.remove(film);
                }
            });
            this.outputStream.write(("-------------------\r\n").getBytes(StandardCharsets.UTF_8));
            //Try to write the statistics into the Log
            this.writeWebScrappingStats(System.currentTimeMillis() - start);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Scraps a single film given its index in the {@link ScrappingApp#filmList}
     *
     * @param index Index of the film to be scrapped
     * @return True if the scrapping was successful. False otherwise
     */
    public boolean scrapFilmByIndex(int index) {
        boolean result = false;

        try {
            Film film = this.filmList.get(index);
            this.scrapRemainingAttr(film);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Generates a json file containing insert Bulk tasks
     *
     * @return True if the file could be completely written
     */
    public boolean generateBulkTasksFile() {
        boolean result = false;
        try {
            int successfulWrites = 0;
            long start = System.currentTimeMillis();
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(BULK_TASKS_FILE)));
            for (Film film : this.filmList) {
                out.writeBytes("{\"index\":{}}\r\n");
                out.writeBytes(film.toJson() + "\r\n");
                successfulWrites++;
            }
            out.close();

            this.writeBulkStats((System.currentTimeMillis() - start), successfulWrites);

            System.out.println("Successful write (" + (System.currentTimeMillis() - start) + " ms)");
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Adds to the {@link ScrappingApp#SCRAP_LOG} the configuration of the current {@link ScrappingApp}
     * It specifies the date and hour at which the ScrapperApp was started and the file used to get the initial film data
     *
     * @param imdbDataExcel Route to the excel file containing the imdb film information
     * @return True if the statistics could be written in the log. False otherwise
     */
    private boolean writeInitialStats(String imdbDataExcel) {
        boolean result = false;
        try {
            this.outputStream.write(("\n\n-----|NEW SCRAPPING APP|-------------\r\n").getBytes(StandardCharsets.UTF_8));
            this.outputStream.write(("Started at: " + new Date(System.currentTimeMillis()) + "\r\n").getBytes(StandardCharsets.UTF_8));
            this.outputStream.write(("Initial data load file: " + imdbDataExcel + "\r\n").getBytes(StandardCharsets.UTF_8));
            this.outputStream.write(("-------------------------------------\r\n").getBytes(StandardCharsets.UTF_8));
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Adds to the {@link ScrappingApp#SCRAP_LOG} the statistics obtained from creating the JSON Bulk file
     *
     * @param timelapse        time elapses scrapping the necessary information
     * @param successfulWrites number of tasks successfully written in the {@link ScrappingApp#SCRAP_LOG}
     * @return True if the statistics could be written in the log. False otherwise
     */
    private boolean writeBulkStats(long timelapse, int successfulWrites) {
        boolean result = false;
        //Try to write the statistics into the Log
        try {
            this.outputStream.write(("-----|BULK TASK FILE|----------------\r\n").getBytes(StandardCharsets.UTF_8));
            byte[] strToBytes = ("Number of tasks created: " + successfulWrites + "/" + this.successfulScraps + "\r\n").getBytes(StandardCharsets.UTF_8);
            this.outputStream.write(strToBytes);
            strToBytes = ("Writing time: " + (timelapse / 1000) + " seconds)\r\n").getBytes(StandardCharsets.UTF_8);
            this.outputStream.write(strToBytes);
            this.outputStream.write(("-------------------------------------\r\n").getBytes(StandardCharsets.UTF_8));
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Adds to the {@link ScrappingApp#SCRAP_LOG} the statistics obtained from scrapping all the available films
     *
     * @param timelapse time elapses scrapping the necessary information
     * @return True if the statistics could be written in the log. False otherwise
     */
    private boolean writeWebScrappingStats(long timelapse) {
        boolean result = false;
        //Try to write the statistics into the Log
        try {
            this.outputStream.write(("-----|SCRAPPING STATISTICS|----------\r\n").getBytes(StandardCharsets.UTF_8));
            byte[] strToBytes = ("Total scrapping time: " + (timelapse / 1000 / 60) + " minutes)\r\n").getBytes(StandardCharsets.UTF_8);
            this.outputStream.write(strToBytes);
            strToBytes = ("Successful scraps: " + this.getScrappingStats() + "\r\n").getBytes(StandardCharsets.UTF_8);
            this.outputStream.write(strToBytes);
            this.outputStream.write(("-------------------------------------\r\n").getBytes(StandardCharsets.UTF_8));
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Adds to the {@link ScrappingApp#SCRAP_LOG} the statistics obtained from scrapping a single film
     *
     * @param filmID    id of the film whose scrapping statistics are printed
     * @param timelapse time elapses scrapping the necessary information
     * @return True if the statistics could be written in the log. False otherwise
     */
    private boolean writeFilmScrappingStats(int filmID, long timelapse) {
        boolean result = false;
        try {
            byte[] strToBytes = ("Scrapped film: " + filmID + " (" + (timelapse / 1000) + " seconds)\r\n").getBytes(StandardCharsets.UTF_8);
            outputStream.write(strToBytes);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
