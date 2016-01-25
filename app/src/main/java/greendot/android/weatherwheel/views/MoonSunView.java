package greendot.android.weatherwheel.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.GregorianCalendar;

import greendot.android.weatherwheel.R;
import greendot.android.weatherwheel.WeatherActivity;
import greendot.android.weatherwheel.WeatherWheelApplication;
import greendot.android.weatherwheel.domain.Weather;
import greendot.android.weatherwheel.utility.ITickListener;
import greendot.android.weatherwheel.utility.TickProvider;
import greendot.android.weatherwheel.utility.TimeListener;
import greendot.android.weatherwheel.utility.TimeObservable;

/**
 * Created by Rittel on 30.05.2015.
 */
public class MoonSunView extends View implements TimeListener, ITickListener {


    private TimeObservable observable;

    private static final int SUN_DRAWABLE_ID = R.drawable.sun_image_points;
    private static final int MOON_DRAWABLE_ID = R.drawable.moon_image;
    private static final int MIN_HEIGHT = 1200;
    private static final int MAX_HEIGHT = 400;
    private GregorianCalendar time = new GregorianCalendar();
    private Weather weather;

    private static final int TICK_DELAY = 15;

    private float scale;
    private int screenX;
    private int screenY;


    private Drawable sunDrawable;
    private Bitmap sunBitmap;
    private int SUN_SIZE_X;
    private int SUN_SIZE_Y;
    private static final float SUN_SCALE = 0.35f;
    private final Paint p = new Paint();
    private Matrix sunMatrix = new Matrix();
    private float sunRotated = 0;
    private static final float SUN_ROTATE_TICK = 1.5f;

    private Drawable moonDrawable;
    private Bitmap moonBitmap;
    private int MOON_SIZE_X;
    private int MOON_SIZE_Y;
    private static final float MOON_SCALE = 0.35f;
    private Matrix moonMatrix = new Matrix();


    private static final int MINUTES_PER_DAY = 24 * 60;

    private boolean animate = false;
    private GregorianCalendar up;
    private GregorianCalendar down;

    public MoonSunView(Context context) {
        super(context);
    }

    public MoonSunView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MoonSunView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MoonSunView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void initialize(WeatherActivity activity, Weather weather, int screenX, int screenY) {
        initializeTimeFixed(weather, screenX, screenY);
        startAnimating();
        register(activity);
    }

    public void initializeTimeFixed(Weather weather, int screenX, int screenY) {
        this.weather = weather;
        float scaleX = (float) screenX / WeatherWheelApplication.BASE_SCALEX;
        float scaleY = (float) screenY / WeatherWheelApplication.BASE_SCALEY;

        scale = scaleX < scaleY ? scaleX : scaleY;
        this.screenX = screenX;
        this.screenY = screenY;

        sunDrawable = getResources().getDrawable(SUN_DRAWABLE_ID);
        sunBitmap = BitmapFactory.decodeResource(getResources(), SUN_DRAWABLE_ID);

        SUN_SIZE_X = sunDrawable.getIntrinsicWidth();
        SUN_SIZE_Y = sunDrawable.getIntrinsicHeight();

        moonDrawable = getResources().getDrawable(MOON_DRAWABLE_ID);
        moonBitmap = BitmapFactory.decodeResource(getResources(), MOON_DRAWABLE_ID);

        MOON_SIZE_X = moonDrawable.getIntrinsicWidth();
        MOON_SIZE_Y = moonDrawable.getIntrinsicHeight();

        time = new GregorianCalendar(weather.getTimeZone());

        up = weather.getSunUp();
        down = weather.getSunDown();
        if (observable != null) {
            unregister(observable);
        }
    }

    public void startAnimating() {
        animate = true;
        TickProvider.getInstance(TICK_DELAY).registerTickListener(this);
    }

    public void stopAnimating() {
        animate = false;
        TickProvider.getInstance(TICK_DELAY).unregisterTickListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (weather == null || !animate) {
            super.onDraw(canvas);
            return;
        }

        int minutes = getMinutesOfDay(time);

        int x = 0;
        int y = 0;
        if (minutes > getMinutesOfDay(up) && minutes < getMinutesOfDay(down)) {
            double percent = getPercent(up, down);
            x = getXPos(percent, (int) (SUN_SIZE_X * SUN_SCALE));
            y = getYPos(percent);

            sunMatrix.setRotate(sunRotated, SUN_SIZE_X / 2, SUN_SIZE_Y / 2);
            sunMatrix.postScale(SUN_SCALE * scale, SUN_SCALE * scale);
            sunMatrix.postTranslate(x, y);
            canvas.drawBitmap(sunBitmap, sunMatrix, p);
        } else {

            double percent = getPercent(down, up);
            x = getXPos(percent, (int) (MOON_SIZE_X * MOON_SCALE));
            y = getYPos(percent);

            moonMatrix.setScale(MOON_SCALE * scale, MOON_SCALE * scale);
            moonMatrix.postTranslate(x, y);
            canvas.drawBitmap(moonBitmap, moonMatrix, p);
        }

        super.onDraw(canvas);
    }

    private int getYPos(double percent) {
        return (int) (screenY - (Math.sin(percent * Math.PI) * MAX_HEIGHT * scale + MIN_HEIGHT * scale));
    }

    private int getXPos(double percent, int imageWidth) {
        return (int) (screenX + imageWidth - (screenX + imageWidth) * percent * scale - imageWidth * scale);
    }

    private double getPercent(GregorianCalendar start, GregorianCalendar end) {
        int beginMinutes = getMinutesOfDay(start);
        int endMinutes = getMinutesOfDay(end);
        int timeMinutes = getMinutesOfDay(time);
        return (((double) timeMinutes + MINUTES_PER_DAY - beginMinutes) % MINUTES_PER_DAY) /
                ((endMinutes + MINUTES_PER_DAY - beginMinutes) % MINUTES_PER_DAY);
    }

    public static int getMinutesOfDay(GregorianCalendar time) {
        int minutes = time.get(Calendar.HOUR_OF_DAY) * 60;
        minutes += time.get(Calendar.MINUTE);
        return minutes;
    }


    @Override
    public void timeChanged(GregorianCalendar time) {
        this.time = (GregorianCalendar) time.clone();
        if (weather != null) {
            time.setTimeZone(weather.getTimeZone());
        }
        invalidate();
    }

    public void register(TimeObservable observable) {
        this.observable = observable;
        observable.add(this);
    }

    public void unregister(TimeObservable observable) {
        observable.remove(this);
    }

    @Override
    public void tick() {
        sunRotated = (sunRotated + SUN_ROTATE_TICK) % 360;
        invalidate();
    }
}
