package greendot.android.weatherwheel.utility;

import android.os.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rittel on 12.06.2015.
 */
public class TickProvider {
    private final int delay;
    private ArrayList<ITickListener> listeners = new ArrayList<>();
    private boolean running;

    private static Map<Integer, TickProvider> providers = new HashMap<>();

    public TickProvider(int delay) {
        this.delay = delay;
    }

    public static TickProvider getInstance(int delay) {
        TickProvider provider;

        if ((provider = providers.get(delay)) == null) {
            provider = new TickProvider(delay);
            providers.put(delay, provider);
        }
        return provider;
    }


    private Handler mHandler = new Handler();
    Runnable mTick = new Runnable() {
        public void run() {
            issueTicks();
            mHandler.postDelayed(this, delay); // 20ms == 60fps
        }
    };

    private synchronized void issueTicks() {
        for (ITickListener ticker : listeners) {
            ticker.tick();
        }
    }

    public synchronized void registerTickListener(ITickListener listener) {
        if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
        if (!running) {
            running = true;
            mHandler.removeCallbacks(mTick);
            mHandler.post(mTick);
        }
    }

    public synchronized void unregisterTickListener(ITickListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                running = false;
                mHandler.removeCallbacks(mTick);
            }
        }

    }
}
