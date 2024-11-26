package com.usama.plugins.capacitorsettings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class AppSettingsHelper {

    /**
     * Opens the app's settings page in the device settings.
     *
     * @param context The context from which to start the settings activity.
     */
    public static void openAppSettings(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }
}
