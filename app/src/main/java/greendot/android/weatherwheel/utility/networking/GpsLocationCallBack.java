package greendot.android.weatherwheel.utility.networking;


import greendot.android.weatherwheel.domain.Location;

/**
 * Created by Rittel on 16.07.2015.
 */
public interface GpsLocationCallBack{
    void returnLocation(Location location);
    boolean gpsFetchFailed();
}
