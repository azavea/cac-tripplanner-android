# cac-tripplanner-android
GoPhillyGo Android app

## Build status

![CircleCI](https://circleci.com/gh/azavea/cac-tripplanner-android/tree/develop.svg?style=svg)](https://circleci.com/gh/azavea/cac-tripplanner-android/tree/develop)


## Setup

Copy API keys file for debug and release builds:
 - `cp example/api_keys.xml app/src/debug/res/values/api_keys.xml`
 - `cp example/api_keys.xml app/src/release/res/values/api_keys.xml`

Edit each to set the Android Maps key for both build flavors. See the comments in the example file for directions on how to create the keys.

Also edit to set the server API key for posting user flags.

Copy the example file for the Crashlytics/Fabric configuration and set the API key and secret.
These can be found under the [organization settings](https://www.fabric.io/settings/organizations).

 - `cp example/fabric.properties app/fabric.properties`


Configure Firebase for the crash reporting plugin to function. The Firebase configuration file can be found under the [Firebase console](https://console.firebase.google.com). Copy the `google-services.json` Firebase configuration file to the `app` directory.


## Release

 - Copy the example file for the release build configuration to `/app` and set the passwords
 - Also copy the keystore file used for signing uploads to `/app`
 - Bump the app version number and string
 - Generate a signed release build
