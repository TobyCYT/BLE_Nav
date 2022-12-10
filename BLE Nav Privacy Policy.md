## Privacy policy

I hereby state, to the best of my knowledge and belief, that I have not programmed this app to collect any personally identifiable information.

### Explanation of permissions requested in the app

The list of permissions required by the app can be found in the `AndroidManifest.xml` file:

https://github.com/TobyCYT/BLE_Nav/blob/main/app/src/main/AndroidManifest.xml

<br/>

|                 Permission                  | Why it is required                                                                                                        |
|:-------------------------------------------:|---------------------------------------------------------------------------------------------------------------------------|
|       `android.permission.BLUETOOTH`        | Required for app to function properly, this app uses Bluetooth Low Energy Signals to operate.                             |
|    `android.permission.BLUETOOTH_ADMIN`     | Required for app to initiate device discovery.                                                                            |
|   `android.permission.BLUETOOTH_CONNECT`    | Required for app to ask user to enable bluetooth service.                                                                 |
|     `android.permission.BLUETOOTH_SCAN`     | Allows for app to look for Bluetooth Low Energy Signals.                                                                  |
|  `android.permission.ACCESS_FINE_LOCATION`  | On Android 11 and lower, a Bluetooth scan could potentially be used to gather information about the location of the user. |
| `android.permission.ACCESS_COARSE_LOCATION` | On Android 9 and lower, use ACCESS_COARSE_LOCATION permission instead of the ACCESS_FINE_LOCATION permission.             |
|        `android.permission.VIBRATE`         | Enables the app to vibrate device to notify user.                                                                         |


 <hr style="border:1px solid gray">