package com.MAB.BLE_Nav;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class WarningMessage extends DialogFragment {
    String msg;
    String mode;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning")
                .setMessage(msg);
        if (mode.equals("normal")) builder.setNeutralButton("Ok", (dialog, id) -> dialog.dismiss());
        else if (mode.equals("exit")) builder.setNegativeButton("Ok", (dialog, id) -> System.exit(0));
        // Create the AlertDialog object and return it
        Dialog popup = builder.create();
        popup.setCancelable(false);
        popup.setCanceledOnTouchOutside(false);
        return popup;
    }

    WarningMessage(String msg, String mode){
        this.msg = msg;
        this.mode = mode;
    }
}
