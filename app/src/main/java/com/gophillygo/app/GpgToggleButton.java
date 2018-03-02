package com.gophillygo.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.CompoundButton;

/**
 * Custom toggle button that changes colors with state.
 */

public class GpgToggleButton extends CompoundButton {

    public GpgToggleButton(Context context) {
        this(context, null);
    }

    public GpgToggleButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setBackgroundResource(R.drawable.toggle_button_selector);
        setGravity(Gravity.CENTER);
        setClickable(true);
    }
}
