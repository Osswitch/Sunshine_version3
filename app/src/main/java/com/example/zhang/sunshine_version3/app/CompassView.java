package com.example.zhang.sunshine_version3.app;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

/**
 * Created by zhang on 05/11/15.
 */
public class CompassView extends View {

    public CompassView(Context context){
        super(context);
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) context.getSystemService(
                        Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            sendAccessibilityEvent(
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
        }

    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CompassView(Context context, AttributeSet attrs, int DefaultStyle) {
        super(context, attrs, DefaultStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int myHeight = hSpecSize;
        if (hSpecMode == MeasureSpec.EXACTLY) {
            myHeight = hSpecSize;
        } else if (hSpecMode == MeasureSpec.AT_MOST) {
            //Wrap Content
        }

        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int myWidth = wSpecSize;

        if (wSpecMode == MeasureSpec.EXACTLY) {
            myWidth = wSpecSize;
        } else if (wSpecMode == MeasureSpec.AT_MOST) {
            //Wrap Content
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        //return super.dispatchPopulateAccessibilityEvent(event);
        event.getText().add(null);
        return true;
    }
}
