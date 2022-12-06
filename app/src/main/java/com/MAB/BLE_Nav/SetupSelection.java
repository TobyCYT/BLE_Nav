package com.MAB.BLE_Nav;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

public class SetupSelection extends AppCompatActivity {

    Button btn_arrival;
    Button btn_signage;
    Button btn_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_selection);

        btn_arrival = findViewById(R.id.arrivalMode);
        btn_signage = findViewById(R.id.signageMode);
        btn_back = findViewById(R.id.btn_back);

        btn_arrival.setOnClickListener(v -> {
            Intent intent = new Intent(SetupSelection.this, ArrivalModeSetup.class);
            startActivity(intent);
        });
        btn_signage.setOnClickListener(v -> {
            Intent intent = new Intent(SetupSelection.this, SignageModeSetup.class);
            startActivity(intent);
        });
        btn_back.setOnClickListener(v -> {
            Intent intent = new Intent(SetupSelection.this, StartupPage.class);
            startActivity(intent);
        });
    }

    public void showPopup() {
        DialogFragment popup = new not_implemented_popup();
        popup.show(getSupportFragmentManager(), "Not Implemented Error");
    }
}