
# Unblu SDK Demo Application

This application serves as a demonstration of the Unblu SDK's capabilities, showcasing the simplest method of integrating and utilizing the SDK.

It provides a foundation for reproducing potential issues or bugs and offers a framework for debugging and testing purposes.

Feel free to submit issues, feature requests, or pull requests to enhance the functionality of this demo application.


# General Unblu Android Integration Guide

This guide will help you integrate Unblu into your Android application.

The steps described below are implemented in this project. However, it is crucial to be aware of the most important step, which you must perform in every project that uses our SDK.

If any aspects are found to be missing, please review the project and code to check how it is configured.


## 1. Add Unblu to Your Project

### 1.1 Prerequisites

    • Java 17
    • Android version 7 or newer
	• API version 24 or newer.


### 1.2 Important Gradle Config

•	Project-level Settings: Add `google()` and `mavenCentral()` to your repositories.
•	Module-level Dependencies:
•	Add Unblu SDK modules (e.g., `coresdk`, `callmodule`, `mobilecobrowsingmodule`) in the dependencies block.

### 1.3 Manifest Permissions

At minimum, Unblu needs Internet and often Audio/Video permissions. For example:

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

####	android.permission.INTERNET:
•	Grants the app permission to access the internet.
•	Required for network-related operations, such as downloading data, sending requests, or connecting to APIs.

####	android.permission.BLUETOOTH:
•	Grants access to interact with Bluetooth devices.
•	Includes scanning for devices, pairing, and communication over Bluetooth.

####	android.permission.POST_NOTIFICATIONS:
•	Required for posting notifications on Android 13 (API level 33) and above.
•	Ensures the app has explicit user consent to send notifications.

####	android.permission.RECORD_AUDIO:
•	Grants the app permission to record audio from the microphone.
•	Needed for features like voice recording, video calls, or sound analysis.

####    android.permission.CAMERA
•	Allows an app to access the device’s camera to capture video or images.
•	Recording videos using the camera.



**There are also other important permissions that you may need to use that have a more complex usage pattern depending on the Android version.**



#### READ_EXTERNAL_STORAGE:
	•   Granted apps access to all files in external storage, including images, videos, and documents.
	•	Limitation: Provided broad access, potentially exposing sensitive data.
	•	Restricted apps to their own files by default, enhancing user privacy.
    •	Starting with Android 13 (API level 33), the permission has been deprecated

#### READ_MEDIA_AUDIO: Access to audio files.

#### READ_MEDIA_IMAGES: Access to image files.

#### READ_MEDIA_VIDEO: Access to video files.

#### READ_MEDIA_AUDIO: Access to audio files.

	•	Allows users to grant apps access to specific media types, replacing the broader READ_EXTERNAL_STORAGE permission.
	•	Partial Media Access:
	•	For devices running Android 12L (API level 32) or lower, continue using READ_EXTERNAL_STORAGE.


#### **Comply with Google Play Policies:**

•	By January 22, 2025, apps requesting READ_MEDIA_IMAGES or READ_MEDIA_VIDEO must undergo an access review to justify the necessity for broad media access. Failure to comply may result in app updates being blocked or removal from Google Play.

### 1.4 Add Unblu credentials

