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

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Random;

import greendot.android.weatherwheel.R;
import greendot.android.weatherwheel.WeatherActivity;
import greendot.android.weatherwheel.WeatherWheelApplication;
import greendot.android.weatherwheel.domain.TimeState;
import greendot.android.weatherwheel.domain.Weather;
import greendot.android.weatherwheel.exceptions.NoSuchTimeStateException;
import greendot.android.weatherwheel.utility.ITickListener;
import greendot.android.weatherwheel.utility.TickProvider;
import greendot.android.weatherwheel.utility.TimeListener;
import greendot.android.weatherwheel.utility.TimeObservable;

/**
 * Created by Rittel on 11.04.2015.
 */
public class WeatherView extends View implements TimeListener, ITickListener {


    private Weather weather;
    private GregorianCalendar time = new GregorianCalendar();
    private TimeObservable observable;

    private static final int TICK_RATE = 15;


    // cloud values
    private double CLOUD_SPAWN_SPACE = 0.2;
    private int CLOUD_SPAWN_OFFSET = 40;
    private static final int CLOUD_DRAWABLE_ID = R.drawable.rsz_cloud;
    private static final float CLOUD_BASE_SPAWN = 1f;
    private static final int CLOUD_BASE_TICK = 5;
    private static final float CLOUD_WIND_SCALE = 1f;
    private static final float CLOUD_SCALE = 0.5f;
    private static int CLOUD_SIZE_X = 0;
    private static int CLOUD_SIZE_Y = 0;
    private int cloudTickCounter = 0;
    private static float CLOUD_SIZE_DIFF = 0.6f;
    private static float CLOUD_SPEED_DIFF = 0.4f;
    public static final int CLOUD_OPACITY = 215;
    private static final int CLOUD_OPACITY_DIFF = 40;
    private static final int CLOUD_SPAWN_MODULO = 15;

    //rain values
    private static final int RAIN_DRAWABLE_ID = R.drawable.rsz_drop;
    private static float RAIN_SIZE_DIFF = 0.4f;
    private static final int RAIN_BASE_TICK = 15;
    private static int RAIN_SIZE_X = 0;
    private static int RAIN_SIZE_Y = 0;
    private int rainTickCounter = 0;
    private static final float RAIN_SCALE = 0.8f;
    private static final float RAIN_BASE_SPAWN = 1f;
    public static final int RAIN_OPACITY = 215;
    private static final int RAIN_OPACITY_DIFF = 40;
    private static final int RAIN_SPAWN_MODULO = 15;

    //snow values
    private static final int SNOW_DRAWABLE_ID = R.drawable.rsz_snowball;
    private static float SNOW_SIZE_DIFF = 0.6f;
    private static int SNOW_SIZE_X = 0;
    private static int SNOW_SIZE_Y = 0;
    private static final int SNOW_BASE_TICK = 5;
    private int snowTickCounter = 0;
    private static final float SNOW_SCALE = 0.8f;
    private static final float SNOW_BASE_SPAWN = 1f;
    public static final int SNOW_OPACITY = 215;
    private static final int SNOW_OPACITY_DIFF = 40;
    private static final int SNOW_SPAWN_MODULO = 10;

    //additional weather values
    private double wind;

    //images
    private Drawable cloudDrawable;
    private Bitmap cloudBitmap;
    private Drawable rainDrawable;
    private Bitmap rainBitmap;
    private Drawable snowDrawable;
    private Bitmap snowBitmap;


    private boolean animate = false;


    private float scaleX;
    private float scaleY;
    private int screenX;
    private int screenY;

    private static Random random = new Random();

    private ArrayList<Cloud> cloudList = new ArrayList<Cloud>();
    private ArrayList<RainDrop> rainList = new ArrayList<>();
    private ArrayList<SnowFlake> snowList = new ArrayList<>();


    public void startAnimation() {
        animate = true;
        TickProvider.getInstance(TICK_RATE).registerTickListener(this);
        invalidate();
    }

    public void stopAnimation() {
        animate = false;
        TickProvider.getInstance(TICK_RATE).unregisterTickListener(this);
    }


