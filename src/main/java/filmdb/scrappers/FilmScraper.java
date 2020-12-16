package filmdb.scrappers;

import filmdb.entities.Film;
import filmdb.entities.ScrapStatus;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FilmScraper {
    //Class parameters
    private static final String BULK_TASKS_FILE = "bulkTasks.json";
    private static final String SCRAP_LOG = "scrappingApp.log";
    private static final double AVG_SCRAP_TIME = 5.7; // measured in seconds

    // Attributes
    private int successfulScraps;
    private int uncompletedScraps;
    private int failedScraps;
    private int totalScraps;
    private long totalScrapTime;
    private int scrappingProgress;
    private final long initDate;
    private final ArrayList<Film> scrappedFilms;
    private ArrayList<Integer> notScrappedFilms;
    private final FileOutputStream outputStream = new FileOutputStream(FilmScraper.SCRAP_LOG, true);

    /**
     * Constructor to initialize a new {@link FilmScraper} that will scrap the films contained in the specified excel file
     *
     * @param imdbDataExcelFile Route to the excel file containing imdb film data
     * @throws Exception Raised if the initial data load could not be completed
     */
    public FilmScraper(String imdbDataExcelFile, int startIndex, int totalFilmsToScrap) throws Exception {
        this.successfulScraps = 0;
        this.uncompletedScraps = 0;
        this.failedScraps = 0;
        this.totalScraps = 0;
        this.totalScrapTime = 0;
        this.scrappingProgress = 0;
        this.initDate = System.currentTimeMillis();
        this.scrappedFilms = new ArrayList<>();
        this.notScrappedFilms = new ArrayList<>();

        this.writeInitialStats(imdbDataExcelFile);
        this.scrappedFilms.addAll(new ExcelScraper(imdbDataExcelFile).scrapExcel(startIndex, totalFilmsToScrap, this.outputStream));
        if (this.scrappedFilms.isEmpty()) {
            throw new Exception("The excel format is not correct");
        }
        this.totalScraps = this.scrappedFilms.size();
    }

    /**
     * Prints the final statistics and closes the {@link FilmScraper#outputStream}
     *
     * @throws IOException Raised if the {@link FileOutputStream} could not be closed
     */
    public void close() throws IOException {
        this.writeFinalStats();
        this.outputStream.close();
    }

    /**
     * Scraps the remaining {@link Film} attributes that the excel could not set
     *
     * @param film {@link Film} initialized with IMDb Excel data
     */
    private void scrapRemainingAttr(Film film) {
        long start = System.currentTimeMillis();
        film.initializeUnsetAttributes();
        this.writeFilmScrappingStats(film.getImdbID(), System.currentTimeMillis() - start, film.getStatus());
        this.updateScrappingProgress();
        if (!film.getStatus().isCompleted()) {
            this.notScrappedFilms.add(film.getImdbID());
        }
    }

    /**
     * Provides the ratio between the specified parameter and {@link FilmScraper#totalScraps}
     *
     * @return A String containing the stats of the {@link FilmScraper} with the format "#successful/#total"
     */
    private String getOverallRatio(int numerator) {
        return numerator + "/" + this.totalScraps;
    }

    /**
     * Updates the {@link FilmScraper#scrappingProgress}. The value is only modified when the progress has
     * increased, at least, 1%. Additionally prints, in the standard output, the current progress of the scrapping process
     */
    private void updateScrappingProgress() {

        double currentProgress = ((double)(this.successfulScraps + this.uncompletedScraps + this.failedScraps) / this.totalScraps) * 100;
        if (currentProgress > this.scrappingProgress) {
            this.scrappingProgress++;
            System.out.println("****** Scrapping progress: " + this.scrappingProgress + "%");
        }
    }

    /**
     * Prints, in the standard output, the estimated execution time of the current scrapping process
     *
     * @param filmsToScrap Number of films to be scrapped
     */
    private void printEstimatedExecutionTime(int filmsToScrap) {
        int numCores = Runtime.getRuntime().availableProcessors();
        double dExeTime = AVG_SCRAP_TIME * filmsToScrap;
        if (filmsToScrap > 1) {
            //When more than one film is to be scrapped, the process is parallelized using all the available cores of the CPU
            dExeTime /= numCores;
        }

        String sExeTime = "****** New Scrapping process started at " + new Date(System.currentTimeMillis()) + "\r\n****** Estimated execution time: ";

        if (dExeTime / 60 < 0) { //less than one minute
            sExeTime += dExeTime + " seconds";
        } else {
            sExeTime += String.format("%.2f", dExeTime / 60) + " minutes";
        }
        System.out.println(sExeTime);
    }

    /**
     * Scraps all the information of the films parsed from the IMDb data excel. The scrapping process is
     * parallelized using all the available CPU cores
     */
    public void scrapAllFilms() {
        long start = System.currentTimeMillis();
        try {
            this.printEstimatedExecutionTime(this.totalScraps);
            this.outputStream.write(("-----|STARTING WEB SCRAPPING|-----\r\n").getBytes(StandardCharsets.UTF_8));
            this.outputStream.write(("--------|Mode: full scrap|--------\r\n").getBytes(StandardCharsets.UTF_8));
            this.scrappedFilms.parallelStream().forEach(this::scrapRemainingAttr);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Try to write the statistics into the Log
            this.writeWebScrappingStats(System.currentTimeMillis() - start);
        }

    }

    /**
     * Scraps a single film given its index in the {@link FilmScraper#scrappedFilms}
     *
     * @param imdbID Id of the film to be scrapped
     */
    private void scrapFilmByImdbID(int imdbID) {
        long start = System.currentTimeMillis();
        try {
            if (!this.scrappedFilms.contains(new Film(imdbID))) {
                throw new IndexOutOfBoundsException("Film " + imdbID + " not found in the list");
            }
            //Find the film to be scrapped
            for (Film film : this.scrappedFilms) {
                if (film.getImdbID() == imdbID) {
                    this.scrapRemainingAttr(film);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Scraps a single film given its index in the {@link FilmScraper#scrappedFilms}
     *
     * @param imdbID Id of the film to be scrapped
     * @see FilmScraper#scrapFilmByImdbID(int)
     */
    public void scrapSingleFilm(int imdbID) {
        long start = System.currentTimeMillis();
        try {
            this.printEstimatedExecutionTime(1);
            this.outputStream.write(("-----|STARTING WEB SCRAPPING|-----\r\n").getBytes(StandardCharsets.UTF_8));
            this.outputStream.write(("-------|Mode: single scrap|-------\r\n").getBytes(StandardCharsets.UTF_8));
            this.scrapFilmByImdbID(imdbID);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Try to write the statistics into the Log
            this.writeWebScrappingStats(System.currentTimeMillis() - start);

        }
    }

    /**
     * Scraps a given set of films. The scrapping process is parallelized using all the available CPU cores
     *
     * @param list List containing the imdbIDs of the films to be scrapped
     * @throws Exception Raised when scrapping the attributes from the web
     */
    public void scrapFilmSet(List<Integer> list) {

        long start = System.currentTimeMillis();
        try {
            this.printEstimatedExecutionTime(list.size());
            this.outputStream.write(("-----|STARTING WEB SCRAPPING|-----\r\n").getBytes(StandardCharsets.UTF_8));
            this.outputStream.write(("------|Mode: partial scrap|-------\r\n").getBytes(StandardCharsets.UTF_8));

            list.parallelStream().forEach(this::scrapFilmByImdbID);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Try to write the statistics into the Log
            this.writeWebScrappingStats(System.currentTimeMillis() - start);
        }
    }


    /**
     * Adds to the {@link FilmScraper#SCRAP_LOG} the configuration of the current {@link FilmScraper}
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
     * Adds to the {@link FilmScraper#SCRAP_LOG} the statistics obtained from scrapping all the available films
     *
     * @param timelapse time elapses scrapping the necessary information
     * @return True if the statistics could be written in the log. False otherwise
     */
    private boolean writeWebScrappingStats(long timelapse) {
        boolean result = false;
        //Try to write the statistics into the Log
        try {
            double avgScrapTime;
            if (this.successfulScraps <= 0) {
                avgScrapTime = 0;
            } else {
                avgScrapTime = (this.totalScrapTime / this.successfulScraps) / 1000.0;
            }

            this.outputStream.write(("-------------------------------------\r\n").getBytes(StandardCharsets.UTF_8));
            this.outputStream.write(("-----|SCRAPPING STATISTICS|----------\r\n").getBytes(StandardCharsets.UTF_8));
            byte[] strToBytes = ("Total scrapping time: " + (timelapse / 1000 / 60) + " minutes\r\n").getBytes(StandardCharsets.UTF_8);
            this.outputStream.write(strToBytes);
            strToBytes = ("Average time per scrap: " + avgScrapTime + " seconds\r\n").getBytes(StandardCharsets.UTF_8);
            this.outputStream.write(strToBytes);
            strToBytes = ("Successful scraps: " + this.getOverallRatio(successfulScraps) + "\r\n").getBytes(StandardCharsets.UTF_8);
            this.outputStream.write(strToBytes);
            strToBytes = ("Uncompleted scraps: " + this.getOverallRatio(uncompletedScraps) + "\r\n").getBytes(StandardCharsets.UTF_8);
            this.outputStream.write(strToBytes);
            strToBytes = ("Failed scraps: " + this.getOverallRatio(failedScraps) + "\r\n").getBytes(StandardCharsets.UTF_8);
            this.outputStream.write(strToBytes);
            strToBytes = ("Non-scrapped films: " + this.notScrappedFilms + "\r\n").getBytes(StandardCharsets.UTF_8);
            this.outputStream.write(strToBytes);

            this.outputStream.write(("-------------------------------------\r\n").getBytes(StandardCharsets.UTF_8));
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Adds to the {@link FilmScraper#SCRAP_LOG} the statistics obtained from scrapping a single film
     *
     * @param filmID    id of the film whose scrapping statistics are printed
     * @param timelapse time elapses scrapping the necessary information
     * @param status    status of the scrapping. OK if the operation was successful. Otherwise contains the reference to exception caused
     * @return True if the statistics could be written in the log. False otherwise
     */
    private boolean writeFilmScrappingStats(int filmID, long timelapse, ScrapStatus status) {
        boolean result = false;
        try {
            switch (status.getStatusCode()) {
                case ScrapStatus.COMPLETED_STATUS:
                    this.successfulScraps++;
                    outputStream.write(("Scrapped film: " + filmID + " (" + (timelapse / 1000) + " seconds)\r\n").getBytes(StandardCharsets.UTF_8));
                    break;
                case ScrapStatus.ERROR_STATUS:
                    this.failedScraps++;
                    outputStream.write(("ERROR scrapping film: " + filmID + " (ref: " + status.getMessage() + ")\r\n").getBytes(StandardCharsets.UTF_8));
                    break;
                case ScrapStatus.NOT_COMPLETED_STATUS:
                    this.uncompletedScraps++;
                    outputStream.write(("INCOMPLETE scrapping film: " + filmID + " (ref: " + status.getMessage() + ")\r\n").getBytes(StandardCharsets.UTF_8));
                    break;
            }
            this.totalScrapTime += timelapse;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Adds to the {@link FilmScraper#SCRAP_LOG} the final statistics obtained after the app has scrapped
     * all the films and has generated the correspondent files
     *
     * @return True if the statistics could be written in the log. False otherwise
     */
    private boolean writeFinalStats() {
        boolean result = false;
        try {
            this.outputStream.write(("-----|CLOSING SCRAPPING APP|----------\r\n").getBytes(StandardCharsets.UTF_8));
            this.outputStream.write(("Total execution time: " + ((System.currentTimeMillis() - initDate) / 1000 / 60) + " minutes\r\n").getBytes(StandardCharsets.UTF_8));
            this.outputStream.write(("======================================\r\n").getBytes(StandardCharsets.UTF_8));
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
                    new FileOutputStream(BULK_TASKS_FILE, true)));
            for (Film film : this.scrappedFilms) {
                if (film.getStatus().isCompleted()) {
                    out.writeBytes("{\"index\":{}}\r\n");
                    out.writeBytes(film.toJson() + "\r\n");
                    successfulWrites++;
                }
            }
            out.close();

            this.writeBulkStats((System.currentTimeMillis() - start), successfulWrites);

            System.out.println("Bulk file generated (" + (System.currentTimeMillis() - start) + " ms)");
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Adds to the {@link FilmScraper#SCRAP_LOG} the statistics obtained from creating the JSON Bulk file
     *
     * @param timelapse        time elapses scrapping the necessary information
     * @param successfulWrites number of tasks successfully written in the {@link FilmScraper#SCRAP_LOG}
     * @return True if the statistics could be written in the log. False otherwise
     */
    private boolean writeBulkStats(long timelapse, int successfulWrites) {
        boolean result = false;
        //Try to write the statistics into the Log
        try {
            this.outputStream.write(("-----|BULK TASK FILE|----------------\r\n").getBytes(StandardCharsets.UTF_8));
            byte[] strToBytes = ("Number of tasks created: " + successfulWrites + "/" + this.successfulScraps + "\r\n").getBytes(StandardCharsets.UTF_8);
            this.outputStream.write(strToBytes);
            strToBytes = ("Writing time: " + (timelapse / 1000.0) + " seconds\r\n").getBytes(StandardCharsets.UTF_8);
            this.outputStream.write(strToBytes);
            this.outputStream.write(("-------------------------------------\r\n").getBytes(StandardCharsets.UTF_8));
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
