package greendot.android.weatherwheel.utility;

import android.content.Context;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import greendot.android.weatherwheel.R;

/**
 * Created by Rittel on 19.03.2015.
 */
public class CountryRessourceProvider {
    private static Map<String, Locale> countryMap;

    static {
        countryMap = new HashMap<String, Locale>();
        Locale usLocale = new Locale("en", "US");
        for (String locale : Locale.getISOCountries()) {
            Locale temp = new Locale("", locale);
            countryMap.put(temp.getDisplayCountry(usLocale), temp);
        }
    }

    public static int getRessource(Context context, String countryname) {
        int ressource = context.getResources().getIdentifier(countryname.toLowerCase() + "_spinner", "drawable", context.getPackageName());
        if (ressource == 0) {
            countryname = getCountryCode(countryname);
            ressource = context.getResources().getIdentifier(countryname.toLowerCase() + "_spinner", "drawable", context.getPackageName());
        }

        if (ressource == 0) {
            return R.drawable.standard_spinner;
        } else {
            return ressource;
        }
    }

    private static String getCountryCode(String countryname) {

        //TODO: new york and sfo
        Locale temp = countryMap.get(countryname);
        if (temp == null) {
            return "";
        }
        return temp.getCountry().substring(1);
    }
}
