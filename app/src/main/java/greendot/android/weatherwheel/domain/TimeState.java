package greendot.android.weatherwheel.domain;

import java.util.GregorianCalendar;

/**
 * Created by Rittel on 06.03.2015.
 */
public class TimeState {

    private GregorianCalendar time;
    private WeatherState weatherState;

    public void setTime(GregorianCalendar time) {
        this.time = (GregorianCalendar) time.clone();
    }

    public void setWeatherState(WeatherState weatherState) {
        this.weatherState = weatherState;
    }

    public GregorianCalendar getTime() {
        return time;
    }

    public WeatherState getWeatherState() {
        return weatherState;
    }

    public TimeState() {

    }

    public TimeState(GregorianCalendar time, WeatherState weatherState) {
        this();
        this.time = (GregorianCalendar) time.clone();
        this.weatherState = weatherState;
    }

    public TimeState createCopy(GregorianCalendar newTime) {
        WeatherState weatherState = this.weatherState.createCopy(newTime);
        return new TimeState(newTime, weatherState);
    }



}
