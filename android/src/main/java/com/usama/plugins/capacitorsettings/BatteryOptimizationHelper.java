package com.usama.plugins.capacitorsettings;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

public class BatteryOptimizationHelper {

    private final Context context;
    private final ActivityResultLauncher<Intent> batteryOptimizationLauncher;
    private PluginCall savedCall;

    public BatteryOptimizationHelper(Context context, ActivityResultLauncher<Intent> launcher) {
        this.context = context;
        this.batteryOptimizationLauncher = launcher;
    }

    // Prompt user to disable battery optimization
    public void promptForBatteryOptimization(PluginCall call) {
        this.savedCall = call;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            if (!powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));

                batteryOptimizationLauncher.launch(intent);
            } else {
                JSObject response = new JSObject();
                response.put("status", "already_disabled");
                response.put("message", "Battery optimization is already disabled.");
                call.resolve(response);
            }
        } else {
            call.reject("Battery optimization settings are not available on this device.");
        }
    }

    // Handle the result of the user's selection
    public void handleActivityResult(boolean granted) {
        if (savedCall != null) {
            JSObject response = new JSObject();
            response.put("status", granted ? "allowed" : "rejected");
            response.put("message", granted
                    ? "User allowed disabling battery optimization."
                    : "User rejected disabling battery optimization.");
            savedCall.resolve(response);
            savedCall = null;
        }
    }
}
