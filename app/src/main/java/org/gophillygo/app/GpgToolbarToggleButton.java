package org.gophillygo.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.CompoundButton;

/**
 * Custom toggle button that changes colors with state.
 * Styled for use in a toolbar.
 */

public class GpgToolbarToggleButton extends CompoundButton {
    public GpgToolbarToggleButton(Context context) {
        this(context, null);
    }

    public GpgToolbarToggleButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setBackgroundResource(R.drawable.toggle_button_toolbar_selector);
        setGravity(Gravity.CENTER);
        setClickable(true);
    }
}
