package org.gophillygo.app.activities;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.gophillygo.app.R;

import java.util.Objects;

public class AppInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        Toolbar toolbar = findViewById(R.id.app_info_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        // set up links
        TextView mainLink = findViewById(R.id.app_info_root_site_link);
        Linkify.addLinks(mainLink, Linkify.WEB_URLS);
        TextView blogLink = findViewById(R.id.app_info_blog_link);
        Linkify.addLinks(blogLink, Linkify.WEB_URLS);
        TextView cacLink = findViewById(R.id.app_info_cac_link);
        Linkify.addLinks(cacLink, Linkify.WEB_URLS);
        MovementMethod method = LinkMovementMethod.getInstance();
        mainLink.setMovementMethod(method);
        blogLink.setMovementMethod(method);
        cacLink.setMovementMethod(method);
    }

}
