package com.example.zhang.sunshine_version3.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * Created by zhang on 25/02/16.
 */
public class LocationEditTextPreference extends EditTextPreference {

    private static final int DEFAULT_MINIMUM_LOCATION_LENGTH = 2;
    private int mMinLength;

    public LocationEditTextPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attributeSet,
                R.styleable.LocationEditTextPreference,
                0,
                0
        );

        try {
            mMinLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength, DEFAULT_MINIMUM_LOCATION_LENGTH);
        } finally {
            a.recycle();
        }
    }
}
