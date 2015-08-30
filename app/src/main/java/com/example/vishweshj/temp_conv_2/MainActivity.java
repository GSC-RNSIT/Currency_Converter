package com.example.vishweshj.temp_conv_2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;


public class MainActivity extends ActionBarActivity implements OnItemSelectedListener {

    TextView out_text;
    EditText in_text;
    String fromCurrency, toCurrency;
    String spinnerList[] = {"USD","EUR","JPY","GBP","CHF","CAD",
            "AUD","INR","CNY","AED","SGD","RUB"};
    Spinner fromSpinner, toSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        out_text = (TextView) findViewById(R.id.textView_output);
        in_text = (EditText) findViewById(R.id.editText);

        if (savedInstanceState != null) {
            out_text.setText(savedInstanceState.getString("textviewstate"));
        }

        fromSpinner = (Spinner) findViewById(R.id.fromSpinner);
        ArrayAdapter<String> fromAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, spinnerList);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);
        fromSpinner.setOnItemSelectedListener(this);

        toSpinner = (Spinner) findViewById(R.id.toSpinner);
        ArrayAdapter<String> toAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, spinnerList);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);
        toSpinner.setOnItemSelectedListener(this);




    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("textviewstate", String.valueOf(out_text.getText()));


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_help) {

            Intent helpIntent = new Intent(this, HelpActivity.class);
            startActivity(helpIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onButtonClick(View view) {

        new urlOperation().execute();

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {

            case R.id.fromSpinner:
                fromSpinner.setSelection(position);
                fromCurrency = (String) fromSpinner.getSelectedItem();
                break;

            case R.id.toSpinner:
                toSpinner.setSelection(position);
                toCurrency = (String) toSpinner.getSelectedItem();
                break;

        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public class urlOperation extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            final String LOG_TAG = null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String currencyQuery = null;

            try {


                final String BASE_URL = "http://apilayer.net/api/live";
                final String ACCESS_KEY = "?access_key=1aa02183cef1c7f7f895eb2b1ad0464a&source=";
                final String CURRENCY = "&currencies=";
                final String FORMAT = "&format=1";

                /*Limitation - Since the API used for conversion is a free plan, it only allows
                one source currency, USD. Thus when source currency is changed, the converted
                value returned is 0.0
                 */


                URL url;

                /*Due to the above mentioned limitations, the following method is used to
                fetch the URL in order to facilitate usage of different source currencies
                 */

                if(fromCurrency.equals("USD"))  {
                    url = new URL(BASE_URL + ACCESS_KEY + fromCurrency + CURRENCY +
                            toCurrency + FORMAT);
                }
                else if(toCurrency.equals("USD"))   {
                    url = new URL(BASE_URL + ACCESS_KEY + toCurrency + CURRENCY +
                            fromCurrency + FORMAT);
                }
                else    {
                    url = new URL(BASE_URL + ACCESS_KEY + "USD" + CURRENCY +
                            fromCurrency + "," + toCurrency + FORMAT);
                }



                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                InputStream inputStream =  urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {


                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {

                }

                currencyQuery = buffer.toString();

                /*
                A new parameter "type" is used in the getFactorUtil.getFactor() function.
                It is used to specify the three types of conversions used to work around the
                limitation.
                 */

                if(fromCurrency.equals("USD")) {
                    //USD to any other currency
                    getFactorUtil.conversionFactor = getFactorUtil.getFactor(currencyQuery,
                            fromCurrency, toCurrency,0);
                }
                else if(toCurrency.equals("USD")) {
                    //Other currencies to USD
                    getFactorUtil.conversionFactor = getFactorUtil.getFactor(currencyQuery,
                            toCurrency, fromCurrency,1);
                }
                else {
                    //Neither currency is USD
                    getFactorUtil.conversionFactor = getFactorUtil.getFactor(currencyQuery,
                            toCurrency, fromCurrency,2);
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            double value = Float.parseFloat(in_text.getText().toString());
            double factor = getFactorUtil.getJsonQuery();

            out_text.setText(String.valueOf(conversionUtil.convertCurrency(value, factor)));
        }
    }
}