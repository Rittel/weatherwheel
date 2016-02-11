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
import java.util.GregorianCalendar;

import greendot.android.weatherwheel.domain.Location;

/**
 * Created by Rittel on 31.05.2015.
 */
public class SunUpDownFetcher extends OpenWeatherMapsBaseFetcher {
    private Context context;

    private static final String QUERY_START =  "http://api.openweathermap.org/data/2.5/weather?q=";

    public SunUpDownFetcher(Context context) {
        this.context = context;
    }



    public void getSunUpDown(Location town, final SunUpDownCallback callback) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        String townEncoded = "";
        try {
            townEncoded = URLEncoder.encode(town.toString(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = QUERY_START + townEncoded + QUERY_END;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String test = response;
                        JSONObject jObj = null;
                        try {
                            jObj = new JSONObject(test);
                            JSONObject sysObj = jObj.getJSONObject("sys");
                            GregorianCalendar up = new GregorianCalendar();
                            GregorianCalendar down = new GregorianCalendar();
                            up.setTimeInMillis(sysObj.getLong("sunrise")*1000);
                            down.setTimeInMillis(sysObj.getLong("sunset")*1000);
                            callback.setTimeUpDown(new SunUpDownCallback.UpDownIntermediate(down, up));
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


}
