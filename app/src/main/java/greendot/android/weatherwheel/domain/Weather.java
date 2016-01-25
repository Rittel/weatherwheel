package greendot.android.weatherwheel.domain;


import java.sql.Time;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import greendot.android.weatherwheel.exceptions.NoSuchTimeStateException;

/**
 * Created by Rittel on 06.03.2015.
 */
public class Weather {
    private ArrayList<TimeState> timeStates;
    private Location location;
    private GregorianCalendar sunUp;
    private GregorianCalendar sunDown;

    public static final int millisBetweenStates = 1000 * 60 * 60 * 3; //3 hours
    private TimeZone timeZone;

    public ArrayList<TimeState> getTimeStates() {
        return timeStates;
    }

    public void setTimeStates(ArrayList<TimeState> timeStates) {
        this.timeStates = timeStates;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public TimeState getCurrentState() throws NoSuchTimeStateException {
        return getStateForTime(new GregorianCalendar(timeZone));
    }

    public TimeState getStateForTime(GregorianCalendar time) throws NoSuchTimeStateException {
        for (TimeState timeState : timeStates) {
            if (Math.abs(timeState.getTime().getTimeInMillis() - time.getTimeInMillis()) < millisBetweenStates / 2) {
                return timeState;
            }
        }
        if (time.before(timeStates.get(0).getTime())) {
            return timeStates.get(0).createCopy(time);
        }
        if (time.after(timeStates.get(timeStates.size() - 1).getTime())) {
            return timeStates.get(timeStates.size() - 1).createCopy(time);
        }
        throw new NoSuchTimeStateException("Given time: " + time.toString());
    }

    public GregorianCalendar getSunDown() {
        return sunDown;
    }

    public void setSunDown(GregorianCalendar sunDown) {
        this.sunDown = (GregorianCalendar) sunDown.clone();
        this.sunDown.setTimeZone(timeZone);
    }

    public GregorianCalendar getSunUp() {
        return sunUp;
    }

    public void setSunUp(GregorianCalendar sunUp) {
        this.sunUp = (GregorianCalendar) sunUp.clone();
        this.sunUp.setTimeZone(timeZone);
    }

    public GregorianCalendar getMinTime() {
        return getTimeStates().get(0).getTime();
    }

    public GregorianCalendar getMaxTime() {
        return getTimeStates().get(getTimeStates().size() - 1).getTime();
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }
}
