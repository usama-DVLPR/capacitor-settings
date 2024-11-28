package com.usama.plugins.capacitorsettings;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.google.android.gms.location.LocationServices;
import androidx.core.app.ActivityCompat;
import com.usama.plugins.capacitorsettings.BatteryOptimizationHelper;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.Handler;
import android.os.Looper;

@CapacitorPlugin(
        name = "CapacitorSettings",
        permissions = {
                @Permission(strings = {Manifest.permission.BLUETOOTH_CONNECT}, alias = "bluetoothConnect")
        }
)
public class CapacitorSettingsPlugin extends Plugin {

    private LocationAccuracyHelper locationAccuracyHelper;
    private BluetoothHelper bluetoothHelper;
    private ActivityResultLauncher<IntentSenderRequest> locationActivityResultLauncher;
    private ActivityResultLauncher<Intent> batteryOptimizationActivityResultLauncher;
    private ActivityResultLauncher<Intent> activityResultLauncher;

    @Override
    public void load() {
        super.load();

        // Initialize LocationAccuracyHelper
        locationActivityResultLauncher = getActivity().registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    PluginCall savedCall = getSavedCall();
                    if (savedCall == null) {
                        return; // Ensure there's no crash if the saved call is null
                    }
                    if (locationAccuracyHelper != null) {
                        locationAccuracyHelper.handleActivityResult(savedCall, result.getResultCode());
                    }
                }
        );
        locationAccuracyHelper = new LocationAccuracyHelper(locationActivityResultLauncher);

        // Setup for Bluetooth Helper
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothHelper = new BluetoothHelper(bluetoothAdapter, getActivity().registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    PluginCall savedCall = getSavedCall();
                    if (savedCall == null) {
                        return; // Ensure there's no crash if the saved call is null
                    }
                    // Handle Bluetooth enable result
                    if (result.getResultCode() == getActivity().RESULT_OK) {
                        JSObject ret = new JSObject();
                        ret.put("status", "enabled");
                        ret.put("userSelection", "Allow");
                        ret.put("message", "Bluetooth has been enabled successfully.");
                        savedCall.resolve(ret);
                    } else {
                        JSObject ret = new JSObject();
                        ret.put("status", "disabled");
                        ret.put("userSelection", "Deny");
                        ret.put("message", "Bluetooth enabling was cancelled or failed.");

                        savedCall.resolve(ret);
                    }
                }
        ));

        // Initialize BatteryOptimizationHelper
        batteryOptimizationActivityResultLauncher = getActivity().registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    PluginCall savedCall = getSavedCall();
                    if (savedCall == null) {
                        return; // Avoid issues with null savedCall
                    }
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // User allowed battery optimization to be disabled
                        JSObject successResult = new JSObject();
                        successResult.put("status", "success");
                        successResult.put("message", "Battery optimization successfully disabled.");
                        savedCall.resolve(successResult);
                    } else {
                        // User did not allow or cancelled
                        JSObject failureResult = new JSObject();
                        failureResult.put("status", "denied");
                        failureResult.put("message", "User denied or cancelled the battery optimization request.");
                        savedCall.reject(failureResult.toString());
                    }
                }
        );

        // Register the ActivityResultLauncher during plugin load (before the activity is resumed)
        ActivityResultLauncher<Intent> activityResultLauncher = getActivity().registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Handle the result of the battery optimization request
                    PluginCall savedCall = getSavedCall();
                    if (savedCall == null) {
                        return; // Avoid issues with null savedCall
                    }
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // User allowed battery optimization to be disabled
                        JSObject successResult = new JSObject();
                        successResult.put("status", "success");
                        successResult.put("message", "Battery optimization successfully disabled.");
                        savedCall.resolve(successResult);
                    } else {
                        // User did not allow or cancelled
                        JSObject failureResult = new JSObject();
                        failureResult.put("status", "denied");
                        failureResult.put("message", "User denied or cancelled the battery optimization request.");
                        savedCall.reject(failureResult.toString());
                    }
                }
        );
    }

    @PluginMethod
    public void locationAccuracy(PluginCall call) {
        saveCall(call); // Save the plugin call
        Context context = getContext();
        locationAccuracyHelper.checkLocationAccuracy(call, LocationServices.getSettingsClient(context));
    }

    @PluginMethod
    public void bluetoothEnable(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (getPermissionState("bluetoothConnect") != PermissionState.GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.BLUETOOTH_CONNECT)) {
                    requestPermissionForAlias("bluetoothConnect", call, "handleBluetoothPermission");
                } else {
                    showSettingsDialog(call);
                }
                return;
            }
        }

        if (bluetoothHelper.isBluetoothSupported()) {
            if (bluetoothHelper.isBluetoothEnabled()) {
                bluetoothHelper.resolveBluetoothAlreadyEnabled(call);
            } else {
                saveCall(call); // Save the plugin call
                bluetoothHelper.requestBluetoothEnable(call);
            }
        } else {
            bluetoothHelper.rejectBluetoothNotSupported(call);
        }
    }

    private void showSettingsDialog(PluginCall call) {
        new AlertDialog.Builder(getContext())
                .setTitle("Permission Required")
                .setMessage("Bluetooth permission is required to enable this feature. Please allow it in app settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    AppSettingsHelper.openAppSettings(getContext());
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                    call.reject("Bluetooth permission is required.");
                })
                .create()
                .show();
    }

    @PermissionCallback
    private void handleBluetoothPermission(PluginCall call) {
        if (getPermissionState("bluetoothConnect") == PermissionState.GRANTED) {
            if (bluetoothHelper.isBluetoothSupported()) {
                if (bluetoothHelper.isBluetoothEnabled()) {
                    bluetoothHelper.resolveBluetoothAlreadyEnabled(call);
                } else {
                    saveCall(call); // Save the plugin call
                    bluetoothHelper.requestBluetoothEnable(call);
                }
            } else {
                bluetoothHelper.rejectBluetoothNotSupported(call);
            }
        } else {
            call.reject("Permission denied for Bluetooth connectivity.");
        }
    }

