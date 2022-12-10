package com.MAB.BLE_Nav;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

public class StartupPage extends AppCompatActivity {
    Button btn_start_nav;
    Button btn_setup;

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup_page);

        btn_start_nav = findViewById(R.id.btn_start);
        btn_setup = findViewById(R.id.btn_setup);

        btn_start_nav.setOnClickListener(v -> {
            Intent intent = new Intent(StartupPage.this, Navigation.class);
            startActivity(intent);
        });
        btn_setup.setOnClickListener(v -> {
            Intent intent = new Intent(StartupPage.this, SetupSelection.class);
            startActivity(intent);
        });

        checkPermissions(StartupPage.this, this);

        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        if (btAdapter == null) {
            // Device doesn't support Bluetooth
            showWAR("Current device does not support bluetooth.", "exit");
        }
        btScanner = btAdapter.getBluetoothLeScanner();

        if (!btAdapter.isEnabled()){
            ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            showWAR("Bluetooth must be switched on for the system.", "exit");
                        }
                    });
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activityResultLauncher.launch(intent);
        }
    }

    public void showWAR(String msg, String mode){
        DialogFragment popup = new WarningMessage(msg, mode);
        popup.show(getSupportFragmentManager(), "Warning Message." );
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static void checkPermissions(Activity activity, Context context){
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.VIBRATE,
        };

        if(!hasPermissions(context, PERMISSIONS)){
            ActivityCompat.requestPermissions( activity, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}