package greendot.android.weatherwheel.utility;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import greendot.android.weatherwheel.R;
import greendot.android.weatherwheel.domain.Location;
import greendot.android.weatherwheel.domain.Weather;
import greendot.android.weatherwheel.utility.AveragedStatusProvider;
import greendot.android.weatherwheel.utility.networking.WeatherCallback;
import greendot.android.weatherwheel.utility.networking.WeatherFetcher;
import greendot.android.weatherwheel.views.BackgroundManager;
import greendot.android.weatherwheel.views.MoonSunView;
import greendot.android.weatherwheel.views.WeatherView;

/**
 * Created by Rittel on 03.06.2015.
 */
public class WeatherItemAdapter extends ArrayAdapter<Location> {

    private List<Location> locations;
    private HashMap<Integer, WeatherItemInitializer> initializers = new HashMap<>();
    private Context context;
    private static final int RESOURCE_ID = R.layout.drawer_weather_item;


    public WeatherItemAdapter(Context context, List<Location> objects) {
        super(context, RESOURCE_ID, objects);
        this.context = context;
        this.locations = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            row = inflater.inflate(RESOURCE_ID, parent, false);
        }
        TextView view = (TextView) row.findViewById(R.id.locationText);
        view.setText(locations.get(position).getCity());

        Weather w;
        if (null != (w = WeatherFetcher.getWeather(locations.get(position).toString()))) {
            initializeView(row, w);
        } else {
            initializers.put(position, new WeatherItemInitializer(row, locations.get(position)));
        }

        return row;
    }

    private void initializeView(View row, final Weather w) {
        WeatherView view = (WeatherView) row.findViewById(R.id.weatherView);
        view.initializeTimeFixed(w, row.getWidth(), row.getHeight());
        view.setCloudSpawnSpace(0.8f);
        view.setScale(0.5f);
        view.setCloudSpawnOffset(-100);

        BackgroundManager manager = new BackgroundManager(context);
        manager.initializeTimeFixed(row, w, false, row.getWidth(), row.getHeight());
        setTemperature(row, w);
        setMoonSun(row, w);

        FloatingActionButton b = (FloatingActionButton) row.findViewById(R.id.deleteButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locations.remove(w.getLocation());
                notifyDataSetInvalidated();
            }
        });
    }

    @Override
    public void notifyDataSetInvalidated() {
        initializers.clear();
        super.notifyDataSetInvalidated();
    }

    private void setTemperature(View row, Weather weather) {
        TextView tempText = (TextView) row.findViewById(R.id.temperatureText);

        DecimalFormat df = new DecimalFormat("#0.0");
        double tempValue = AveragedStatusProvider.getTemperature(weather, new GregorianCalendar(weather.getTimeZone()));
        //double tempValue = weather.getStateForTime(currentTime).getWeatherState().getTemperature();
        tempText.setText(df.format(tempValue) + "°C");
    }

    private void setMoonSun(View row, Weather weather) {
        int minutes = MoonSunView.getMinutesOfDay(new GregorianCalendar(weather.getTimeZone()));

        if (minutes < MoonSunView.getMinutesOfDay(weather.getSunUp()) || minutes > MoonSunView.getMinutesOfDay(weather.getSunDown())) {
            ImageView view = (ImageView) row.findViewById(R.id.sun_moon_view);
            view.setImageResource(R.drawable.moon_image);
        } else {
            ImageView view = (ImageView) row.findViewById(R.id.sun_moon_view);
            view.setImageResource(R.drawable.sun_image);

        }
    }


    private class WeatherItemInitializer implements WeatherCallback {
        private View view;
        private boolean isInitialized = false;

        private int weatherFetchTries = 0;
        private static final int MAX_WEATHER_FETCH_TRIES = 2;
        private Location location;

        public WeatherItemInitializer(View view, Location location) {
            this.view = view;
            this.location = location;
            new WeatherFetcher(context).getWeather(location.getCity(), this);
        }

        public void load(Weather weather) {
            isInitialized = true;
            initializeView(view, weather);
        }

        public boolean isInitialized() {
            return isInitialized;
        }

        @Override
        public void setWeather(Weather w) {
            weatherFetchTries = 0;
            if (w != null) {
                load(w);
            }
        }

        @Override
        public boolean getWeatherFailed() {
            weatherFetchTries++;
            return weatherFetchTries < MAX_WEATHER_FETCH_TRIES;
        }
    }
}