//    @PluginMethod
//    public void setBatteryMode(PluginCall call) {
//        Context context = getContext();
//        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//        String packageName = context.getPackageName();
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
//                // Already unrestricted, so return success
//                JSObject result = new JSObject();
//                result.put("status", "success");
//                result.put("message", "Battery optimization is already disabled for this app.");
//                call.resolve(result);
//            } else {
//                try {
//                    saveCall(call); // Save the plugin call to handle result properly
//
//                    // Create the intent to request battery optimization exemption
//                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//                    intent.setData(Uri.parse("package:" + packageName));
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                    // Launch the intent using the registered ActivityResultLauncher
//                    activityResultLauncher.launch(intent);
//
//                    // Respond with the 'prompted' status immediately after launching the intent
//                    JSObject result = new JSObject();
//                    result.put("status", "prompted");
//                    result.put("message", "User prompted to disable battery optimization.");
//                    call.resolve(result);
//                } catch (Exception e) {
//                    JSObject result = new JSObject();
//                    result.put("status", "error");
//                    result.put("message", "Failed to request battery optimization: " + e.getMessage());
//                    call.reject(result.toString());
//                }
//            }
//        } else {
//            JSObject result = new JSObject();
//            result.put("status", "unsupported");
//            result.put("message", "Battery optimization settings are not supported on this Android version.");
//            call.reject(result.toString());
//        }
//    }


//    @PluginMethod
//    public void setBatteryMode(PluginCall call) {
//        Context context = getContext();
//        String packageName = context.getPackageName();
//        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
//                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//                intent.setData(Uri.parse("package:" + packageName));
//                try {
//                    context.startActivity(intent);
//                    call.resolve();
//                } catch (Exception e) {
//                    call.reject("Could not open battery optimization settings.");
//                }
//            } else {
//                call.reject("Battery optimization already ignored for this app.");
//            }
//        }
//    }


    @PluginMethod
    public void setBatteryMode(PluginCall call) {
        Context context = getContext();
        String packageName = context.getPackageName();
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));

                saveCall(call); // Save the call to handle it later
                try {
                    startActivityForResult(call, intent, "handleBatteryOptimizationResult");
                } catch (Exception e) {
                    call.reject("Could not open battery optimization settings.");
                }
            } else {
                JSObject response = new JSObject();
                response.put("status", "enabled");
                response.put("userSelection", "Allow");
                response.put("message","Battery optimization already ignored for this app.");
                call.resolve(response);            }
        } else {
            call.reject("Battery optimization settings are not available on this Android version.");
        }
    }


    @ActivityCallback
    private void handleBatteryOptimizationResult(PluginCall call, ActivityResult result) {
        if (call == null) {
            return;
        }

        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getContext().getPackageName();
            if (pm != null && pm.isIgnoringBatteryOptimizations(packageName)) {
                JSObject response = new JSObject();
                response.put("status", "enabled");
                response.put("userSelection", "Allow");
                response.put("message", "Battery optimization disabled successfully.");
                call.resolve(response);
            } else {
                JSObject response = new JSObject();
                response.put("status", "disabled");
                response.put("userSelection", "Deny");
                response.put("message", "Battery optimization not disabled by the user.");
                call.resolve(response);
//                call.reject("Battery optimization not disabled by the user.");
            }
        } else {
            call.reject("Battery optimization settings are not available on this Android version.");
        }
    }


}
