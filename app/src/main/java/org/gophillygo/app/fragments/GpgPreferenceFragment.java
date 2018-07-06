package org.gophillygo.app.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;

import org.gophillygo.app.R;

public class GpgPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // remove dividers: https://stackoverflow.com/a/27952333
        View rootView = getView();
        ListView list = rootView.findViewById(android.R.id.list);
        list.setDivider(null);
    }
}