For the demo app to work, you must add your Unblu credentials (your API key and the server URL) to [`UnbluConstants.kt`](https://github.com/unblu/android-demo-app/blob/main/app/src/main/java/com/unblu/navigation/demoapp/unblu/UnbluConstants.kt).


## 2. Key Implementation Steps with Code


### 2.1	 Inheriting from UnbluApplication or Using UnbluApplicationHelper.

You have two options for integrating Unblu:
1.	Inherit from UnbluApplication:

•	If your main application class extends `UnbluApplication`, no additional configuration is required.

3.	Keep your existing Application inheritance and manually invoke Unblu methods:

 •	In your Application class, within the `onCreate()` method, call:

```
UnbluApplicationHelper.onCreate(this);
```

 •	In the `onTerminate()` method, call:

```
UnbluApplicationHelper.onTerminate();
```

•	In the `onConfigurationChanged(Configuration newConfig)` method, call:

```
UnbluApplicationHelper.onConfigurationChanged(newConfig);
```

These calls allow Unblu to observe device state changes (like orientation) and listen for activity lifecycle events.

Finally, in your Activity, within the `onNewIntent(Intent intent)` method, you must also call:

```
UnbluApplicationHelper.onNewIntent(intent.getExtras());
```

This is necessary to properly handle notification actions, such as tapping a button in an incoming call notification.


### 2.2	Create client configuration.
Here you configure specific client settings.

```
val unbluClientConfiguration = UnbluClientConfiguration.Builder(
    UnbluConstants.ENDPOINT_URL,
    UnbluConstants.ENDPOINT_API_KEY,
    unbluPreferencesStorage,
    UnbluDownloadHandler.createExternalStorageDownloadHandler(application),
    UnbluPatternMatchingExternalLinkHandler()
)
```

### 2.3	Create the modules that will be used.
Here you can configure specific module settings.

```

val callModule = CallModuleProviderFactory.createDynamic(
    VonageModule.createForDynamic(),
    LiveKitModuleProvider.createForDynamic()
)
val coBrowsingModule = MobileCoBrowsingModuleProvider.create()

```

### 2.4	Register Modules.

```
val unbluClientConfiguration = UnbluClientConfiguration.Builder(...)
    .registerModule(callModule)
    .registerModule(coBrowsingModule)
    .build()
```

### 2.5 Subscribing to Unblu UI Visibility Requests at Application Startup

You can use this code at a very early stage in your application’s lifecycle for example, in the onCreate() method of your Application class:

```
Unblu
    .onUiVisibilityRequest()
    .subscribe {
        // Handle UI visibility requests, e.g., show the Unblu UI when needed
    }
```

This allows you to be notified of incoming events, such as call notifications, and to display the Unblu UI if necessary.


### 2.6	You can use observables available in modules to handle module events.


```
callModule.isCallActive()
    .subscribe(
        { value ->
            print(value)
        },{}
    )

```

### 2.7	Create and Start Client.
When the client starts, it attempts to load the initial JavaScript scripts and establish a connection via the JavaScript API to the collaboration server.

You can access the Unblu view here or utilize a separate event stream as demonstrated in section 2.6.
```
Unblu.createVisitorClient(
    application,
    activity,
    unbluClientConfiguration,
    unbluNotificationApi,
    { client ->
        successCallback.onSuccess(client)
        visitorClient = client
        // unbluViewModel.setView(client.mainView)
    },
    initializeExceptionCallback
)
```

### 2.8 Embed Unblu View into the UI
The view is an Unblu container that extends RelativeLayout and contains an embedded `WebView`.
```

Unblu.onVisitorInitialized()
    .map { it.mainView }
    .subscribe { view -> unbluViewModel.setView(view) }
...

setContent {
    val mainView by remember { unbluViewModel.view }
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        UnbluSheet(mainView) { }
    }
}
```


### 2.9  Enable Firebase push notifications

After completing the following steps, when the app starts and the Unblu API is initialized, the Firebase token will be sent to the server,
allowing it to send push notifications.
When the Unblu notifications module receives a remote notification, it analyzes whether it originates from the Unblu collaboration server.
If it does, the notification is processed; otherwise, it is ignored.


1. Add Firebase dependencies:

```
dependencies {
    classpath 'com.google.gms:google-services:x.x.x'
}
```

2. Apply the Google services plugin in your app-level build.gradle:

```
plugins {
    id("com.google.gms.google-services")
}
```

3. Add Unblu Firebase dependencies to your app-level build.gradle:

```
dependencies {
    implementation("com.unblu.mobile-sdk-android:firebasenotificationmodule:x.x.x")
}
 ```


4. Add the Firebase service in your AndroidManifest.xml:

```
<service
    android:name="com.unblu.sdk.module.firebase_notification.UnbluFirebaseNotificationService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

5. Place the google-services.json file in the app/ directory.

6. Add this permission to your AndroidManifest.xml:

```
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

7. If targeting Android 13 (API 33+) and above, you need to request notification permissions:

```
 requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQUEST_CODE)
```

8. Initialize Unblu notification API in your code:

```
private var unbluNotificationApi: UnbluNotificationApi =
    UnbluFirebaseNotificationService.getNotificationApi()
```
