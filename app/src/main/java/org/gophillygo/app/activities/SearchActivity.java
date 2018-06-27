package org.gophillygo.app.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;


import org.gophillygo.app.R;

public class SearchActivity extends AppCompatActivity {

    private static final String LOG_LABEL = "SearchActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Log.d(LOG_LABEL, "Search activity created");
        handleSearch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(LOG_LABEL, "on new intent");
        handleSearch();
    }

    private void handleSearch() {
        Log.d(LOG_LABEL, "handleSearch");
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String searchQuery = intent.getStringExtra(SearchManager.QUERY);

            Log.d(LOG_LABEL, "TODO: set up search adapter for query " + searchQuery);

            /*
            CustomSearchAdapter adapter = new CustomSearchAdapter(this,
                    android.R.layout.simple_dropdown_item_1line,
                    StoresData.filterData(searchQuery));
            listView.setAdapter(adapter);
            */

        }else if(Intent.ACTION_VIEW.equals(intent.getAction())) {
            String selectedSuggestionRowId =  intent.getDataString();
            //execution comes here when an item is selected from search suggestions
            //you can continue from here with user selected search item
            Log.d(LOG_LABEL, "selected search suggestion " + selectedSuggestionRowId);
        }
    }
}
