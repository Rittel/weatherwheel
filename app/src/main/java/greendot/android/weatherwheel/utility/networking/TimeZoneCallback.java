package greendot.android.weatherwheel.utility.networking;

import java.util.TimeZone;

/**
 * Created by Rittel on 24.03.2015.
 */
public interface TimeZoneCallback {

    void setTimezone(TimeZone offset);
    boolean getTimeZoneFailed();
}
