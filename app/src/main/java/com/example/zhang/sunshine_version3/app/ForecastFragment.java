package com.example.zhang.sunshine_version3.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.zhang.sunshine_version3.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String Log_Tag = ForecastFragment.class.getSimpleName();

    private static final int FETCHER_WEATHER_LOADER_ID = 0;

    ForecastAdapter mForecastAdapter;

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;


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

        mForecastAdapter = new ForecastAdapter(
                getActivity(),
                null,
                0
        );

        ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);
        listView.setAdapter(mForecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor =(Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                    startActivity(intent);
                }
            }
        });

        return rootView;
        //return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FETCHER_WEATHER_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        Uri weatherForLocationUri = WeatherContract.WeatherEntry
                .buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mForecastAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mForecastAdapter.swapCursor(null);
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FETCHER_WEATHER_LOADER_ID, null, this);
    }

    public void updateWeather() {

        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());
        fetchWeatherTask.execute(location);
    }


//    private class FetchWeatherTask extends AsyncTask<String, Void, String[]>{
//
//        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
//
//        private String getReadableDateString (Long time){
//            //SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE, MMM dd");
//            //return shortenedDateFormat.format(time);
//            return new SimpleDateFormat("EEE, MMM dd").format(time);
//        }
//
//        private String formatHighLows(double high, double low, String unit){
//            if(unit.equals(getString(R.string.unit_entryValue_imperial))){
//                high = (high * 1.8) + 32;
//                low = (low * 1.8) +32;
//            }else if (!unit.equals(getString(R.string.unit_entryVaule_metric))){
//                Log.d(LOG_TAG, "Unit type not find " + unit);
//            }
//
//            long roundedHigh = Math.round(high);
//            long roundedLow = Math.round(low);
//
//            String highLowStr = roundedHigh + "/" + roundedLow;
//            return highLowStr;
//        }
//
//        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
//            throws JSONException{
//            final String OWM_LIST = "list";
//            final String OWM_WEATHER = "weather";
//            final String OWM_MAIN = "main";
//            final String OWM_TEMPERATURE = "temp";
//            final String OWM_MAX = "max";
//            final String OWM_MIN = "min";
//
//
//            JSONObject forecastJson = new JSONObject(forecastJsonStr);
//            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
//            String[] weatherResults = new String[numDays];
//            String unit = PreferenceManager.getDefaultSharedPreferences(getActivity())
//                    .getString(getString(R.string.pref_unit_key),
//                            getString(R.string.pref_unit_defValue));
//
//            //Log.v(LOG_TAG, "unit is " + unit);
//
//            for (int i = 0; i < weatherArray.length(); i++){
//                Date time = new Date();
//                //For Date class (Date.getTime() + 1000*60*60*24) comes to next day
//                String day = getReadableDateString(time.getTime() + i*1000*60*60*24);
//                JSONObject dayForecast = weatherArray.getJSONObject(i);
//                JSONObject dayWeather = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
//                String description = dayWeather.getString(OWM_MAIN);
//                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
//                double high = temperatureObject.getDouble(OWM_MAX);
//                double low = temperatureObject.getDouble(OWM_MIN);
//
//                String highAndLow = formatHighLows(high, low, unit);
//                weatherResults[i] = day + " - " + description + " - " + highAndLow;
//            }
//            return weatherResults;
//        }
//
//        @Override
//        protected String[] doInBackground(String... strings) {
//
//            //Set web call
//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            String forecastJsonStr = null;
//
//            String format = "json";
//            String units = "metric";
//            int numDays = 7;
//            String API_KEY = getString(R.string.API_KEY);
//
//            try{
//                //Construct url
//                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/" +
//                //        "daily?q=montreal&mode=json&units=metric&cnt=7");
//
//                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/" +
//                        "forecast/daily?";
//                final String QUERY_PARAM = "q";
//                final String FORMAT_PARAM = "mode";
//                final String UNITS_PARAM = "units";
//                final String DAYS_PARAM = "cnt";
//                final String API_PARAM = "APPID";
//
//                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
//                        .appendQueryParameter(QUERY_PARAM, strings[0])
//                        .appendQueryParameter(FORMAT_PARAM, format)
//                        .appendQueryParameter(UNITS_PARAM, units)
//                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
//                        .appendQueryParameter(API_PARAM, API_KEY)
//                        .build();
//
//                URL url = new URL(builtUri.toString());
//
//
//                //Log.v(LOG_TAG, "Built URI " + builtUri);
//                //Log.v(LOG_TAG, "Built URI String " + builtUri.toString());
//
//                //Open url and made the construction
//                urlConnection = (HttpURLConnection) url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                //Read the input
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                if (inputStream == null){
//                    return null;
//                }
//
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while ((line = reader.readLine()) != null){
//                    buffer.append(line + "/n");
//                }
//
//                if (buffer.length() == 0){
//                    return null;
//                }
//
//                forecastJsonStr = buffer.toString();
//
//                //Log.v(LOG_TAG, "Forecast JSON String " + forecastJsonStr);
//
//            }catch(IOException e){
//                Log.e(LOG_TAG, "ERROR", e);
//            }finally {
//                if (urlConnection != null){
//                    urlConnection.disconnect();
//                }
//                if (reader != null){
//                    try{
//                        reader.close();
//                    }catch (final IOException e){
//                        Log.e(LOG_TAG, "Error closing the stream");
//                    }
//                }
//
//            }
//
//            try{
//                String[] weatherData = getWeatherDataFromJson(forecastJsonStr, numDays);
//                return weatherData;
//            }catch(JSONException e){
//                Log.e(LOG_TAG, e.getMessage(), e);
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String[] result) {
//            if (result != null){
//                mForecastAdapter.clear();
//                mForecastAdapter.addAll(result);
//                /*for (String dayWeatherData : result){
//                    mForecastAdapter.add(dayWeatherData);
//                }*/
//            }
//        }
//    }

}