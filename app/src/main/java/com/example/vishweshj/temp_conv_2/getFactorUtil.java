package com.example.vishweshj.temp_conv_2;

/**
 * Created by Vishwesh J on 25-08-2015.
 */

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;





public class getFactorUtil {


    static double conversionFactor = 0;


    public static double getJsonQuery() {

        return conversionFactor;

    }

    public static double getFactor(String currencyQuery, String fromCurrency, String toCurrency)
            throws JSONException {

        final String LOG_TAG = null;
        double conversionFactor = 0;

        try {

            JSONObject currencyQueryObject = new JSONObject(currencyQuery);
            JSONObject quotesObject = currencyQueryObject.getJSONObject("quotes");
            conversionFactor = quotesObject.getDouble(fromCurrency + toCurrency);


        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return conversionFactor;

    }
}