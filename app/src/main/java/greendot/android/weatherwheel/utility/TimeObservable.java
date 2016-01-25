package greendot.android.weatherwheel.utility;

/**
 * Created by Rittel on 15.05.2015.
 */
public interface TimeObservable {
    void add(TimeListener listener);

    void remove(TimeListener listener);
}
