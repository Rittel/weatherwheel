package greendot.android.weatherwheel;

import android.app.Application;
import android.content.Context;

/**
 * Created by Rittel on 30.05.2015.
 */
public class WeatherWheelApplication extends Application {

    private static Context context;

    public static Context getAppContext() {
        return context;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        context = getApplicationContext();
    }

    public static final float BASE_SCALEX = 1080;
    public static final float BASE_SCALEY = 1920;
}
