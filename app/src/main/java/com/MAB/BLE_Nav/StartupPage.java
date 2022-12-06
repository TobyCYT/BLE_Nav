package com.MAB.BLE_Nav;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

public class StartupPage extends AppCompatActivity {
    Button btn_start_nav;
    Button btn_setup;

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
    }

    public void showPopup(){
        DialogFragment popup = new not_implemented_popup();
        popup.show(getSupportFragmentManager(), "Not Implemented Error" );
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public static void checkPermissions(Activity activity, Context context){
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_PRIVILEGED,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
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