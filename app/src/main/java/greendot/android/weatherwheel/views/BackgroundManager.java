package greendot.android.weatherwheel.views;

import android.content.Context;
import android.view.View;

import java.util.Calendar;
import java.util.GregorianCalendar;

import greendot.android.weatherwheel.R;
import greendot.android.weatherwheel.WeatherActivity;
import greendot.android.weatherwheel.WeatherWheelApplication;
import greendot.android.weatherwheel.domain.Weather;
import greendot.android.weatherwheel.utility.TimeListener;
import greendot.android.weatherwheel.utility.TimeObservable;

/**
 * Created by Rittel on 16.05.2015.
 */
public class BackgroundManager implements TimeListener {

    private TimeObservable observable;
    private Context context;
    private GregorianCalendar currentTime;
    private Weather weather;
    private TransparentableView blueBackGround;
    private TransparentableView blackBackGround;
    private StarView starView;
    private View darkView;


    private static int counter = 0;
    private int id;

    private static final int MINUTES_PER_DAY = 24 * 60;
    private static final int MAX_ALPHA_BLACK = 100;

    //default schowing stars is off
    private boolean showStars;
    private float scaleY;
    private float scaleX;
    private int screenX;
    private int screenY;


    public BackgroundManager(Context context) {
        this.context = context;
        id = counter;
        counter++;
    }


    public void initialize(WeatherActivity activity, View parentView, Weather weather, boolean showStars, int screenX, int screenY) {
        initializeTimeFixed(parentView, weather, showStars, screenX, screenY);


        register(activity);
    }

    public void initializeTimeFixed(View parentView, Weather weather, boolean showStars, int screenX, int screenY) {

        this.weather = weather;
        scaleX = screenX / WeatherWheelApplication.BASE_SCALEX;
        scaleY = screenY / WeatherWheelApplication.BASE_SCALEY;

        this.screenX = screenX;
        this.screenY = screenY;


        this.weather = weather;
        blueBackGround = (TransparentableView) parentView.findViewById(R.id.blueBackGroundView);
        blackBackGround = (TransparentableView) parentView.findViewById(R.id.blackBackGroundView);
        currentTime = new GregorianCalendar(weather.getTimeZone());

        //starview initialization
        this.showStars = showStars;
        if (showStars) {
            starView = (StarView) parentView.findViewById(R.id.starView);
            starView.initialize(screenX, screenY);
        }

        blueBackGround.init(context.getResources().getColor(R.color.dark_blue));
        blackBackGround.init(context.getResources().getColor(R.color.black));
        if (observable != null) {
            unregister(observable);
        }

        update();
    }

    @Override
    public void timeChanged(GregorianCalendar time) {
        currentTime = (GregorianCalendar) time.clone();
        if (weather != null) {
            currentTime.setTimeZone(weather.getTimeZone());
        }
        update();
    }


    private void update() {
        updateColor();
        updateDark();
        updateStars();
    }

    private void updateDark() {
        blackBackGround.update(getCurrentAlphaValue(MAX_ALPHA_BLACK));
    }

    private void updateStars() {
        if (!showStars) {
            return;
        }
        starView.update(percentageToNoon());

    }

    private void updateColor() {
        blueBackGround.update(getCurrentAlphaValue(255));
    }


    public void register(TimeObservable observable) {
        observable.add(this);
        this.observable = observable;
    }

    public void unregister(TimeObservable observable) {
        observable.remove(this);
    }

    public int getCurrentAlphaValue(int max) {

        return (int) (Math.abs(percentageToNoon()) * max);
    }

    public double percentageToNoon() {
        if (currentTime == null) {
            return 0;
        }
        int minute = currentTime.get(Calendar.HOUR_OF_DAY) * 60 + currentTime.get(Calendar.MINUTE);
        int diff = MINUTES_PER_DAY / 2 - minute;
        return (double) diff / (MINUTES_PER_DAY / 2);
    }


    public void setShowStars(boolean showStars) {
        this.showStars = showStars;
    }
}
