package greendot.android.weatherwheel.utility.networking;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import java.util.Timer;
import java.util.TimerTask;

import greendot.android.weatherwheel.MapsActivity;
import greendot.android.weatherwheel.WeatherWheelApplication;

/**
 * Created by Rittel on 16.07.2015.
 */
public class LocationProvider implements GpsLocationCallBack {


    private Timer timer1;
    private LocationManager lm;
    private GpsLocationCallBack locationCallBack;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    private Location tempResult;
    private boolean found = false;

    private static final int GPS_REQUEST = 501;

    private int gpsFetchTries = 0;
    private static final int MAX_GPS_FETCH_TRIES = 2;

    public boolean getCurrentLocation(Activity context, GpsLocationCallBack result) {
        //I use LocationResult callback class to pass location value from MyLocation to user code.
        locationCallBack = result;
        if (lm == null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_REQUEST);
            return false;
        }

        //exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //don't start listeners if no provider is enabled
        if (!gps_enabled && !network_enabled)
            return false;

        if (gps_enabled)
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        if (network_enabled)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        timer1 = new Timer();
        timer1.schedule(new GetLastLocation(), 20000);
        return true;
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer1.cancel();
            retLocation(location);
            if (ActivityCompat.checkSelfPermission(WeatherWheelApplication.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(WeatherWheelApplication.getAppContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerNetwork);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            // if (gps_enabled) {
            //    tempResult = location;
            //} else {
            timer1.cancel();
            retLocation(location);
            if (ActivityCompat.checkSelfPermission(WeatherWheelApplication.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(WeatherWheelApplication.getAppContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // }
            lm.removeUpdates(this);
            lm.removeUpdates(locationListenerGps);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    @Override
    public boolean gpsFetchFailed() {
        gpsFetchTries++;
        return gpsFetchTries < MAX_GPS_FETCH_TRIES;
    }

    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(WeatherWheelApplication.getAppContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(WeatherWheelApplication.getAppContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            lm.removeUpdates(locationListenerGps);
            if (tempResult == null) {
                lm.removeUpdates(locationListenerNetwork);
            } else {
                retLocation(tempResult);
                return;
            }
            Location net_loc = null, gps_loc = null;
            if (gps_enabled)
                gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (network_enabled)
                net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            //if there are both values use the latest one
            if (gps_loc != null && net_loc != null) {
                if (gps_loc.getTime() > net_loc.getTime())
                    retLocation(gps_loc);
                else
                    retLocation(net_loc);
                return;
            }

            if (gps_loc != null) {
                retLocation(gps_loc);
                return;
            }
            if (net_loc != null) {
                retLocation(net_loc);
                return;
            }
            returnLocation(null);
        }
    }

    private void retLocation(Location loc) {
        gpsFetchTries = 0;
        if (loc == null) {
            locationCallBack.returnLocation(null);
        }
        if (found) {
            return;
        }
        found = true;
        new WeatherFetcher(WeatherWheelApplication.getAppContext()).getLocation(loc.getLongitude(), loc.getLatitude(), this);
    }

//    public String getNameAndIDForTown(double lon, double lat) {
//
//
//        String cityName = null;
//        Geocoder gcd = new Geocoder(WeatherWheelApplication.getAppContext(), Locale.getDefault());
//        List<Address> addresses;
//        try {
//            addresses = gcd.getFromLocation(lat,
//                    lon, 1);
//            if (addresses.size() > 0)
//                System.out.println(addresses.get(0).getLocality());
//            cityName = addresses.get(0).getLocality();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return cityName;
//    }

    @Override
    public void returnLocation(greendot.android.weatherwheel.domain.Location location) {
        locationCallBack.returnLocation(location);
    }

    public void getLocation(String townName, GpsLocationCallBack callBack) {
        this.locationCallBack = callBack;
        new WeatherFetcher(WeatherWheelApplication.getAppContext()).getLocation(townName, this);
    }

}
