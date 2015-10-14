package com.example.zhang.sunshine_version3.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    //private static final String Log_Tag = ForecastFragment.class.getSimpleName();

    ArrayAdapter<String> mForecastAdapter;


    public ForecastFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Tell the fragment there is a menu
        setHasOptionsMenu(true);
    }

    //inflate the menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ArrayAdapter<String>(
                // The current context (this activity)
                getActivity(),
                // The name of layout ID
                R.layout.list_item_forecast,
                // The ID of the textview
                R.id.list_item_forecast_textview,
                //weekForecast
                new ArrayList<String>()
        );

        ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = mForecastAdapter.getItem(position);
                //Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                //Toast.makeText(getContext(), forecast, Toast.LENGTH_SHORT).show();
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(detailIntent);
            }
        });


        return rootView;
        //return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public void updateWeather() {

        //Read location value
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        String location = sharedPreferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_defValue));
        new FetchWeatherTask().execute(location);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]>{

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        
        private String getReadableDateString (Long time){
            //SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE, MMM dd");
            //return shortenedDateFormat.format(time);
            return new SimpleDateFormat("EEE, MMM dd").format(time);
        }

        private String formatHighLows(double high, double low, String unit){
            if(unit.equals(getString(R.string.unit_entryValue_imperial))){
                high = (high * 1.8) + 32;
                low = (low * 1.8) +32;
            }else if (!unit.equals(getString(R.string.unit_entryVaule_metric))){
                Log.d(LOG_TAG, "Unit type not find " + unit);
            }

            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException{
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_MAIN = "main";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";


            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
            String[] weatherResults = new String[numDays];
            String unit = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString(getString(R.string.pref_unit_key),
                            getString(R.string.pref_unit_defValue));

            //Log.v(LOG_TAG, "unit is " + unit);

            for (int i = 0; i < weatherArray.length(); i++){
                Date time = new Date();
                //For Date class (Date.getTime() + 1000*60*60*24) comes to next day
                String day = getReadableDateString(time.getTime() + i*1000*60*60*24);
                JSONObject dayForecast = weatherArray.getJSONObject(i);
                JSONObject dayWeather = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                String description = dayWeather.getString(OWM_MAIN);
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                String highAndLow = formatHighLows(high, low, unit);
                weatherResults[i] = day + " - " + description + " - " + highAndLow;
            }
            return weatherResults;
        }

        @Override
        protected String[] doInBackground(String... strings) {

            //Set web call
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 7;
            String API_KEY = getString(R.string.API_KEY);

            try{
                //Construct url
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/" +
                //        "daily?q=montreal&mode=json&units=metric&cnt=7");

                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/" +
                        "forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String API_PARAM = "APPID";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, strings[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(API_PARAM, API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());


                //Log.v(LOG_TAG, "Built URI " + builtUri);
                //Log.v(LOG_TAG, "Built URI String " + builtUri.toString());

                //Open url and made the construction
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //Read the input
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null){
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null){
                    buffer.append(line + "/n");
                }

                if (buffer.length() == 0){
                    return null;
                }

                forecastJsonStr = buffer.toString();

                //Log.v(LOG_TAG, "Forecast JSON String " + forecastJsonStr);

            }catch(IOException e){
                Log.e(LOG_TAG, "ERROR", e);
            }finally {
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
                if (reader != null){
                    try{
                        reader.close();
                    }catch (final IOException e){
                        Log.e(LOG_TAG, "Error closing the stream");
                    }
                }

            }

            try{
                String[] weatherData = getWeatherDataFromJson(forecastJsonStr, numDays);
                return weatherData;
            }catch(JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null){
                mForecastAdapter.clear();
                mForecastAdapter.addAll(result);
                /*for (String dayWeatherData : result){
                    mForecastAdapter.add(dayWeatherData);
                }*/
            }
        }
    }

}