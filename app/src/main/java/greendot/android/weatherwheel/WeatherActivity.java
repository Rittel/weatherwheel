package greendot.android.weatherwheel;

import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import greendot.android.weatherwheel.domain.Location;
import greendot.android.weatherwheel.domain.Weather;
import greendot.android.weatherwheel.utility.AveragedStatusProvider;
import greendot.android.weatherwheel.utility.CountryRessourceProvider;
import greendot.android.weatherwheel.utility.ITickListener;
import greendot.android.weatherwheel.utility.TickProvider;
import greendot.android.weatherwheel.utility.TimeListener;
import greendot.android.weatherwheel.utility.TimeObservable;
import greendot.android.weatherwheel.utility.ViewUtils;
import greendot.android.weatherwheel.utility.WeatherItemAdapter;
import greendot.android.weatherwheel.utility.networking.WeatherCallback;
import greendot.android.weatherwheel.utility.networking.WeatherFetcher;
import greendot.android.weatherwheel.views.BackgroundManager;
import greendot.android.weatherwheel.views.MoonSunView;
import greendot.android.weatherwheel.views.WeatherView;


public class WeatherActivity extends AppCompatActivity
        implements WeatherCallback, TimeObservable {

    private static final int ADD_LOCATION_REQUEST = 1;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    private DrawerLayout mDrawerLayout;
    private WeatherItemAdapter weatherItemAdapter;
    private ListView mDrawerList;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private String townnName = "London";
    private ImageView spinnerView;
    private int screenwidth = 0;
    private int screenheight = 0;
    private Weather weather;
    private GregorianCalendar currentTime = new GregorianCalendar();
    private BackgroundManager backgroundManager;


    private View longClicked;
    private int scrollLoc;

    private static final float BASE_SPINNER_SCALE = 0.6f;
    private static final int DEFAULT_SCREEN_Y = 1920;
    private static final int DEFAULT_SCREEN_X = 1080;
    private static float SPINNER_SCREEN_SCALE;
    Matrix globeMatrix = new Matrix();

    ArrayList<Location> locations = new ArrayList<>();

    private ArrayList<TimeListener> listeners = new ArrayList<>();

    WeatherView weatherView;
    MoonSunView moonSunView;


    private int weatherFetchTries = 0;
    private static final int MAX_WEATHER_FETCH_TRIES = 2;

    ToggleButton playToggleButton;
    ITickListener playTickListener;

    //Rotation values
    float lastMoveAmount, moveAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_acitivty);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                View tempView = longClicked;
                if (longClicked != null) {
                    removeShadowedListItem();
                }
                if (tempView == null || mDrawerList.getPositionForView(tempView) != position) {
                    longClicked = view;
                    ViewUtils.animateView(
                            view.findViewById(R.id.shadowView), View.VISIBLE, 1, 400);
                    ViewUtils.animateView(findViewById(R.id.addTownButton), View.INVISIBLE, 0, 400);
                    scrollLoc = getScrollAmount();
                }
                return true;
            }
        });
        mDrawerList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int scrolly = getScrollAmount();
                if (Math.abs(scrolly - scrollLoc) > 50) {
                    scrollLoc = 0;
                    removeShadowedListItem();
                }
            }
        });

        Location tokyo = new Location();
        tokyo.setCity("Tokyo");
        locations.add(tokyo);

        Location linz = new Location();
        linz.setCity("Linz");
        locations.add(linz);

        Location sydney = new Location();
        sydney.setCity("Sydney");
        locations.add(sydney);

        Location london = new Location();
        london.setCity("London");
        locations.add(london);

        Location rome = new Location();
        rome.setCity("Rome");
        locations.add(rome);

        Location mexico = new Location();
        mexico.setCity("Mexico City");
        locations.add(mexico);

        // Set the adapter for the list view
        weatherItemAdapter = new WeatherItemAdapter(getBaseContext(), locations);
        mDrawerList.setAdapter(weatherItemAdapter);
        // Set the list's click listener
        //mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mTitle = getTitle();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenwidth = size.x;
        screenheight = size.y;
        SPINNER_SCREEN_SCALE = (float) (screenwidth) / DEFAULT_SCREEN_X;


        spinnerView = (ImageView) findViewById(R.id.spinnerView);


        new WeatherFetcher(this).getWeather(sydney.getCity(), this);


        placeSpinnerView();

        moonSunView = (MoonSunView) findViewById(R.id.moonSunView);
        weatherView = (WeatherView) findViewById(R.id.weatherView);
        backgroundManager = new BackgroundManager(this);

        View touchView = findViewById(R.id.touchView);
        touchView.setOnTouchListener(new View.OnTouchListener() {

            private float x1, x2;


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case (MotionEvent.ACTION_DOWN):
                        x1 = event.getX();
                        break;
                    case (MotionEvent.ACTION_MOVE):
                        x2 = event.getX();
                        float divX = x1 - x2;

                        //take 180 degrees as max turn and get percentage of moved over screen
                        rotateByDelta(divX);

                        break;
                    case (MotionEvent.ACTION_UP):
                        lastMoveAmount = moveAmount;
                        break;
                    default:

                }
                return true;
            }


        });
        rotateImage(0);


        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View view) {
                removeShadowedListItem();
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });

        playToggleButton = (ToggleButton) findViewById(R.id.playView);
        playToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playToggleButton.isChecked()) {
                    playRotate();
                } else {
                    pauseRotate();
                }
            }
        });
    }

    private void rotateByDelta(float divX) {
        float imageX = spinnerView.getMeasuredWidth();
        float tempAmount = lastMoveAmount + divX / imageX * 180;
        GregorianCalendar time = calcMoveAmount(tempAmount);
        setTime(time);
        if (isTimeInBounds(time)) {
            rotateImage(-tempAmount);
            moveAmount = tempAmount;
        }
    }

    private void pauseRotate() {
        TickProvider.getInstance(20).unregisterTickListener(playTickListener);
    }

    private void playRotate() {
        if (playTickListener == null) {
            playTickListener = new ITickListener() {
                @Override
                public void tick() {
                    if (currentTime.before(weather.getMaxTime())) {
                        rotateByDelta(20);
                        lastMoveAmount = moveAmount;
                    } else {
                        playToggleButton.performClick();
                    }
                }
            };
        }

        TickProvider.getInstance(20).registerTickListener(playTickListener);
    }

    private int getScrollAmount() {
        View c = mDrawerList.getChildAt(0);
        if (c == null) {
            return 0;
        }
        int scrolly = -c.getTop() + mDrawerList.getFirstVisiblePosition() * c.getHeight();
        return scrolly;
    }


    public void addLocation(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivityForResult(intent, ADD_LOCATION_REQUEST);
    }

    @Override
    public boolean getWeatherFailed() {
        weatherFetchTries++;
        return weatherFetchTries < MAX_WEATHER_FETCH_TRIES;
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        new WeatherFetcher(this).getWeather(weatherItemAdapter.getItem(position).getCity(), this);
        removeShadowedListItem();
    }

    private GregorianCalendar calcMoveAmount(float moveAmount) {
        float minutes = moveAmount / 180 * (60 * 24 / 2);

        GregorianCalendar time = new GregorianCalendar(weather.getTimeZone());
        time.add(Calendar.MINUTE, (int) minutes);

        return time;
    }

    private boolean isTimeInBounds(GregorianCalendar time) {
        return !(time.before(weather.getMinTime()) || time.after(weather.getMaxTime()));
    }

    private void setTime(GregorianCalendar time) {
        if (weather == null) {
            return; //TODO exception
        }
        GregorianCalendar tempTime = (GregorianCalendar) time.clone();
        if (tempTime.before(weather.getMinTime())) {
            tempTime.setTimeInMillis(weather.getMinTime().getTimeInMillis());
        } else if (tempTime.after(weather.getMaxTime())) {
            tempTime.setTimeInMillis(weather.getMaxTime().getTimeInMillis());
        }
        currentTime = tempTime;

        updateTimeInText();
        updateTempAndHumidity();
        notifyTimeListeners();
    }

    private void updateTimeInText() {
        TextView timeText = (TextView) findViewById(R.id.timeText);
        SimpleDateFormat format = new SimpleDateFormat("EE, KK:mm a");
        if (weather != null) {
            format.setTimeZone(weather.getTimeZone());
        }
        String text = format.format(currentTime.getTime());
        text = text.replace("am", "AM");
        text = text.replace("pm", "PM");
        timeText.setText(text);
    }

    private void updateTempAndHumidity() {
        if (weather == null) {
            return;
        }

        TextView tempText = (TextView) findViewById(R.id.temperatureText);
        TextView humidityText = (TextView) findViewById(R.id.humidityText);

        DecimalFormat df = new DecimalFormat("#0.0");
        double tempValue = AveragedStatusProvider.getTemperature(weather, currentTime);
        //double tempValue = weather.getStateForTime(currentTime).getWeatherState().getTemperature();
        tempText.setText(df.format(tempValue) + "°C");
        humidityText.setText(AveragedStatusProvider.getHumidity(weather, currentTime) + "%");

    }

    private void rotateImage(float amount) {

        float width = spinnerView.getDrawable().getIntrinsicWidth();

        spinnerView.setScaleType(ImageView.ScaleType.MATRIX);   //required
        globeMatrix.setRotate(amount, width / 2, width / 2);
        globeMatrix.postScale(BASE_SPINNER_SCALE * SPINNER_SCREEN_SCALE, BASE_SPINNER_SCALE * SPINNER_SCREEN_SCALE);
        globeMatrix.postTranslate(-((spinnerView.getDrawable().getIntrinsicHeight() * BASE_SPINNER_SCALE * SPINNER_SCREEN_SCALE / 2) - screenwidth / 2),
                screenheight - ((spinnerView.getDrawable().getIntrinsicHeight() * BASE_SPINNER_SCALE * SPINNER_SCREEN_SCALE / 2)) - screenheight / 5);
        spinnerView.setImageMatrix(globeMatrix);
    }


    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setWeather(Weather w) {
        if (w != null) {
            weatherFetchTries = 0;
            weather = w;

            int countryRessource = CountryRessourceProvider.getRessource(this, weather.getLocation().getCountry());
            setSpinnerImage(countryRessource);
            placeSpinnerView();
            weatherView.initialize(this, weather, screenwidth, screenheight);
            backgroundManager.initialize(this, findViewById(R.id.container), weather, true, screenwidth, screenheight);
            moonSunView.initialize(this, weather, screenwidth, screenheight);
            currentTime = new GregorianCalendar(w.getTimeZone());
            setTime(calcMoveAmount(0));
        } else {
            Log.d("WEATHERWHEEL", "Weather tried to be set to null");
        }
    }

    @Override
    public void add(TimeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void remove(TimeListener listener) {
        listeners.remove(listener);
    }

    public void notifyTimeListeners() {
        for (TimeListener listener : listeners) {
            listener.timeChanged(currentTime);
        }
    }


    private void placeSpinnerView() {


        ViewTreeObserver viewTreeObserver = spinnerView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                    if (currentapiVersion >= Build.VERSION_CODES.JELLY_BEAN) {
                        spinnerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        spinnerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }


                    spinnerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    int height = spinnerView.getMeasuredHeight();
                    if (height != 0) {
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) spinnerView.getLayoutParams();
                        params.bottomMargin = -(screenwidth / 2);
                        spinnerView.setLayoutParams(params);
                    }

                }
            });
        }
    }

    @Override
    protected void onPause() {
        weatherView.stopAnimation();
        super.onPause();
    }

    @Override
    protected void onResume() {
        weatherView.startAnimation();
        super.onResume();
    }

    public void setSpinnerImage(int resource) {
        spinnerView.setImageResource(resource);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == ADD_LOCATION_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Location loc;
                if ((loc = (Location) data.getExtras().getSerializable(MapsActivity.LOCATION_EXTRA)) != null) {
                    locations.add(loc);
                    weatherItemAdapter.notifyDataSetInvalidated();
                }
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            }
        }
    }

    private void removeShadowedListItem() {
        if (longClicked != null) {
            ViewUtils.animateView(
                    longClicked.findViewById(R.id.shadowView), View.INVISIBLE, 0, 400);
            ViewUtils.animateView(findViewById(R.id.addTownButton), View.VISIBLE, 1, 400);
            longClicked = null;
        }
    }
}
