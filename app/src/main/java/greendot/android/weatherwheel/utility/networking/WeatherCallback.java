package greendot.android.weatherwheel.utility.networking;

import greendot.android.weatherwheel.domain.Weather;

/**
 * Created by Rittel on 24.03.2015.
 */
public interface WeatherCallback  {
    void setWeather(Weather w);
    boolean getWeatherFailed();
}
