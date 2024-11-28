import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(CapacitorSettingsPlugin)
public class CapacitorSettingsPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "CapacitorSettingsPlugin"
    public let jsName = "CapacitorSettings"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = CapacitorSettings()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }

    @objc func locationAccuracy(_ call: CAPPluginCall) {
        // No operation on iOS
        call.resolve([
            "status": "not-supported",
            "userSelection": "none",
            "message": "This method is not supported on iOS."
        ])
    }
    @objc func bluetoothEnable(_ call: CAPPluginCall) {
        // No operation on iOS
        call.resolve([
            "status": "not-supported",
            "userSelection": "none",
            "message": "This method is not supported on iOS."
        ])
    }
     @objc func setBatteryMode(_ call: CAPPluginCall) {
        // No operation on iOS
        call.resolve([
            "status": "not-supported",
            "userSelection": "none",
            "message": "This method is not supported on iOS."
        ])
    }
}
