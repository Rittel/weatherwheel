package greendot.android.weatherwheel.exceptions;

/**
 * Created by Rittel on 11.04.2015.
 */
public class NoSuchTimeStateException extends Exception {
    public NoSuchTimeStateException() {
    }

    public NoSuchTimeStateException(String detailMessage) {
        super(detailMessage);
    }

    public NoSuchTimeStateException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public NoSuchTimeStateException(Throwable throwable) {
        super(throwable);
    }
}
