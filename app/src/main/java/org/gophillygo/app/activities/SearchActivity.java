package org.gophillygo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.gophillygo.app.R;

public class SearchActivity extends AppCompatActivity {

    private static final String LOG_LABEL = "SearchActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        // Immediately return to sending activity
        finish();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleSearch();
    }

    private void handleSearch() {
        finish();
    }
}
