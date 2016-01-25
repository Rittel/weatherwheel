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

import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by Rittel on 15.05.2015.
 */
public class TimeZoneUtils {
    private Context context;

    public TimeZoneUtils(Context context) {
        this.context = context;
    }


    public void getTimeZone(double lat, double lng, final TimeZoneCallback callback) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://api.geonames.org/timezoneJSON?lat=" + lat + "&lng=" + lng + "&username=rittel";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String test = response;
                        JSONObject jObj = null;
                        try {
                            jObj = new JSONObject(test);
                            String timezone = jObj.getString("timezoneId");
                            TimeZone zone = TimeZone.getTimeZone(timezone);
                            callback.setTimezone(zone);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

    public static final long getTimeInMillis(GregorianCalendar calendar) {
        int offset = calendar.getTimeZone().getRawOffset();
        return calendar.getTimeInMillis() + offset;
    }

}
