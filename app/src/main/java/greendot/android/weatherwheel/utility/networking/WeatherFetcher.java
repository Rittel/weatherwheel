package greendot.android.weatherwheel.utility.networking;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import greendot.android.weatherwheel.domain.Location;
import greendot.android.weatherwheel.domain.Weather;

/**
 * Created by Rittel on 06.03.2015.
 */
public class WeatherFetcher extends OpenWeatherMapsBaseFetcher implements TimeZoneCallback, SunUpDownCallback {

    private Context context;

    private Weather weather = null;
    private WeatherCallback callback;
    private GpsLocationCallBack gpsLocationCallBack;
    private String weatherString;


    private static final String TOWNQUERY = "q=";
    private static final String LATQUERY = "lat=";
    private static final String LONQUERY = "lon=";
    private static final String IDQUERY = "id=";


    private static Map<String, Weather> nameMap = new HashMap<>();
    private static Map<String, Weather> longLatMap = new HashMap<>();
    private static Map<Integer, Weather> idMap = new HashMap<>();


    private int timeZoneFetchTries = 0;
    private static final int MAX_TIME_ZONE_TRIES = 2;


    private int sunFetchTries = 0;
    private static final int MAX_SUN_FETCH_TRIES = 2;


    public WeatherFetcher(Context context) {
        this.context = context;
    }

    public static Weather getWeather(String location) {
        return nameMap.get(location);
    }

    public static Weather getWeather(Integer id) {
        return idMap.get(id);
    }

    public void getWeather(String townName, final WeatherCallback callback) {
        if (nameMap.containsKey(townName)) {
            callback.setWeather(nameMap.get(townName));
            return;
        }
        this.callback = callback;
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        String townEncoded = encodeLocation(townName);
        String url = QUERY_START + TOWNQUERY + townEncoded + QUERY_END;
        weather = null;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String msg = jsonObject.getString("message");
                            if (msg.startsWith("Error")) {
                                Log.e("WeatherWheel", "City not found");
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        weatherString = response;
                        weather = new Weather();
                        weather = WeatherBuilder.insertLocation(weather, weatherString);
                        Location loc = weather.getLocation();

                        new TimeZoneUtils(context).getTimeZone(loc.getLatitude(), loc.getLongitude(), WeatherFetcher.this);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("WEATHERWHEEL", error.getMessage() + "Error");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void getWeather(Location location, WeatherCallback callback) {
        if (location.getID() == null) {

        }
        getWeather(location.getID(), callback);
    }

    public void getWeather(Integer id, final WeatherCallback callback) {
        if (idMap.containsKey(id)) {
            callback.setWeather(idMap.get(id));
            return;
        }
        this.callback = callback;
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = QUERY_START + IDQUERY + id + QUERY_END;
        weather = null;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String msg = jsonObject.getString("message");
                            if (msg.equalsIgnoreCase("Error: Not found city")) {
                                Log.e("WeatherWheel", "City not found");
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        weatherString = response;
                        weather = new Weather();
                        weather = WeatherBuilder.insertLocation(weather, weatherString);
                        Location loc = weather.getLocation();

                        new TimeZoneUtils(context).getTimeZone(loc.getLatitude(), loc.getLongitude(), WeatherFetcher.this);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("WEATHERWHEEL", error.getMessage() + "Error");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void getLocation(double lon, double lat, final GpsLocationCallBack callBack) {
        String url = QUERY_START + LATQUERY + lat + "&" + LONQUERY + lon + QUERY_END;
        this.gpsLocationCallBack = callBack;
        getLocation(url);

    }


    public void getLocation(String townName, final GpsLocationCallBack callback) {
        if (nameMap.containsKey(townName)) {
            callback.returnLocation(nameMap.get(townName).getLocation());
            return;
        }
        this.gpsLocationCallBack = callback;
        String townEncoded = encodeLocation(townName);

        String url = QUERY_START + TOWNQUERY + townEncoded + QUERY_END;
        getLocation(url);
    }

    private static String encodeLocation(String location) {
        String townEncoded = "";
        try {
            townEncoded = URLEncoder.encode(location, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return townEncoded;

    }

    private void getLocation(String requestString) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = requestString;
        weather = null;
        // Request a string response from the provided URL.
        final StringRequest stringRequest = createStringRequest(url);
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private StringRequest createStringRequest(final String url) {
        return new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String msg = jsonObject.getString("message");
                            if (msg.equalsIgnoreCase("Error: Not found city")) {
                                Log.e("WeatherWheel", "City not found");
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        weatherString = response;
                        weather = new Weather();
                        weather = WeatherBuilder.insertLocation(weather, weatherString);
                        Location loc = weather.getLocation();
                        gpsLocationCallBack.returnLocation(loc);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("WEATHERWHEEL", error.getMessage() + "Error");
                if (callback.getWeatherFailed()) {
                    StringRequest stringRequest = createStringRequest(url);
                    RequestQueue queue = Volley.newRequestQueue(context);
                    queue.add(stringRequest);
                }
            }
        });
    }


    @Override
    public void setTimezone(TimeZone offset) {
        weather = WeatherBuilder.insertTimeSteps(weather, offset, weatherString);
        new SunUpDownFetcher(context).getSunUpDown(weather.getLocation(), this);
    }


    @Override
    public void setTimeUpDown(UpDownIntermediate upDown) {
        weather.setSunUp(upDown.getUp());
        weather.setSunDown(upDown.getDown());
        WeatherCallback temp = callback;
        callback = null;

        putMaps(weather);

        temp.setWeather(weather);
    }

    @Override
    public boolean getTimeZoneFailed() {
        timeZoneFetchTries++;
        return timeZoneFetchTries <= MAX_TIME_ZONE_TRIES;
    }

    @Override
    public boolean getSunUpDownFailed() {
        sunFetchTries++;
        return sunFetchTries <= MAX_SUN_FETCH_TRIES;
    }


    private void putMaps(Weather weather) {
        nameMap.put(weather.getLocation().getCity(), weather);
        idMap.put(Integer.valueOf(weather.getLocation().getID()), weather);
    }

    private class WeatherStringRequest {

    }

}
