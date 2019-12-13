package org.gophillygo.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import org.gophillygo.app.data.DestinationDao;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class GoPhillyGoContentProvider extends ContentProvider {

    private static final String LOG_LABEL = "GPGContentProvider";

    // SQL LIKE match wildcard
    private static final Character WILDCARD = '%';

    @SuppressWarnings("WeakerAccess")
    @Inject
    DestinationDao destinationDao;

    public GoPhillyGoContentProvider() {
        Log.d(LOG_LABEL, "constructor");
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(@NonNull Uri uri) {
        // Implement this to handle requests for the MIME type of the data at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        AndroidInjection.inject(this);
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        if (destinationDao == null) {
            Log.e(LOG_LABEL, "Failed to find injected DAO for content provider");
            return null;
        }

        String query = uri.getLastPathSegment();
        return destinationDao.searchAttractions(WILDCARD + query + WILDCARD);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
