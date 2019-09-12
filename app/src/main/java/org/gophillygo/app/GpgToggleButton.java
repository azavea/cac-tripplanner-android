package org.gophillygo.app;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;

import androidx.databinding.BindingAdapter;

import com.google.android.material.button.MaterialButton;

/**
 * Custom toggle button that changes colors with state.
 */

public class GpgToggleButton extends MaterialButton {

    public GpgToggleButton(Context context) {
        this(context, null);
    }

    public GpgToggleButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        setBackgroundTintList(getResources()
                .getColorStateList(R.color.toggle_button_background_color_selector));

        setGravity(Gravity.CENTER);
        setClickable(true);
    }

    @BindingAdapter("app:onCheckedChanged")
    public static void setOnCheckedChanged(MaterialButton button, OnCheckedChangeListener listener) {
        button.clearOnCheckedChangeListeners();
        button.addOnCheckedChangeListener(listener);
    }
}
