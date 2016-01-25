package greendot.android.weatherwheel.utility.networking;

import org.json.JSONObject;

import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by Rittel on 31.05.2015.
 */
public interface SunUpDownCallback{

    void setTimeUpDown(UpDownIntermediate upDown);

    class UpDownIntermediate {
        private GregorianCalendar up;
        private GregorianCalendar down;

        public UpDownIntermediate(GregorianCalendar down, GregorianCalendar up) {
            this.down = down;
            this.up = up;
        }

        public GregorianCalendar getDown() {
            return down;
        }

        public void setDown(GregorianCalendar down) {
            this.down = (GregorianCalendar) down.clone();
        }

        public GregorianCalendar getUp() {
            return up;
        }

        public void setUp(GregorianCalendar up) {
            this.up = (GregorianCalendar) up.clone();
        }
    }

    boolean getSunUpDownFailed();
}
