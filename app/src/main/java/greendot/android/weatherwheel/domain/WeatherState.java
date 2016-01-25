package greendot.android.weatherwheel.domain;

import android.text.format.Time;

import java.util.GregorianCalendar;

/**
 * Created by Rittel on 06.03.2015.
 */
public class WeatherState {


    private double temperature;
    private String descr;
    private String condition;
    private int humidity;
    private int pressure;
    private double thunder;
    private double snow;
    private double rain;
    private double clouds;
    private double windSpeed;
    private GregorianCalendar time;
    private double windDirection;

    public WeatherState(double temperature, String descr, String condition, int humidity, int pressure, double thunder, double snow, double rain, double clouds, double windSpeed, GregorianCalendar time, double windDirection) {
        this.temperature = temperature;
        this.descr = descr;
        this.condition = condition;
        this.humidity = humidity;
        this.pressure = pressure;
        this.thunder = thunder;
        this.snow = snow;
        this.rain = rain;
        this.clouds = clouds;
        this.windSpeed = windSpeed;
        this.time = time;
        this.windDirection = windDirection;
    }

    public WeatherState(WeatherState state) {
        this(state.temperature,state.descr,state.condition,state.humidity,state.pressure,state.thunder,state.snow,state.rain,state.clouds,state.windSpeed,state.time,state.windDirection);
    }

    public WeatherState() {

    }


    public void setDescr(String descr) {
        this.descr = descr;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }


    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public void setThunder(double thunder) {
        this.thunder = thunder;
    }

    public void setSnow(double snow) {
        this.snow = snow;
    }

    public void setRain(double rain) {
        this.rain = rain;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setClouds(double clouds) {
        this.clouds = clouds;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public void setTime(GregorianCalendar time) {
        this.time = (GregorianCalendar) time.clone();
    }


    public double getClouds() {
        return clouds;
    }

    public String getCondition() {
        return condition;
    }

    public String getDescr() {
        return descr;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getPressure() {
        return pressure;
    }

    public double getRain() {
        return rain;
    }

    public double getSnow() {
        return snow;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getThunder() {
        return thunder;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public GregorianCalendar getTime() {
        return time;
    }

    public void setWindDirection(double windDirection) {
        this.windDirection = windDirection;
    }

    public double getWindDirection() {
        return windDirection;
    }

    public WeatherState createCopy(GregorianCalendar newTime) {

        WeatherState state = new WeatherState(this);
        state.setTime(newTime);
        return state;

    }
}
