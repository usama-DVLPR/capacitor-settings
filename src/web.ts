import { WebPlugin } from '@capacitor/core';

import type { CapacitorSettingsPlugin } from './definitions';

export class CapacitorSettingsWeb extends WebPlugin implements CapacitorSettingsPlugin {
  /**
   * This method is not implemented for Web or iOS platforms.
   * It throws an error to indicate platform incompatibility.
   */
  async locationAccuracy(): Promise<{ status: string; userSelection: string; message:string }> {
    throw new Error('CapacitorSettings: locationAccuracy is only available on Android.');
  }

 
  async bluetoothEnable(): Promise<{ 
    status: string; 
    userSelection: string;
    message:string;
  }> {
    throw new Error('CapacitorSettings: bluetoothEnable is only available on Android.');
  }


  // async batteryOptimizationSettings(): Promise<{ 
  //   status: string; 
  //   userSelection: string;
  //   message:string;
  // }>{
  //   throw new Error('CapacitorSettings: batteryOptimizationSettings is only available on Android.');

  // };

  setBatteryMode(): Promise<{
    status: string; 
    userSelection: string; 
    message:string;
  }>{
        throw new Error('CapacitorSettings: setBatteryMode is only available on Android.');

  };
}
