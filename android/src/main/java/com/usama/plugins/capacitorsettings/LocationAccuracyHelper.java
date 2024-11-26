package com.usama.plugins.capacitorsettings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

public class LocationAccuracyHelper {

    private final ActivityResultLauncher<IntentSenderRequest> locationActivityResultLauncher;
    private PluginCall savedCall;

    public LocationAccuracyHelper(ActivityResultLauncher<IntentSenderRequest> launcher) {
        this.locationActivityResultLauncher = launcher;
    }

    public void checkLocationAccuracy(PluginCall call, SettingsClient client) {
        this.savedCall = call;


        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
            JSObject response = new JSObject();
            response.put("status", "enabled");
            response.put("userSelection", "Already Enabled");
            response.put("message", "The feature is already enabled and operational.");
            call.resolve(response);
        });

        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(
                            resolvable.getResolution().getIntentSender()
                    ).build();
                    locationActivityResultLauncher.launch(intentSenderRequest);
                } catch (Exception ex) {
                    call.reject("Could not prompt user for location accuracy", ex);
                }
            } else {
                call.reject("Location settings are not satisfied.");
            }
        });
    }

    public void handleActivityResult(PluginCall call, int resultCode) {
        if (savedCall == null) {
            return;
        }

        JSObject response = new JSObject();
        if (resultCode == android.app.Activity.RESULT_OK) {
            response.put("status", "enabled");
            response.put("userSelection", "Turn On");
            response.put("message", "The feature was successfully enabled.");
        } else {
            response.put("status", "disabled");
            response.put("userSelection", "No Thanks");
            response.put("message", "The feature was not enabled as the user declined.");
        }
        savedCall.resolve(response);
        savedCall = null;
    }
}
