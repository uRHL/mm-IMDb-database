package filmdb.entities;

public class ScrapStatus {
    public static final int ERROR_STATUS = 0;
    public static final int NOT_COMPLETED_STATUS = 1;
    public static final int COMPLETED_STATUS = 2;

    private static final String DEFAULT_MESSAGE = "Scrapping not completed";
    private static final String OK_MESSAGE = "Scrapping completed successfully";
    private static final String SERVER_DOWN_MSG = "access denied or Server down";

    private int statusCode;
    private String message;

    /**
     * Default constructor. Initializes status to {@link ScrapStatus#NOT_COMPLETED_STATUS}
     *
     * @see ScrapStatus#setStatusUncompleted()
     */
    public ScrapStatus() {
        this.setStatusUncompleted();
    }

    //Getters & Setters
    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    // Class methods

    /**
     * Sets the status to {@link ScrapStatus#COMPLETED_STATUS}
     */
    public void setStatusCompleted() {
        this.statusCode = ScrapStatus.COMPLETED_STATUS;
        this.message = ScrapStatus.OK_MESSAGE;
    }

    /**
     * Sets the status to {@link ScrapStatus#NOT_COMPLETED_STATUS}
     */
    public void setStatusUncompleted() {
        this.statusCode = ScrapStatus.NOT_COMPLETED_STATUS;
        this.message = ScrapStatus.DEFAULT_MESSAGE;
    }

    /**
     * Sets the status to {@link ScrapStatus#ERROR_STATUS} with the default error message
     */
    public void setServerError() {
        this.statusCode = ScrapStatus.ERROR_STATUS;
        this.message = ScrapStatus.SERVER_DOWN_MSG;
    }

    /**
     * Sets the status to {@link ScrapStatus#ERROR_STATUS} with its corresponding error message
     *
     * @param errorMessage Message of the exception raised when scrapping film's data
     */
    public void setStatusError(String errorMessage) {
        this.statusCode = ScrapStatus.ERROR_STATUS;
        this.message = errorMessage;
    }

    /**
     * Checks the status of the {@link ScrapStatus}
     *
     * @return True if the {@link ScrapStatus#statusCode} equals {@link ScrapStatus#COMPLETED_STATUS}
     */
    public boolean isCompleted() {
        return (this.statusCode == ScrapStatus.COMPLETED_STATUS);
    }

    /**
     * Checks the status of the {@link ScrapStatus}
     *
     * @return True if the {@link ScrapStatus#statusCode} equals {@link ScrapStatus#ERROR_STATUS}
     */
    public boolean isError() {
        return (this.statusCode == ScrapStatus.COMPLETED_STATUS);
    }

    public boolean isServerError() {
        return (this.message.equals(ScrapStatus.SERVER_DOWN_MSG));
    }
}
