package org.gophillygo.app;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.CompoundButton;

import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;

/**
 * Custom toggle button that changes colors with state.
 */

public class GpgToggleButton extends CompoundButton {

    private static final String LOG_LABEL = "GpgToggleButton";

    public GpgToggleButton(Context context) {
        this(context, null);
    }

    public GpgToggleButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setBackgroundResource(R.drawable.toggle_button_selector);
        setGravity(Gravity.CENTER);
        setClickable(true);
    }


    @BindingAdapter("app:onCheckedChanged")
    public static void setOnCheckedChanged(CompoundButton button, OnCheckedChangeListener listener) {
        Log.d(LOG_LABEL, "setOnCheckedChanged is setting the listener");
        button.setOnCheckedChangeListener(listener);
    }
}
