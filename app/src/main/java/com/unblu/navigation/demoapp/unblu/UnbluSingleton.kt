package com.unblu.navigation.demoapp.unblu

import android.app.Activity
import android.app.Application
import android.view.View
import com.unblu.livekitmodule.LiveKitModuleProvider
import com.unblu.navigation.usability.unblu.UnbluConstants
import com.unblu.sdk.core.Unblu
import com.unblu.sdk.core.callback.InitializeExceptionCallback
import com.unblu.sdk.core.callback.InitializeSuccessCallback
import com.unblu.sdk.core.configuration.UnbluClientConfiguration
import com.unblu.sdk.core.configuration.UnbluDownloadHandler
import com.unblu.sdk.core.configuration.UnbluPreferencesStorage
import com.unblu.sdk.core.links.UnbluPatternMatchingExternalLinkHandler
import com.unblu.sdk.core.module.call.CallModuleProviderFactory
import com.unblu.sdk.core.notification.UnbluNotificationApi
import com.unblu.sdk.core.visitor.UnbluVisitorClient
import com.unblu.sdk.module.call.CallModule
import com.unblu.sdk.module.firebase_notification.UnbluFirebaseNotificationService
import com.unblu.sdk.module.mobilecobrowsing.MobileCoBrowsingModule
import com.unblu.sdk.module.mobilecobrowsing.MobileCoBrowsingModuleProvider
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.*
import com.unblu.sdk.module.call.CallModuleProvider as VonageModule

object UnbluSingleton {
    //Store uiShowRequests as you may receive them when you don't have a client running
    //or the Unblu Ui isn't attached
    private var onUiShowRequest = BehaviorSubject.createDefault(false)
    private var visitorClient: UnbluVisitorClient? = null
    private lateinit var callModule: CallModule
    private lateinit var coBrowsingModule: MobileCoBrowsingModule
    //Make sure you have your google-services.json in the app/ folder
    private var unbluNotificationApi: UnbluNotificationApi =
        UnbluFirebaseNotificationService.getNotificationApi()
    private lateinit var unbluPreferencesStorage: UnbluPreferencesStorage
    val conversationOpen = BehaviorSubject.createDefault(false)

    fun start(
        uApplication: Application,
        activity: Activity,
        successVoidCallback: InitializeSuccessCallback<UnbluVisitorClient>,
        deinitializeExceptionCallback: InitializeExceptionCallback
    ) {
        createClient(uApplication, activity, successVoidCallback, deinitializeExceptionCallback)
    }

    private fun createClient(
        uApplication: Application,
        activity: Activity,
        successCallback: InitializeSuccessCallback<UnbluVisitorClient>,
        initializeExceptionCallback: InitializeExceptionCallback
    ) {
        val unbluClientConfiguration = createUnbluClientConfiguration(uApplication)
        if (unbluClientConfiguration == null) {
            initializeExceptionCallback.onConfigureNotCalled()
            return
        }
        Unblu.createVisitorClient(
            uApplication,
            activity,
            unbluClientConfiguration,
            unbluNotificationApi,
            { client ->
                client.openConversation
                    .map { it.isPresent }
                    .subscribe(conversationOpen::onNext)
                client.setModalViewEnabled(true)
                successCallback.onSuccess(client)
                visitorClient = client
            },
            initializeExceptionCallback
        )
    }

    private fun createUnbluClientConfiguration(uApplication: Application): UnbluClientConfiguration? {
        callModule = CallModuleProviderFactory.createDynamic(
            VonageModule.createForDynamic(),
            LiveKitModuleProvider.createForDynamic()
        )
        coBrowsingModule = MobileCoBrowsingModuleProvider.create()
        val builder = UnbluClientConfiguration.Builder(
            UnbluConstants.ENDPOINT_URL,
            UnbluConstants.ENDPOINT_API_KEY,
            unbluPreferencesStorage,
            UnbluDownloadHandler.createExternalStorageDownloadHandler(uApplication),
            UnbluPatternMatchingExternalLinkHandler()
        )
            .setCameraUploadsEnabled(true)
            .setVideoUploadsEnabled(true)
            .setPhotoUploadsEnabled(true)
            .registerModule(callModule)
            .registerModule(coBrowsingModule)
        return builder.build()
    }

    fun getClient(): UnbluVisitorClient? {
        return if (Objects.isNull(visitorClient) || (visitorClient!!.isDeInitialized)) null else visitorClient
    }

    fun getUnbluUi(): View? {
        return visitorClient?.mainView
    }

    fun setRequestedUiShow() {
        onUiShowRequest.onNext(true)
    }

    fun hasUiShowRequest(): Observable<Boolean> {
        return onUiShowRequest
    }

    fun clearUiShowRequest() {
        onUiShowRequest.onNext(false)
    }

    fun createSharedPreferences(mainApplication: Application) {
        unbluPreferencesStorage =
            UnbluPreferencesStorage.createSharedPreferencesStorage(mainApplication.applicationContext)
    }
}