    public WeatherView(Context context) {
        super(context);
    }

    public WeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WeatherView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void initialize(WeatherActivity activity, Weather weather, int screenX, int screenY) {
        initializeTimeFixed(weather, screenX, screenY);
        register(activity);
    }

    public void initializeTimeFixed(Weather weather, int screenX, int screenY) {
        this.weather = weather;
        scaleX = screenX / WeatherWheelApplication.BASE_SCALEX;
        scaleY = screenY / WeatherWheelApplication.BASE_SCALEY;

        this.screenX = screenX;
        this.screenY = screenY;

        cloudDrawable = getResources().getDrawable(CLOUD_DRAWABLE_ID);
        cloudBitmap = BitmapFactory.decodeResource(getResources(), CLOUD_DRAWABLE_ID);

        CLOUD_SIZE_X = cloudDrawable.getIntrinsicWidth();
        CLOUD_SIZE_Y = cloudDrawable.getIntrinsicHeight();


        rainDrawable = getResources().getDrawable(RAIN_DRAWABLE_ID);
        rainBitmap = BitmapFactory.decodeResource(getResources(), RAIN_DRAWABLE_ID);

        RAIN_SIZE_X = rainDrawable.getIntrinsicWidth();
        RAIN_SIZE_Y = rainDrawable.getIntrinsicHeight();

        snowDrawable = getResources().getDrawable(SNOW_DRAWABLE_ID);
        snowBitmap = BitmapFactory.decodeResource(getResources(), SNOW_DRAWABLE_ID);

        SNOW_SIZE_X = snowDrawable.getIntrinsicWidth();
        SNOW_SIZE_Y = snowDrawable.getIntrinsicHeight();

        cloudList.clear();
        rainList.clear();
        snowList.clear();

        if (observable != null) {
            unregister(observable);
        }

        time = new GregorianCalendar(weather.getTimeZone());
        startAnimation();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (weather == null || !animate) {
            super.onDraw(canvas);
            return;
        }


        drawDroppables(canvas);
        drawClouds(canvas);
        invalidate();
        super.onDraw(canvas);
    }

    private void updateThings() {
        if (weather == null) {
            return;
        }
        try {
            TimeState state = weather.getStateForTime(time);
            double clouds = state.getWeatherState().getClouds();
            double rain = state.getWeatherState().getRain();
            double snow = state.getWeatherState().getSnow();
            wind = state.getWeatherState().getWindSpeed();
            spawnClouds(clouds);
            spawnDroppables(rain, snow);
            moveClouds();
            moveDropables();
        } catch (NoSuchTimeStateException e) {
            //e.printStackTrace();
            wind = 0;
            moveClouds();
            moveDropables();
        }

    }

    private void drawClouds(Canvas canvas) {


        for (Cloud cloud : cloudList) {
            cloud.draw(canvas);
        }
    }

    private void spawnClouds(double clouds) {
        int r = random.nextInt((int) (100 * (1 / clouds)));
        if (r * CLOUD_BASE_SPAWN * clouds < clouds * 100) {
            if (cloudTickCounter == 0) {
                addCloud();
            }
            cloudTickCounter = (cloudTickCounter + 1) % CLOUD_SPAWN_MODULO;
        }
    }

    private void spawnDroppables(double rain, double snow) {
        for (Cloud cloud : cloudList) {
            if (rain > 0) {
                int r = random.nextInt((int) (100 * (1 / rain)));
                if (r * RAIN_BASE_SPAWN * rain < rain * 100) {
                    if (rainTickCounter == 0) {
                        addRain(cloud);
                    }
                    rainTickCounter = (rainTickCounter + 1) % RAIN_SPAWN_MODULO;
                }
            }
            if (snow > 0) {
                int r = random.nextInt((int) (100 * (1 / snow)));
                if (r * SNOW_BASE_SPAWN * snow < snow * 100) {
                    if (snowTickCounter == 0) {
                        addSnow(cloud);
                    }
                    snowTickCounter = (snowTickCounter + 1) % SNOW_SPAWN_MODULO;
                }
            }

        }
    }

