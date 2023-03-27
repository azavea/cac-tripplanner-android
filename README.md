# cac-tripplanner-android

GoPhillyGo Android app

## Build status

[![CircleCI](https://circleci.com/gh/azavea/cac-tripplanner-android.svg?style=svg)](https://circleci.com/gh/azavea/cac-tripplanner-android)

## Setup

Copy API keys file for debug and release builds:

- `cp example/api_keys.xml app/src/debug/res/values/api_keys.xml`
- `cp example/api_keys.xml app/src/release/res/values/api_keys.xml`

Edit each to set the Android Maps key for both build flavors: release values can be found in 1Password; see the comments in the example file for directions on how to create a debug key.

Also edit to set the server API key for posting user flags.

Configure Firebase for the crash reporting plugin to function. The Firebase configuration file can be found under the [Firebase console](https://console.firebase.google.com) or 1Password. Copy the `google-services.json` Firebase configuration file to the `app` directory.

## Testing in Android Studio

**Setting up Your Emulators**

Emulators require an SDK and virtual device to run. To install a new SDK:

- Select the SDK Manager (the icon that looks like a box with a downward arrow) from the menu in the upper righthand corner of Android Studio
- Look to see that your target SDK version is installed; if not, check it and select "apply" then "ok"

To create a new device:

- Select the Device Manager (the icon that shows the phone with the Android robot) next to the SDK Manager icon
- Select which device you would like to test, then click "Next"
- Look for and download your targt API level as necessary, paying attention to any warnings that appear in the bottom righthand corner of the popup, then click "Next"
- Verify the configuration and then select "Finish"

Configure at least 3 emulators for the target, lowest supported, and most recent API levels of your release.

**Debugging**

- To run the app in debug mode, select Build&#8594;Select Build Variant and set the Active Build Variant to debug
- Select your desired emulator and then click the "Play" button in the top righthand menu to start the app
- If the app closes instead of starting, select the Debug icon in the top righthand menu. It will attempt to start the app itself and print out errors in the debug log in the bottom lefthand corner of the screen
- Once debugging is running, you can click around to see other errors
- You can view linting errors by selecting Code&#8594;Inspect Code and selecting the desire scope of inspection. The output from this inspection will appear in the "Problems" log in the bottom lefthand corner of the screen.
- After making changes to the code, you can select "Apply Code Changes and Restart Activity" (the icon with an arrow arching over the letter A) in the upper right menu

## Release

**Starting a Release**

- Run `git checkout master && git pull` and `git checkout develop && git pull`, then from `develop`, start a release branch with `git flow release start [version number]`
- If you haven't already, replace the values in `app/src/release/res/values/api_keys.xml` with the values from 1Password.
- Download the keystore file from the GoPhillyGo Android Upload Key entry in 1Password and move it into `app/`
- Copy and set the values in the `release.properties` and `fabric.properties` files to those in 1Password:
  - `cp example/release.properties app/release.properties`
  - `cp example/fabric.properties app/fabric.properties`
- Bump the version numbers in `versionCode` and `versionName` in `app/build.gradle`

**Signing the App Bundle**

- From inside Android Studio, select Build&#8594;Generate Signed Bundle/APK
- Select that you'd like to generate an Android App Bundle, then click next
- For the keystore path, select "Choose existing" and select the downloaded keystore file. Set the keystore password, key alias, and key (alias) password with the values from `release.properties`
- Uncheck "export encrypted key for enrolling published apps in Google Play App Signing" if necessary
- Select release for the destination folder, then select finish
- You can view the output of this process via the Event Log, which you can toggle in the bottom righthand corner

**Testing**

- Once the App Bundle has finished building, in Android Studio select Build&#8594;Select Build Variant
- Change the Active Build Variant in the left panel from debug to release and then click run
  - _Note:_ If you followed the Debugging instructions above, you'll need to uninstall the debug version of the app on the emulator before you can run the release version. Otherwise, you'll see "inconsistent certificates" errors.
- Test the app and make sure it works as expected, including exploring the map, saving an event to your calendar, getting directions to a place or event, and liking an event

**Releasing to Google Play for Testing**

- View the Google Play console from the AzaveaDev Google account and select Testing&#8594;Open testing from the left menu, then create a new release
- Upload the signed App Bundle and add the reason for the release in the release notes field, then click review
- Review to make sure everything looks good, then select start roll out. It will take a while (about 3-4 hours) for the test version to be ready; this will be reflected when the release status switches from "In review" to "Available to unlimited testers"
- Invite users to test by sending them one of the links from Open testing&#8594;Testers&#8594;How testers join your test. They should open the link with the Google Account of their phone's Google Play account.

**Finishing the Release**

- Once the app has been throughly tested, from the open testing page, select Promote release&#8594;Production, review, and then select Start Rollout
- Use `git flow release finish [version number]` to merge the release branch back to master and delete the release branch. Add the reason for the release to the tag commit.
- Be sure to push the `master` and `develop` branches and `git push --tags`
- From the GitHub release page, click to 'Draft new release'
- Enter the tag for the new release and a description of what changed, then click publish
