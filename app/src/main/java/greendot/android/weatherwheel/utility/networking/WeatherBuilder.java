package greendot.android.weatherwheel.utility.networking;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import greendot.android.weatherwheel.domain.Location;
import greendot.android.weatherwheel.domain.TimeState;
import greendot.android.weatherwheel.domain.Weather;
import greendot.android.weatherwheel.domain.WeatherState;
import greendot.android.weatherwheel.utility.JSONUtils;

/**
 * Created by Rittel on 07.03.2015.
 */
public class WeatherBuilder {

    public static Weather buildWeather(String weatherString) {

        Weather weather = new Weather();
        // We create out JSONObject from the data
        try {
            JSONObject jObj = new JSONObject(weatherString);

            weather.setLocation(parseLocation(jObj));
            weather.setTimeStates(parseWeatherStates(jObj, TimeZone.getTimeZone("GMT")));
            return weather;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Weather insertLocation(Weather weather, String weatherString) {
        try {
            JSONObject jObj = new JSONObject(weatherString);

            weather.setLocation(parseLocation(jObj));
            return weather;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Weather insertTimeSteps(Weather weather, TimeZone offset, String weatherString) {
        try {
            JSONObject jObj = new JSONObject(weatherString);
            weather.setTimeZone(offset);
            weather.setTimeStates(parseWeatherStates(jObj, offset));
            return weather;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Location parseLocation(JSONObject jObj) throws JSONException {
        Location loc = new Location();

        JSONObject cityObj = JSONUtils.getObject("city", jObj);
        loc.setCountry(JSONUtils.getString("country", cityObj));
        loc.setCity(JSONUtils.getString("name", cityObj));
        loc.setID("" + JSONUtils.getInt("id", cityObj));

        JSONObject coordObj = JSONUtils.getObject("coord", cityObj);
        loc.setLatitude(JSONUtils.getDouble("lat", coordObj));
        loc.setLongitude(JSONUtils.getDouble("lon", coordObj));


        return loc;
    }

    private static ArrayList<TimeState> parseWeatherStates(JSONObject jObj, TimeZone timezone) throws JSONException {

        JSONArray jArr = jObj.getJSONArray("list");
        ArrayList<TimeState> states = new ArrayList<>();

        for (int i = 0; i < jArr.length(); i++) {
            TimeState tState = new TimeState();
            WeatherState wState = new WeatherState();

            JSONObject current = jArr.getJSONObject(i);
            JSONArray weatherArray = current.getJSONArray("weather");
            parseSubStates(wState, weatherArray);


            JSONObject mainObj = JSONUtils.getObject("main", current);
            wState.setHumidity(JSONUtils.getInt("humidity", mainObj));
            wState.setPressure(JSONUtils.getInt("pressure", mainObj));
            wState.setTemperature(JSONUtils.getDouble("temp", mainObj));

            // Wind
            JSONObject wObj = JSONUtils.getObject("wind", current);
            wState.setWindSpeed(JSONUtils.getDouble("speed", wObj));
            wState.setWindDirection(JSONUtils.getDouble("deg", wObj));


            // Clouds
            JSONObject cObj = JSONUtils.getObject("clouds", current);
            wState.setClouds((double) JSONUtils.getInt("all", cObj) / 100);


            //Time
            GregorianCalendar time = new GregorianCalendar();
            time.setTimeInMillis(((long) (JSONUtils.getInt("dt", current))) * 1000);
            time.setTimeZone(timezone);
            tState.setTime(time);

            tState.setWeatherState(wState);
            states.add(tState);
        }


        return states;

    }

    private static WeatherState parseSubStates(WeatherState state, JSONArray weather) throws JSONException {
        double thunder = 0.0;
        double snow = 0.0;
        double rain = 0.0;
        for (int i = 0; i < weather.length(); i++) {
            JSONObject current = weather.getJSONObject(i);


            int weatherId = JSONUtils.getInt("id", current);
            if (weatherId >= 200 && weatherId < 300) {
                thunder = 1.0;
                rain = 1.0;
            }
            if (weatherId >= 600 && weatherId < 700) {
                if (weatherId == 600) {
                    snow = 0.33;
                } else if (weatherId == 601) {
                    snow = 0.66;
                } else if (weatherId == 602) {
                    snow = 1.0;
                } else {
                    snow = 0.5;
                }
            }
            if (weatherId >= 500 && weatherId < 600) {
                if (weatherId == 500) {
                    rain = 0.25;
                } else if (weatherId == 501) {
                    rain = 0.5;
                } else if (weatherId == 502) {
                    rain = 0.75;
                } else if (weatherId == 503) {
                    rain = 1.0;
                } else {
                    rain = 0.5;
                }
            }
        }
        state.setThunder(thunder);
        state.setSnow(snow);
        state.setRain(rain);

        return state;
    }


}
