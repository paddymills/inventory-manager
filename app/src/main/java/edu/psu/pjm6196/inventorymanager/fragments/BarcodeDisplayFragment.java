package edu.psu.pjm6196.inventorymanager.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import edu.psu.pjm6196.inventorymanager.AddBarcodeActivity;
import edu.psu.pjm6196.inventorymanager.db.Barcode;
import edu.psu.pjm6196.inventorymanager.db.BarcodeDatabase;

public class BarcodeDisplayFragment extends DialogFragment {

    protected static final String TAG = "BarcodeDisplayFragment";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        assert getArguments() != null;
        Barcode barcode = getArguments().getParcelable("barcode", Barcode.class);

        ActivityResultLauncher<Intent> editMaterial = registerForActivityResult(
            new ActivityResultContract<Intent, Boolean>() {
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, Intent intent) {
                    intent.putExtra("mode", AddBarcodeActivity.EDIT_MODE);
                    intent.putExtra("barcode", barcode);
                    return intent;
                }

                @Override
                public Boolean parseResult(int resultCode, @Nullable Intent intent) {
                    return resultCode == Activity.RESULT_OK;
                }
            },
            wasBarcodeUpdated -> {
                Log.d(TAG, "Barcode updated: " + wasBarcodeUpdated);
                if (wasBarcodeUpdated)
                    Toast.makeText(getContext(), "Barcode updated", Toast.LENGTH_SHORT).show();
            });

        builder
            .setTitle("Material Info")
            .setMessage(
                "Barcode ID: " + barcode.id_hash + "\n" +
                    "Material: " + barcode.material.material_master + "\n" +
                    "Grade: " + barcode.material.grade + "\n" +
                    "Location: " + barcode.material.location + "\n" +
                    "Heat #:" + barcode.material.heat_number + "\n" +
                    "PO #:" + barcode.material.po_number
            )
            .setPositiveButton("Edit", (dialog, id) -> {
                // TODO: do we need to persist the open dialog that launched this?
                editMaterial.launch(new Intent(getContext(), AddBarcodeActivity.class));
            })
            .setNegativeButton("Delete", (dialog, id) -> delete(barcode))
            .setNeutralButton("Return", (dialog, id) -> {
            });

        return builder.create();
    }

    private void delete(Barcode barcode) {
        new AlertDialog.Builder(getContext())
            .setTitle("Confirm delete")
            .setMessage("Are you sure you want to delete barcode " + barcode.id_hash)
            .setPositiveButton("Yes", (d, i) -> {
                BarcodeDatabase.delete(barcode);

                // TODO: make toast of delete confirmation (having trouble with this because of the context)
                Toast.makeText(getContext(), barcode.id_hash + " deleted.", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("No", (d, i) -> {
            })
            .show();
    }
}
