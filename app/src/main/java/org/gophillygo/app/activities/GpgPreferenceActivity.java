package org.gophillygo.app.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.gophillygo.app.R;

public class GpgPreferenceActivity extends AppCompatActivity {

    private static final String LOG_LABEL = "PreferenceActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        setTheme(R.style.GpgPreferenceFragment);

        // set up toolbar
        Toolbar toolbar = findViewById(R.id.preferences_toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
