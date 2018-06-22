package org.gophillygo.app.di;

import com.google.android.gms.oss.licenses.OssLicensesActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class OssLicensesActivityModule {
    @ContributesAndroidInjector
    abstract OssLicensesActivity contributeOssLicensesActivityModule();
}
