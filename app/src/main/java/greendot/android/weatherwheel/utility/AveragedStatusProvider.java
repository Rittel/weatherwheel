package greendot.android.weatherwheel.utility;

import java.util.Calendar;
import java.util.GregorianCalendar;

import greendot.android.weatherwheel.domain.TimeState;
import greendot.android.weatherwheel.domain.Weather;
import greendot.android.weatherwheel.exceptions.NoSuchTimeStateException;

/**
 * Created by Rittel on 30.05.2015.
 */
public class AveragedStatusProvider {

    public static double getTemperature(Weather weather, GregorianCalendar time) {
        TimeState state;
        try {
            state = weather.getStateForTime(time);
        } catch (NoSuchTimeStateException e) {
            e.printStackTrace();
            return 0;
        }
        double temp = state.getWeatherState().getTemperature();
        long timeDiff = time.getTimeInMillis() - state.getTime().getTimeInMillis();
        TimeState nextState = getNextStateForTime(weather, state, timeDiff);
        if (nextState == null) {
            return temp;
        }


        double temp2 = nextState.getWeatherState().getTemperature();
        return getAveragedValue(temp, temp2, timeDiff);
    }

    public static int getHumidity(Weather weather, GregorianCalendar time) {
        TimeState state;
        try {
            state = weather.getStateForTime(time);
        } catch (NoSuchTimeStateException e) {
            e.printStackTrace();
            return 0;
        }
        double hum = state.getWeatherState().getHumidity();
        long timeDiff = time.getTimeInMillis() - state.getTime().getTimeInMillis();
        TimeState nextState = getNextStateForTime(weather, state, timeDiff);
        if (nextState == null) {
            return (int)hum;
        }


        double hum2 = nextState.getWeatherState().getHumidity();
        return (int)getAveragedValue(hum, hum2, timeDiff);
    }


    private static double getAveragedValue(double value1, double value2, long timeDiff) {
        double valueDiff = value2 - value1;
        return value1 + valueDiff * Math.abs(timeDiff) / Weather.millisBetweenStates;
    }


    private static TimeState getNextStateForTime(Weather weather, TimeState state, long timeDiff) {


        GregorianCalendar calendar = new GregorianCalendar(weather.getTimeZone());
        calendar.setTimeInMillis(state.getTime().getTimeInMillis());
        calendar.add(Calendar.MILLISECOND, Weather.millisBetweenStates * Long.signum(timeDiff));

        try {
            return weather.getStateForTime(calendar);
        } catch (NoSuchTimeStateException e) {
            return null;
        }
    }

}
