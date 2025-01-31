package rekab.app.background_locator

import android.content.Context
import android.content.Intent
import com.google.android.gms.location.LocationRequest
import io.flutter.FlutterInjector
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import rekab.app.background_locator.logger.Logger
import rekab.app.background_locator.logger.d
import rekab.app.background_locator.provider.LocationRequestOptions
import java.util.concurrent.atomic.AtomicBoolean

internal fun IsolateHolderService.startLocatorService(context: Context) {

    var serviceStarted: AtomicBoolean? = null
    var callbackHandle: Long? = null
    var callbackInfo: FlutterCallbackInformation? = null
    var args: DartExecutor.DartCallback? = null

    context.d("startLocatorService")

    try {
        serviceStarted = AtomicBoolean(PreferencesManager.isServiceRunning(context))
        // start synchronized block to prevent multiple service instant
        synchronized(serviceStarted) {
            this.context = context
            if (IsolateHolderService.backgroundEngine == null) {

                // We need flutter engine to handle callback, so if it is not available we have to create a
                // Flutter engine without any view
                IsolateHolderService.backgroundEngine = FlutterEngine(context)

                callbackHandle = context.getSharedPreferences(
                        Keys.SHARED_PREFERENCES_KEY,
                        Context.MODE_PRIVATE)
                        .getLong(Keys.CALLBACK_DISPATCHER_HANDLE_KEY, 0)

                callbackHandle?.let {
                    callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(it)
                }

                callbackInfo?.let {
                    args = DartExecutor.DartCallback(
                            context.assets,
                            FlutterInjector.instance().flutterLoader().findAppBundlePath(),
                            it
                    )
                }

                args?.let {
                    IsolateHolderService.backgroundEngine?.dartExecutor?.executeDartCallback(it)
                }
                context.d("callbackHandle: $callbackHandle, callbackInfo: $callbackInfo, args: $args")
            }
        }

        backgroundChannel =
                MethodChannel(IsolateHolderService.backgroundEngine?.dartExecutor?.binaryMessenger,
                              Keys.BACKGROUND_CHANNEL_ID)
        backgroundChannel.setMethodCallHandler(this)

    } catch (throwable: Throwable) {
        throw Exception(
                "IsolateHolderService.startLocatorService was't successfull, " +
                        "serviceStarted: $serviceStarted, callbackHandle: $callbackHandle, " +
                        "callbackInfo: $callbackInfo, args: $args",
                throwable
        )
    }
}

fun getLocationRequest(intent: Intent): LocationRequestOptions {
    val interval: Long = (intent.getIntExtra(Keys.SETTINGS_INTERVAL, 10) * 1000).toLong()
    val accuracyKey = intent.getIntExtra(Keys.SETTINGS_ACCURACY, 4)
    val accuracy = getAccuracy(accuracyKey)
    val distanceFilter = intent.getDoubleExtra(Keys.SETTINGS_DISTANCE_FILTER, 0.0)

    return LocationRequestOptions(interval, accuracy, distanceFilter.toFloat())
}

fun getAccuracy(key: Int): Int {
    return when (key) {
        0 -> LocationRequest.PRIORITY_NO_POWER
        1 -> LocationRequest.PRIORITY_LOW_POWER
        2 -> LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        3 -> LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        4 -> LocationRequest.PRIORITY_HIGH_ACCURACY
        else -> LocationRequest.PRIORITY_HIGH_ACCURACY
    }
}