package org.gophillygo.app.di;

import android.app.Application;

import androidx.room.Room;

import org.gophillygo.app.data.AttractionFlagDao;
import org.gophillygo.app.data.DestinationDao;
import org.gophillygo.app.data.DestinationWebservice;
import org.gophillygo.app.data.EventDao;
import org.gophillygo.app.data.GpgDatabase;
import org.gophillygo.app.data.networkresource.LiveDataCallAdapterFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Inject singleton dependencies for use across the app.
 *
 * Based on:
 * https://github.com/googlesamples/android-architecture-components/blob/e33782ba54ebe87f7e21e03542230695bc893818/GithubBrowserSample/app/src/main/java/com/android/example/github/di/AppModule.java
 */

@Module(includes = ViewModelModule.class)
class AppModule {

    protected static final String DATABASE_NAME = "gpg-database";

    // Data services

    @Singleton
    @Provides
    DestinationWebservice provideDestinationWebservice() {
        // add network query logging by setting client on Retrofit
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();

        // Can be Level.BASIC, Level.HEADERS, or Level.BODY
        // See http://square.github.io/okhttp/3.x/logging-interceptor/ to see the options.
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);
        OkHttpClient client = builder.build();

        return new Retrofit.Builder()
                .client(client)
                .baseUrl(DestinationWebservice.WEBSERVICE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .build()
                .create(DestinationWebservice.class);
    }

    @Singleton
    @Provides
    GpgDatabase provideDatabase(Application app) {
        return Room.databaseBuilder(app, GpgDatabase.class, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton
    @Provides
    DestinationDao provideDestinationDao(GpgDatabase db) {
        return db.destinationDao();
    }

    @Singleton
    @Provides
    EventDao provideEventDao(GpgDatabase db) {
        return db.eventDao();
    }

    @Singleton
    @Provides
    AttractionFlagDao provideAttractionFlagDao(GpgDatabase db) {
        return db.attractionFlagDao();
    }
}
