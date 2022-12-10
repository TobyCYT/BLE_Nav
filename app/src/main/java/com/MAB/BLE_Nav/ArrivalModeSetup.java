package com.MAB.BLE_Nav;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArrivalModeSetup extends AppCompatActivity implements SensorEventListener {
    Button btn_cancel;
    Button btn_confirm;
    EditText name;
    EditText threshold;
    EditText heading;
    TextView headingDisplay;
    Spinner beacon;
    ArrayAdapter<String> adapter;
    List<String> list = new ArrayList<>();

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;

    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];
    double headingValue;

    FirebaseDatabase db;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrival_mode_setup);

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        btn_cancel = findViewById(R.id.btn_back);
        btn_confirm = findViewById(R.id.btn_submit);
        name = findViewById(R.id.name);
        threshold = findViewById(R.id.threshold);
        headingDisplay = findViewById(R.id.headingDisplay);
        heading = findViewById(R.id.heading);
        beacon = findViewById(R.id.beaconDropDown);

        list.add("Please Select Beacon");
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        beacon.setAdapter(adapter);

        btn_cancel.setOnClickListener(v -> {
            stopScanning();
            Intent intent = new Intent(ArrivalModeSetup.this, SetupSelection.class);
            startActivity(intent);
        });

        btn_confirm.setOnClickListener(v -> {
            String[] selection;
            String data;
            String name;
            String threshold;
            String heading;
            heading = this.heading.getText().toString();
            name = this.name.getText().toString();
            threshold = this.threshold.getText().toString();
            selection = this.beacon.getSelectedItem().toString().split("\\|", 0);//[0] name, [1] address, [2] rssi

            if (beacon != null && !beacon.toString().equals("Please Select Beacon") && !name.isEmpty() && !threshold.isEmpty()) {
                //data = name + "|" + selection[1] + "|" + threshold + "|arrival";//[0] name, [1] mac address, [2] threshold, [3] mode
                //writeToFile(data, this);
                data = name + "|" + threshold + "|arrival";//[0] name, [1] threshold, [2] mode
                if (!heading.isEmpty()){
                    data += "|" + heading;
                }
                ref.child(selection[1]).setValue(data);
                Intent intent = new Intent(ArrivalModeSetup.this, SetupSelection.class);
                startActivity(intent);
            } else {
                showWarning("Please make sure name, threshold and beacon are valid.", "normal");
                return;
            }
            stopScanning();
        });

        db = FirebaseDatabase.getInstance("https://bluetooth-test-app-mab-default-rtdb.asia-southeast1.firebasedatabase.app/");
        ref = db.getReference("Beacon Config Database");

        startScanning();
    }

    // Device scan callback.
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            try{
                if (result.getDevice().getName() == null) return;
                String name = result.getDevice().getName();
                String address = result.getDevice().getAddress();
                Integer rssi = result.getRssi();

                String entry = name + "|" + address + "|" + rssi;

                boolean updated = false;
                for (String e : list) {
                    String[] tempArray= e.split("\\|", 0);//[0] name, [1] address, [2] rssi

                    if (Objects.equals(name, tempArray[0]) && Objects.equals(address, tempArray[1])) {
                        if (!rssi.toString().equals(tempArray[2])) list.set(list.indexOf(e), entry);
                        updated = true;
                    }
                }
                if (!updated) list.add(entry);

                Integer idx = beacon.getSelectedItemPosition();

                adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_item, list);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
                beacon.setAdapter(adapter);

                beacon.setSelection(idx);
            }catch (SecurityException e){
                Log.v("error", e.toString());
            }
        }
    };

    public void startScanning() {
        AsyncTask.execute(() -> {
            try{
                ScanSettings scanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                        .setReportDelay(0L)
                        .build();
                btScanner.startScan(null, scanSettings, leScanCallback);
            }catch (SecurityException e){
                Log.v("error", e.toString());
            }
        });
    }

    public void stopScanning() {
        AsyncTask.execute(() -> {
            try{
                btScanner.stopScan(leScanCallback);
            }catch (SecurityException e){
                Log.v("error", e.toString());
            }
        });
    }

    public void showWarning(String msg, String mode){
        DialogFragment popup = new WarningMessage(msg, mode);
        popup.show(getSupportFragmentManager(), "Warning to user" );
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
        Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField,
                    SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);
    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }
        updateOrientationAngles();
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(rotationMatrix, null,
                accelerometerReading, magnetometerReading);

        // "rotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        // "orientationAngles" now has up-to-date information.
        headingValue = orientationAngles[0];
        if (headingValue < 0){
            headingValue = ((2*Math.PI + headingValue)/(2*Math.PI)) * 360;
        }else{
            headingValue = (headingValue/Math.PI) * 180;
        }

        headingDisplay.setText("Current Heading: "+ headingValue);
    }

}