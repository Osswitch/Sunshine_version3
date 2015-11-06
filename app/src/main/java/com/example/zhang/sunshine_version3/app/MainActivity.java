package com.example.zhang.sunshine_version3.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.example.zhang.sunshine_version3.app.sync.SunshineSyncAdapter;

public class MainActivity extends AppCompatActivity implements ForecastFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;
    private String mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocation = Utility.getPreferredLocation(this);

        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container,
                                new DetailActivityFragment(), DETAILFRAGMENT_TAG).commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
        ForecastFragment ff = (ForecastFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast);
        ff.setUseTodayLayout(!mTwoPane);

        SunshineSyncAdapter.initializeSyncAdapter(this);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //Intent settingsIntent = new Intent(this, SettingsActivity.class);
            //startActivity(settingsIntent);
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation(this);
        if (location != null && mLocation != location) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_forecast);
            if (ff != null) {
                ff.onLocationChanged();
            }
            DetailActivityFragment df = (DetailActivityFragment) getSupportFragmentManager()
                    .findFragmentByTag(DETAILFRAGMENT_TAG);
            if (df != null) {
                df.onLocationChanged(location);
            }
            mLocation = location;
        }
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if (mTwoPane) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(DetailActivityFragment.DETAIL_URI, dateUri);

            DetailActivityFragment df = new DetailActivityFragment();
            df.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, df).commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class).setData(dateUri);
            startActivity(intent);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
