package com.usama.plugins.capacitorsettings;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
public class BluetoothHelper {

    private final BluetoothAdapter bluetoothAdapter;
    private final ActivityResultLauncher<Intent> bluetoothActivityResultLauncher;


    public boolean hasBluetoothConnectPermission(Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
    }
    public BluetoothHelper(BluetoothAdapter bluetoothAdapter, ActivityResultLauncher<Intent> bluetoothActivityResultLauncher) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.bluetoothActivityResultLauncher = bluetoothActivityResultLauncher;
    }

    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public void requestBluetoothEnable(PluginCall call) {
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothActivityResultLauncher.launch(enableBtIntent);
        } else {
            JSObject ret = new JSObject();
            ret.put("success", true);
            ret.put("message", "Bluetooth is already enabled.");
            call.resolve(ret);
        }
    }

    public void resolveBluetoothAlreadyEnabled(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("success", true);
        ret.put("message", "Bluetooth is already enabled.");
        call.resolve(ret);
    }

    public void rejectBluetoothNotSupported(PluginCall call) {
        call.reject("Device does not support Bluetooth");
    }
}
