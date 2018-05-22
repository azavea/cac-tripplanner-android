# cac-tripplanner-android
GoPhillyGo Android app


## Setup

Copy API keys file for debug and release builds:
 - `cp example/google_maps_api.xml app/src/debug/res/values/google_maps_api.xml`
 - `cp example/google_maps_api.xml app/src/release/res/values/google_maps_api.xml`

Edit each to set the Android Maps key for both build flavors. See the comments in the example file for directions on how to create the keys.

Also edit to set the server API key for posting user flags.