    private void drawDroppables(Canvas canvas) {


        for (RainDrop drop : rainList) {
            drop.draw(canvas);
        }
        for (SnowFlake drop : snowList) {
            drop.draw(canvas);
        }
    }

    private void addSnow(Cloud cloud) {
        snowList.add(cloud.spawnFlake());
    }


    private void addRain(Cloud cloud) {
        rainList.add(cloud.spawnDrop());

    }


    private void moveClouds() {

        ArrayList<Integer> toRemove = new ArrayList<Integer>();

        for (int i = 0; i < cloudList.size(); i++) {
            Cloud c = cloudList.get(i);
            c.moveTick();
            if (c.getPosX() > screenX * 1.2) {
                toRemove.add(i);
            }
        }

        for (int i = 0; i < toRemove.size(); i++) {
            int i1 = toRemove.get(i);
            cloudList.remove(i1 - i);
        }
    }

    private void moveDropables() {
        ArrayList<Integer> toRemove = new ArrayList<Integer>();

        for (int i = 0; i < rainList.size(); i++) {
            RainDrop drop = rainList.get(i);
            drop.moveTick();
            if (drop.getPosY() > screenY * 1.2) {
                toRemove.add(i);
            }
        }

        for (int i = 0; i < toRemove.size(); i++) {
            int i1 = toRemove.get(i);
            rainList.remove(i1 - i);
        }

        toRemove.clear();
        for (int i = 0; i < snowList.size(); i++) {
            SnowFlake flake = snowList.get(i);
            flake.moveTick();
            if (flake.getPosY() > screenY * 1.2) {
                toRemove.add(i);
            }
        }

        for (int i = 0; i < toRemove.size(); i++) {
            int i1 = toRemove.get(i);
            snowList.remove(i1 - i);
        }
    }

    private void addCloud() {
        int r = (int) (random.nextInt((int) ((double) screenY * CLOUD_SPAWN_SPACE)) + CLOUD_SPAWN_OFFSET * scaleY);
        float v = random.nextFloat() * CLOUD_SPEED_DIFF + (1 - CLOUD_SPEED_DIFF / 2);
        cloudList.add(new Cloud((int) (-CLOUD_SIZE_X * scaleX * (CLOUD_SCALE + CLOUD_SIZE_DIFF / 2)), r, v));
    }

    @Override
    public void timeChanged(GregorianCalendar time) {
        this.time = (GregorianCalendar) time.clone();
        if (weather != null) {
            time.setTimeZone(weather.getTimeZone());
        }
    }

    public void register(TimeObservable observable) {
        observable.add(this);
        this.observable = observable;
    }

    public void unregister(TimeObservable observable) {
        observable.remove(this);
    }

    @Override
    public void tick() {
        updateThings();
    }


    private class Cloud {
        int posX;
        int posY;
        float vX;
        Matrix matrix = new Matrix();

        float scale;
        int opacity;
        Paint p = new Paint();

        public Cloud(int posX, int posY, float vX) {
            this.posX = posX;
            this.posY = posY;
            this.vX = vX;
            scale = random.nextFloat() * CLOUD_SIZE_DIFF + (1 - CLOUD_SIZE_DIFF / 2);
            opacity = CLOUD_OPACITY + random.nextInt(CLOUD_OPACITY_DIFF * 2) - CLOUD_OPACITY_DIFF;
            p.setAlpha(opacity);
        }

        public int getPosX() {
            return posX;
        }

        public void setPosX(int posX) {
            this.posX = posX;
        }

        public int getPosY() {
            return posY;
        }

        public void setPosY(int posY) {
            this.posY = posY;
        }

        public void moveTick() {
            posX += scaleX * CLOUD_BASE_TICK * vX + wind * CLOUD_WIND_SCALE;
        }

        public void draw(Canvas canvas) {

            matrix.setScale(CLOUD_SCALE * scale * scaleY, CLOUD_SCALE * scale * scaleY);
            matrix.postTranslate(posX, posY);
            canvas.drawBitmap(cloudBitmap, matrix, p);
        }


