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
import android.speech.tts.TextToSpeech;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class Navigation extends AppCompatActivity implements SensorEventListener{

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;

    private SensorManager sensorManager;
    private final float[] accelerometerReading = new float[3];
    private final float[] magnetometerReading = new float[3];

    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    Button startScanningButton;
    Button stopScanningButton;
    Button btn_back;
    TextView peripheralTextView;
    TextToSpeech textToSpeech;

    double heading;
    double pitch;
    double roll;
    List<BLEBeacons> BLEBeacons = new ArrayList<>();
    List<String> list = new ArrayList<>();

    FirebaseDatabase db;
    DatabaseReference ref;
    ChildEventListener childEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        peripheralTextView = findViewById(R.id.PeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod());

        startScanningButton = findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(v -> startScanning());

        stopScanningButton = findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(v -> stopScanning());

        btn_back = findViewById(R.id.btn_back);

        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        textToSpeech = new TextToSpeech(getApplicationContext(), i -> {

            // if No error is found then only it will run
            if(i!=TextToSpeech.ERROR){
                // To Choose language of speech
                textToSpeech.setLanguage(Locale.UK);
            }
        });

        db = FirebaseDatabase.getInstance("https://bluetooth-test-app-mab-default-rtdb.asia-southeast1.firebasedatabase.app/");
        try{
            db.setPersistenceEnabled(true);
        }catch (Exception e){
            Log.v("error", e.toString());
        }
        ref = db.getReference("Beacon Config Database");
        ref.keepSynced(true);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                String key = dataSnapshot.getKey();
                String value = dataSnapshot.getValue(String.class);
                String keyValuePair = key+"|"+value;
                String[] data = keyValuePair.split("\\|", 0);
                /*
                [0] - Mac Address
                [1] - Name
                [2] - Threshold
                [3] - Operating Mode
                [4] - Heading
                [5] - pos_y signage
                [6] - neg_y signage
                [7] - pos_x signage
                [8] - neg_x signage
                */
                if (data[3].equals("arrival")){
                    if (data.length == 5) {
                        BLEBeacons.add(new BLEBeacons(data[1], data[0], Double.valueOf(data[2]),
                                Double.valueOf(data[4]), textToSpeech));
                    }else{
                        BLEBeacons.add(new BLEBeacons(data[1], data[0], Double.valueOf(data[2]),
                                null, textToSpeech));
                    }
                }else if (data[3].equals("signage")){
                    String[] tmp = new String[4];
                    tmp[0] = data[5];
                    tmp[1] = data[6];
                    tmp[2] = data[7];
                    tmp[3] = data[8];
                    BLEBeacons.add(new BLEBeacons(data[1], data[0], Double.valueOf(data[2]),
                            Double.valueOf(data[4]), tmp, textToSpeech));
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                String key = dataSnapshot.getKey();
                String value = dataSnapshot.getValue(String.class);
                String keyValuePair = key+"|"+value;

                Log.v("Test", "onChildChanged:" + key + " " + value);

                String[] data = keyValuePair.split("\\|", 0);
                /*
                [0] - Mac Address
                [1] - Name
                [2] - Threshold
                [3] - Operating Mode
                [4] - Heading
                [5] - pos_y signage
                [6] - neg_y signage
                [7] - pos_x signage
                [8] - neg_x signage
                */
                for (BLEBeacons b : BLEBeacons){
                    if (b.macAddress.equals(key)){
                        BLEBeacons.remove(b);
                        if (data[3].equals("arrival")){
                            if (data.length == 5) {
                                BLEBeacons.add(new BLEBeacons(data[1], data[0], Double.valueOf(data[2]),
                                        Double.valueOf(data[4]), textToSpeech));
                            }else{
                                BLEBeacons.add(new BLEBeacons(data[1], data[0], Double.valueOf(data[2]),
                                        null, textToSpeech));
                            }
                        }else if (data[3].equals("signage")){
                            String[] tmp = new String[4];
                            tmp[0] = data[5];
                            tmp[1] = data[6];
                            tmp[2] = data[7];
                            tmp[3] = data[8];
                            BLEBeacons.add(new BLEBeacons(data[1], data[0], Double.valueOf(data[2]),
                                    Double.valueOf(data[4]), tmp, textToSpeech));
                        }
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                String value = dataSnapshot.getValue(String.class);

                Log.v("Test", "onChildRemoved:" + key + " " + value);

                BLEBeacons.removeIf(b -> b.macAddress.equals(key));
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String previousChildName) {
                String key = dataSnapshot.getKey();
                String value = dataSnapshot.getValue(String.class);

                Log.v("Test", "onChildMoved:" + key + " " + value);
                //Not Used
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.v("Test", "postComments:onCancelled", databaseError.toException());
                Toast.makeText(getApplicationContext(), "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        btn_back.setOnClickListener(v -> {
            stopScanning();
            Intent intent = new Intent(Navigation.this, StartupPage.class);
            startActivity(intent);
            ref.removeEventListener(childEventListener);
            finish();
        });

        list.add("Heading\t: ");
        list.add("Pitch\t: ");
        list.add("Roll\t: ");
    }

    @Override
    protected void onPause(){
        super.onPause();

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this);

        ref.removeEventListener(childEventListener);

        BLEBeacons.clear();
    }

    @Override
    protected void onResume(){
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

        ref.addChildEventListener(childEventListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            stopScanning();
            ref.removeEventListener(childEventListener);
        }
        return super.onKeyDown(keyCode, event);
    }

    // Device scan callback.
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (BLEBeacons != null){
                for (BLEBeacons e: BLEBeacons){
                    e.update(result, heading, pitch, roll, getApplicationContext());
                }
            }
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

                StringBuilder compiler = new StringBuilder();
                for (String e: list){
                    compiler.append(e).append("\n");
                }
                peripheralTextView.setText(compiler.toString());
            }catch (SecurityException e){
                Log.v("error", e.toString());
            }
            // auto scroll for text view
            final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0)
                peripheralTextView.scrollTo(0, scrollAmount);
        }
    };

    public void startScanning() {
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);
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
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(() -> {
            try{
                btScanner.stopScan(leScanCallback);
            }catch (SecurityException e){
                Log.v("error", e.toString());
            }
        });
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
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
        heading = orientationAngles[0];
        pitch = orientationAngles[1]; // 0 ~ -1
        roll = orientationAngles[2]; // +-15degree 345 ~ 15
        if (heading < 0){
            heading = ((2*Math.PI + heading)/(2*Math.PI)) * 360;
        }else{
            heading = (heading/Math.PI) * 180;
        }
        if (roll < 0){
            roll = ((2*Math.PI + roll)/(2*Math.PI)) * 360;
        }else{
            roll = (roll/Math.PI) * 180;
        }
        list.set(0, "Heading\t: " + heading);
        list.set(1, "Pitch\t: " + pitch);
        list.set(2, "Roll\t: " + roll);
        StringBuilder compiler = new StringBuilder();
        for (String e: list){
            compiler.append(e).append("\n");
        }
        peripheralTextView.setText(compiler.toString());
    }

}

