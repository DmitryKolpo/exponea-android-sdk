## Release Notes
## Release Notes for 2.7.2
#### April 09, 2020
* Features
  * Switching projects in `anonymize()` method. If you need to switch projects, you can use `anonymize()` method to create a new customer and start fresh tracking into a new project. Please see [ANONYMIZE.md](./ANONYMIZE.md) for more information.
* Bug Fixes
  * Fixed: When the app was opened from a push notifications, in-app messages would not initialize properly and would not show until the app is restarted.
  * Fixed: When the SDK was initialized with the application context instead of the activity context, in-app messages would not show properly.
  * Fixed: Tracking to multiple projects. It now requires both project token and authorization token. Please see [PROJECT_MAPPING.md](./PROJECT_MAPPING.md).


## Release Notes for 2.7.1
#### March 24, 2020
* Features
  * Push notification accent color setting - you can now set ExponeaConfiguration property `pushAccentColor` to change color of push notification title and buttons.
  * Improved documentation on handling push notifications and flushing setup.
* Bug Fixes
  * Fixed: Occasional crash during in-app message preload.
  * Fixed: Push notifications deeplinking issue. In some rare cases, push notification with deeplink would not open correctly.

## Release Notes for 2.7.0
#### March 02, 2020
* Features
  * New in-app messages - display rich messages when app starts or an event is tracked - even when offline. This SDK is fully ready for the feature when it is publicly available soon.
* Bug Fixes
  * Fixed: Rare internal database thread-safety related crash.

## Release Notes for 2.6.3
#### February 20, 2020
* Features
  * Updated Firebase messaging library to version 2.1.0
  * Updated minimum SDK version to 16 as recommended by Google

## Release Notes for 2.6.2
#### January 10, 2020
* Bug Fixes
  * Fixed: Fetch recommendations functionality was reimplemented, it was incorrectly removed at one point. (see [FETCH.md](./FETCH.md))
  * Fixed: Thread safety problem in crash reporting that caused the application to crash (it would still report the crash itself).
  * Fixed: Issue with serializing Infinity, -Infinity and NaN. Double and Float variables can hold these values, but they are not part of JSON standard and Exponea doesn't support them. These values will now be reported as string values, but should be avoided. Fixes [Issue #20](https://github.com/exponea/exponea-android-sdk/issues/20).
  * Removed: Automatic payment tracking was broken and has been removed from the Exponea Android SDK. In case you're interested in this functionality let us know. Manual tracking is simplified - device properties are now automatically added. (see [PAYMENT.md](./PAYMENT.md))

## Release Notes for 2.6.1
#### December 20, 2019
* Bug Fixes
  * Fixed: Google Play Billing Library version > 2.0 would cause the SDK to fail to initialize properly. To mitigate the issue, SDK now only disables automatic payment tracking. Proper solution will be implemented later.

## Release Notes for 2.6.0
#### November 26, 2019
* Features
  * The SDK is now able to report the SDK-related crashes to Exponea. This helps us keep the SDK in a good shape and work on fixes as soon as possible. 
* Bug Fixes
  * Fixed: https://github.com/exponea/exponea-android-sdk/issues/16 - The internal database could have been initialized twice. This won't happen anymore.
  * Fixed: The SDK now correctly reports the version of the host application in the session events.

## 2.5.0
#### November 05, 2019
* Features
  * The SDK has a new protective layer for the public API as well as for the interaction with the operating system. It means that in the production setup it prefers to fail silently instead of crashing the whole application.
  * Push notification events now contain more information about campaigns and the action taken and are consistent with Exponea iOS SDK.
* Bug Fixes
  * Increased overall code quality by fixing many lint errors. This makes many warnings from the SDK disappear when the application is compiled.
  * The internal mechanisms of the SDK are now really hidden and not usable by the application. It prevents developers from using some undocumented internal part of the SDK in an inappropriate way.
  * Fixed: The periodic flushing mechanism has been fixed. Once it was started, it wouldn't stop. Now it does.
  * There are significant improvements in the unit tests of the SDK.

### 2.4.0
#### September 30, 2019
* Features
    * [App links](./APP_LINKS.md): SDK can now track app opens from App Link. Sessions that are started from App Link contain campaign data.
* Bug Fixes
    * Exponea SDK can only be initialized once. Subsequent calls to `Exponea.init` are ignored. Multiple instances of SDK cause sessions to be tracked twice.

### 2.3.3
#### August 28, 2019
* Bug Fixes
  * Fixed: Properties in the events were found to be accumulating over time. This happened always after tracking multiple events (after multiple calls of `Exponea.trackEvent()`, `Exponea.identifyCustomer()` or similar). The impacted versions are thankfully only `2.3.1` and `2.3.2`.

### 2.3.2
* `WorkManager` updated to the latest version to avoid inconsistency and crashes
* Configuration now has a token update frequency, which specifies how often should the push token be sent to Exponea (on change, on every start, daily)

### 2.3.1
* Added option to specify default properties to be tracked with all events

### 2.3.0
* Added option to fetch consent cateogires (see [FETCH.md](./FETCH.md))

### 2.2.7
* Make push notifications valid when they have a title or a message

### 2.2.6
* Improvements to notification tracking parameters

### 2.2.5
* Documentation improvements
* Change notification action_type to "mobile notification" for better tracking purposes

### 2.2.4
* Update dependencies

### 2.2.3
* Possible fix for null enum ordinal crash on routes when loading from database
* Don't display push notifications if title is empty

### 2.2.2
* Fixed a bug with notification body deeplink not getting parsed correctly

### 2.2.1
* Add the ability to use custom Firebase service by create public method in Exponea to handle remote push notification, which tracks and optionally shows notification

### 2.2.0
* Fix for crash with billing client 1.2 dependency 
	- **It is now required to provide a list of SKUs of in app purchases you wish to track. Please see [this document](./PAYMENT.md) for information about how to set this up.**

### 2.1.0
* Advanced tracking for push-notification actions
* Flushing behaviour fixes

### 2.0.0

* Removal of deprecated functions
* Documentation improvements

### 1.2.0-beta-5

* Fix parsing of notification body action deeplink url
* Improve initialising Exponea from configuration file
* Update documentation

### 1.2.0-beta-4

* Add a workaround for a crash happening only on Meizu M5 with Android 6.0 due to OkHttp 

### 1.2.0-beta-3
* Fix an edge case where a notification triggered intent might crash the app
* Add automatic ProGuard rules to the SDK 
* Internal improvements to rich push notifications code

### 1.2.0-beta-2
* Fix image quality in rich push
* Fix dismissal of notification tray on button click

### 1.2.0-beta-1
* Adds support for the rich push notifications feature

### 1.1.7
* Fix for crash when Looper.prepare() wasn't called on Exponea init
* Deprecations of basic auth in favor of token auth
* Anonymize feature now carries over the push token of the device

### 1.1.6
* Automatic session tracking property in configuration is now properly respected
* Fixed a crash when Firebase was initialised before Exponea

### 1.1.5
* Fixed a crash when push notification payload was null

### 1.1.4
* Timestamp type changed from Long to Double for better precision

### 1.1.3
* Fixed anonymize functionality

### 1.1.2
* Default flush mode set to `IMMIDIATE`
* Improved retry mechanism for events flushing


### 1.1.1
* Expandable Notifications

### 1.1.0

* [Anonymize feature](./ANONYMIZE.md)
* Dependencies update
* Pass custom data to FCM manager
* Fixed flushing behavior (Changed timestamp from milliseconds to seconds)
* Fixed push tracking with Android 8+
* SDK is now compatible with Android API 14+  

### 1.0

Initial release.