        public RainDrop spawnDrop() {
            int YSize = (int) (CLOUD_SIZE_Y * CLOUD_SCALE * scale * scaleY - RAIN_SIZE_Y * RAIN_SCALE * scale * scaleY);
            int pY = (int) (YSize * 0.75) + posY;
            int pX = random.nextInt((int) (CLOUD_SIZE_X * CLOUD_SCALE * scale * scaleX)) + posX;
            return new RainDrop(pX, pY);
        }

        public SnowFlake spawnFlake() {
            int YSize = (int) (CLOUD_SIZE_Y * CLOUD_SCALE * scale * scaleY - SNOW_SIZE_X * SNOW_SCALE * scale * 1.2f * scaleY);
            int pY = (int) (YSize * 0.75) + posY;
            int pX = random.nextInt((int) (CLOUD_SIZE_X * CLOUD_SCALE * scale * scaleX)) + posX;
            return new SnowFlake(pX, pY);
        }
    }


    private class RainDrop {
        int posX;
        int posY;
        int vY = RAIN_BASE_TICK;
        float scale;
        int opacity;
        Paint p;
        Matrix matrix = new Matrix();

        public RainDrop(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
            scale = random.nextFloat() * RAIN_SIZE_DIFF + (1 - RAIN_SIZE_DIFF / 2);
            opacity = RAIN_OPACITY + random.nextInt(RAIN_OPACITY_DIFF * 2) - RAIN_OPACITY_DIFF;
            p = new Paint();
            p.setAlpha(opacity);
        }

        public Bitmap getBitmap() {
            return rainBitmap;
        }

        public void draw(Canvas canvas) {
            matrix.setScale(RAIN_SCALE * scale * scaleY, RAIN_SCALE * scale * scaleY);
            matrix.postTranslate(posX, posY);
            canvas.drawBitmap(rainBitmap, matrix, p);
        }

        public void moveTick() {
            posY += vY * scaleY;
        }

        public int getPosX() {
            return posX;
        }

        public void setPosX(int posX) {
            this.posX = posX;
        }

        public int getPosY() {
            return posY;
        }

        public void setPosY(int posY) {
            this.posY = posY;
        }


        public int getvY() {
            return vY;
        }

        public void setvY(int vY) {
            this.vY = vY;
        }
    }

    private class SnowFlake {
        int vY = SNOW_BASE_TICK;
        float scale;
        int posX;
        int posY;
        int opacity;
        Paint p = new Paint();
        Matrix matrix = new Matrix();

        public SnowFlake(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
            scale = random.nextFloat() * SNOW_SIZE_DIFF + (1 - SNOW_SIZE_DIFF / 2);
            opacity = SNOW_OPACITY + random.nextInt(SNOW_OPACITY_DIFF * 2) - SNOW_OPACITY_DIFF;
            p.setAlpha(opacity);
        }

        public Bitmap getBitmap() {
            return snowBitmap;
        }

        public void draw(Canvas canvas) {
            matrix.setScale(SNOW_SCALE * scale * scaleY, SNOW_SCALE * scale * scaleY);
            matrix.postTranslate(posX, posY);
            canvas.drawBitmap(snowBitmap, matrix, p);
        }

        public void moveTick() {
            posY += vY * scaleY;
        }

        public int getPosX() {
            return posX;
        }

        public void setPosX(int posX) {
            this.posX = posX;
        }

        public int getPosY() {
            return posY;
        }

        public void setPosY(int posY) {
            this.posY = posY;
        }

        public int getvY() {
            return vY;
        }

        public void setvY(int vY) {
            this.vY = vY;
        }
    }


    /**
     * should only be used by the navigation bar items
     *
     * @param scale
     */
    @Deprecated
    public void setScale(float scale) {
        scaleY = scale;
        scaleX = scale;
    }

    /**
     * should only be used by the navigation bar items
     */
    @Deprecated
    public void setCloudSpawnSpace(float space) {
        this.CLOUD_SPAWN_SPACE = space;
    }

    /**
     * should only be used by the navigation bar items
     */
    @Deprecated
    public void setCloudSpawnOffset(int offset) {
        this.CLOUD_SPAWN_OFFSET = offset;
    }

}
