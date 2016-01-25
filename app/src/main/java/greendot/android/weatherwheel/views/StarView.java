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
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

import greendot.android.weatherwheel.R;
import greendot.android.weatherwheel.WeatherActivity;
import greendot.android.weatherwheel.WeatherWheelApplication;

/**
 * Created by Rittel on 21.06.2015.
 */
public class StarView extends View {

    public static final int STAR_MAX_SPAWN_FAILS = 10;
    private boolean animate = false;
    private double percentageToNoon;
    private static final int SINGLE_STAR_DRAWABLE_ID = R.drawable.star;
    private Bitmap singleStarBitmap;
    private int SINGLE_STAR_HEIGHT;
    private int SINGLE_STAR_WIDTH;

    private ArrayList<Star> stars = new ArrayList<>();

    private static final float MAX_Y_DISTANCE = 0.8f;
    private static final float NEGATIVE_Y_DISTANCE = 0.2f;
    private static final int STAR_MIN_DISTANCE = 200;
    private static final int STAR_SPAWN_DEGREES = 140;
    private static final float STAR_BASE_SCALE = 0.8f;
    private static final float STAR_SCALE_DIFF = 0.4f;

    private Matrix starMatrix;
    private int screenX;
    private int screenY;
    private Paint starPaint = new Paint();
    private static Random r = new Random();
    private float scale;

    public StarView(Context context) {
        super(context);
    }

    public StarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void initialize(int screenX, int screenY) {


        Drawable singleStarDrawable = getResources().getDrawable(SINGLE_STAR_DRAWABLE_ID);
        singleStarBitmap = BitmapFactory.decodeResource(WeatherWheelApplication.getAppContext().getResources(), SINGLE_STAR_DRAWABLE_ID);

        SINGLE_STAR_WIDTH = singleStarDrawable.getIntrinsicWidth();
        SINGLE_STAR_HEIGHT = singleStarDrawable.getIntrinsicHeight();


        this.screenX = screenX;
        this.screenY = screenY;


        float scaleX = (float) screenX / WeatherWheelApplication.BASE_SCALEX;
        float scaleY = (float) screenY / WeatherWheelApplication.BASE_SCALEY;

        scale = scaleX < scaleY ? scaleX : scaleY;

        animate = true;
        starMatrix = new Matrix();
        stars.clear();
        initializeStars();
    }

    public void update(double v) {
        percentageToNoon = v;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!animate) {

            return;
        }

        drawStars(canvas);

    }

    private void drawStars(Canvas canvas) {
        for (Star star : stars) {
            star.draw(canvas);
        }
    }

    private void initializeStars() {
        int failed = 0;
        while (failed < STAR_MAX_SPAWN_FAILS) {
            int y = (int) (r.nextInt((int) (screenX * (MAX_Y_DISTANCE + NEGATIVE_Y_DISTANCE)) + 1) - NEGATIVE_Y_DISTANCE * screenX);
            float degrees = r.nextFloat() * STAR_SPAWN_DEGREES - STAR_SPAWN_DEGREES / 2;
            Star nStar = new Star(y, degrees);
            if (checkStarPositions(nStar)) {
                stars.add(nStar);
            } else {
                failed++;
            }
        }
    }

    private boolean checkStarPositions(Star nStar) {
        for (Star star : stars) {
            if (star.distanceToStar(nStar) < STAR_MIN_DISTANCE) {
                return false;
            }
        }
        return true;
    }


    private class Star {
        int posY;
        float degrees;
        float ownRotation;
        float localeScale;

        public int getPosY() {
            return posY;
        }

        public void setPosY(int posY) {
            this.posY = posY;
        }

        public float getDegrees() {
            return degrees;
        }

        public void setDegrees(float degrees) {
            this.degrees = degrees;
        }

        public Star(int posY, float degrees) {
            this.posY = posY;
            this.degrees = degrees;
            ownRotation = r.nextFloat() * 360;
            localeScale = r.nextFloat() * STAR_SCALE_DIFF + (1 - STAR_SCALE_DIFF / 2) * STAR_BASE_SCALE;
        }

        public double distanceToStar(Star star) {
            double xDiff = Math.sin(Math.toRadians(degrees)) * (screenY - posY) - Math.sin(Math.toRadians(star.degrees)) * (screenY - star.posY);
            double yDiff = Math.cos(Math.toRadians(degrees)) * (screenY - posY) - Math.cos(Math.toRadians(star.degrees)) * (screenY - star.posY);

            return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
        }

        public void draw(Canvas canvas) {
            starMatrix.setRotate(ownRotation, SINGLE_STAR_WIDTH / 2, SINGLE_STAR_HEIGHT / 2);
            starMatrix.postScale(scale * localeScale, scale * localeScale);
            starMatrix.postTranslate(screenX / 2, posY);
            starMatrix.postRotate((float) (percentageToNoon * 180 + 180) % 360 + degrees, screenX / 2, screenY);
            canvas.drawBitmap(singleStarBitmap, starMatrix, starPaint);
        }
    }
}
