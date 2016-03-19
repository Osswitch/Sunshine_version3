package com.example.zhang.sunshine_version3.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private final String LOG_TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {

            Bundle bundle = new Bundle();
            bundle.putParcelable(DetailActivityFragment.DETAIL_URI, getIntent().getData());
            bundle.putBoolean(DetailActivityFragment.DETAIL_TRANSITION_ANIMATION, true);

            DetailActivityFragment df = new DetailActivityFragment();
            df.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, df)
                    .commit();

            // Being here means we are in animation mode
            supportPostponeEnterTransition();
        }
    }

}
