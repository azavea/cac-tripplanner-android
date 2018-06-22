package org.gophillygo.app.di;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract public class OssLicensesMenuActivityModule {
    @ContributesAndroidInjector
    abstract OssLicensesMenuActivity contributeOssLicensesMenuActivityModule();
}
