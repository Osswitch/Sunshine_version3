package com.example.zhang.sunshine_version3.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.zhang.sunshine_version3.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private ShareActionProvider mShareActionProvider;
    private static String FORECAST_SHARE_HASHTAG = " #SUNSHINEAPP";

    private static final int DETAIL_WEATHER_LOADER_ID = 0;

    private String mForecastStr;

    private static final String[] FORECAST_COLUMNS= {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP

    };

    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_SHORT_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP =3;
    private static final int COL_WEATHER_MIN_TEMP = 4;

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        //Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        //Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mForecastStr != null) {
            mShareActionProvider.setShareIntent(shareForecastIntent());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intentSetting = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intentSetting);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Intent shareForecastIntent(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mForecastStr + FORECAST_SHARE_HASHTAG);
        return intent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_WEATHER_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Intent intent = getActivity().getIntent();

        if (intent == null) {
            return null;
        }

        return new CursorLoader(
                getActivity(),
                intent.getData(),
                FORECAST_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        if (!cursor.moveToFirst()) {
            return;
        }

        String date = Utility.formatDate(cursor.getLong(COL_WEATHER_DATE));
        String weatherDescription = cursor.getString(COL_WEATHER_SHORT_DESC);
        Boolean isMetric = Utility.isMetric(getActivity());
        String maxTemperature = Utility
                .formatTemperature(cursor.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
        String minTemperature = Utility
                .formatTemperature(cursor.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

        mForecastStr = date + " - " + weatherDescription + " - " + maxTemperature
                + " / " + minTemperature;
        TextView textView = (TextView) getView().findViewById(R.id.fragment_detail_textView);
        textView.setText(mForecastStr);

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareForecastIntent());
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

}
