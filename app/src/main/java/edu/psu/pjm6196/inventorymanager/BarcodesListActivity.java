package edu.psu.pjm6196.inventorymanager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.psu.pjm6196.inventorymanager.db.Barcode;
import edu.psu.pjm6196.inventorymanager.db.BarcodeDatabase;
import edu.psu.pjm6196.inventorymanager.db.BarcodeViewModel;

public class BarcodesListActivity extends ActivityBase {

    private static final String TAG = "BarcodesListing";
    ActivityResultLauncher<Intent> getIds;
    ActivityResultLauncher<Intent> addMaterial;
    private BarcodeViewModel barcodeViewModel;
    // TODO: replace with newFiltered
    private boolean filtered = false;
    private ListFilterListener newFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcodes_list);

        RecyclerView recyclerView = findViewById(R.id.listBarcodes);
        BarcodeListAdapter adapter = new BarcodeListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        barcodeViewModel = new ViewModelProvider(this).get(BarcodeViewModel.class);
        barcodeViewModel.getAllBarcodes().observe(this, adapter::setBarcodes);

        if (savedInstanceState != null) {
            filtered = savedInstanceState.getBoolean("filtered", false);
            // TODO: restore filtered by specific listener
            filterMaterial(null);
        }

        Intent called = getIntent();
        if ( called != null && called.hasExtra("barcode_ids") ) {
            ArrayList<String> ids = called.getStringArrayListExtra("barcode_ids");
            Log.d(TAG, "Received barcode ids");
            filtered = true;
            filterMaterial(model -> model.filterByIdHashBarcodes(filtered, ids));
        }

        createActivityResultContracts();

        findViewById(R.id.btn_launch_scanner)
            .setOnClickListener(v -> {
                Intent intent = new Intent(this, ScanActivity.class);
                getIds.launch(intent);
            });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_barcodes_menu, menu);

        if (filtered)
            menu.findItem(R.id.menu_filter).setIcon(R.drawable.ic_nofilter);
        else
            menu.findItem(R.id.menu_filter).setIcon(R.drawable.ic_filter);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_filter) {
            // toggle filtering
            filtered = !filtered;

            return filterMaterial(null);
        }

        if (item.getItemId() == R.id.menu_add) {
            addMaterial.launch(new Intent(this, AddBarcodeActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle instanceState) {
        super.onSaveInstanceState(instanceState);
        instanceState.putBoolean("filtered", filtered);
    }

    @Override
    protected void onRestoreInstanceState(Bundle instanceState) {
        super.onRestoreInstanceState(instanceState);

        filtered = instanceState.getBoolean("filtered", false);
        if (filtered)
            filterMaterial(null);
    }

    private void createActivityResultContracts() {
        // TODO: refactor this so nothing returns to MainActivity to do work
        getIds = registerForActivityResult(
            new ActivityResultContract<Intent, ArrayList<String>>() {
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, Intent intent) {
                    intent.putExtra("calling_activity_intent", ScanActivity.CallingActivityIntent.FILTER_LIST);

                    return intent;
                }

                @Override
                public ArrayList<String> parseResult(int resultCode, @Nullable Intent intent) {
                    if (resultCode == RESULT_OK && intent != null)
                        return intent.getStringArrayListExtra("barcode_ids");

                    return null;
                }
            },
            result -> {
                Log.d(TAG, "Received ids: " + result);
                if (result != null) {
                    // TODO: filter list to either show barcode or show all with a particular attribute of scanned_barcode
                    filtered = true;
                    filterMaterial(model -> model.filterByIdHashBarcodes(filtered, result));
                }
            });
        addMaterial = registerForActivityResult(
            new ActivityResultContract<Intent, Boolean>() {
                @NonNull
                @Override
                public Intent createIntent(@NonNull Context context, Intent intent) {
                    intent.putExtra("calling_activity_intent", ScanActivity.CallingActivityIntent.ADD_MATERIAL);
                    return intent;
                }

                @Override
                public Boolean parseResult(int resultCode, @Nullable Intent intent) {
                    return resultCode == RESULT_OK;
                }
            },
            barcodeWasAdded -> {
                Log.d(TAG, "Barcode added: " + barcodeWasAdded);
                if (barcodeWasAdded)
                    Toast.makeText(this, "Barcode added", Toast.LENGTH_SHORT).show();
            });
    }

    public void displayMaterial(Barcode barcode) {
        Bundle args = new Bundle();
        args.putParcelable("barcode", barcode);

        BarcodeDisplayFragment barcodeDisplay = new BarcodeDisplayFragment();
        barcodeDisplay.setArguments(args);
        barcodeDisplay.show(getSupportFragmentManager(), "barcodeDisplay");
    }

    public boolean filterMaterial(ListFilterListener listener) {
        // set recycler view to have filtered/non-filtered list
        RecyclerView recycler = findViewById(R.id.listBarcodes);
        BarcodeListAdapter adapter = new BarcodeListAdapter(this);
        recycler.setAdapter(adapter);
        barcodeViewModel = new ViewModelProvider(this).get(BarcodeViewModel.class);

        if (listener == null)
            barcodeViewModel.filterBarcodes(filtered);
        else
            listener.setFilter(barcodeViewModel);


        barcodeViewModel.getAllBarcodes().observe(this, adapter::setBarcodes);

        // set icon
        Toolbar menu = findViewById(R.id.toolbar);
        MenuItem menu_item = menu.getMenu().findItem(R.id.menu_filter);
        if (filtered)
            menu_item.setIcon(R.drawable.ic_nofilter);
        else
            menu_item.setIcon(R.drawable.ic_filter);

        // display toast of what happened
        String message = filtered ? "Barcodes filtered" : "Filter removed";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        return true;
    }

    public interface ListFilterListener {
        void setFilter(BarcodeViewModel model);
    }

    public static class BarcodeDisplayFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            assert getArguments() != null;
            Barcode barcode = getArguments().getParcelable("barcode");

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
                        return resultCode == RESULT_OK;
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

    public class BarcodeListAdapter extends RecyclerView.Adapter<BarcodeListAdapter.BarcodeViewHolder> {

        private final LayoutInflater inflater;
        private List<Barcode> barcodes;

        public BarcodeListAdapter(Context ctx) {
            inflater = LayoutInflater.from(ctx);
        }

        @NonNull
        @Override
        public BarcodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View viewItem = inflater.inflate(R.layout.barcode_list_item, parent, false);
            return new BarcodeViewHolder(viewItem);
        }

        @Override
        public void onBindViewHolder(@NonNull BarcodeViewHolder holder, int position) {
            if (barcodes == null) {
                holder.titleView.setText(R.string.fetch_data_title);
            } else {
                Barcode current = barcodes.get(position);
                holder.barcode = current;
                holder.titleView.setText(current.title());
                holder.extrasView.setVisibility(holder.expanded ? View.VISIBLE : View.GONE);
                holder.extras_location.setText(String.format("%s: %s", getString(R.string.query_location_label), current.material.location));
                holder.extras_grade.setText(String.format("%s: %s", getString(R.string.query_grade_label), current.material.grade));
                holder.extras_heat.setText(String.format("%s: %s", getString(R.string.query_heat_label), current.material.heat_number));
                holder.extras_po.setText(String.format("%s: %s", getString(R.string.query_po_label), current.material.po_number));
            }
        }

        @Override
        public int getItemCount() {
            if (barcodes == null)
                return 0;

            return barcodes.size();
        }

        @SuppressLint("NotifyDataSetChanged")
        void setBarcodes(List<Barcode> barcodes) {
            this.barcodes = barcodes;
            notifyDataSetChanged();
        }

        class BarcodeViewHolder extends RecyclerView.ViewHolder {
            private final TextView titleView;
            private final ConstraintLayout extrasView;
            private final TextView extras_location;
            private final TextView extras_grade;
            private final TextView extras_heat;
            private final TextView extras_po;
            private Barcode barcode;
            private boolean expanded;

            private BarcodeViewHolder(View view) {
                super(view);
                titleView = view.findViewById(R.id.barcodeTitle);
                extrasView = view.findViewById(R.id.barcode_extras);
                extras_location = view.findViewById(R.id.barcode_location);
                extras_grade = view.findViewById(R.id.barcode_grade);
                extras_heat = view.findViewById(R.id.barcode_heat);
                extras_po = view.findViewById(R.id.barcode_po);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BarcodesListActivity.this);
//                SharedPreferences.Editor editor = prefs.edit();
                // TODO: hide attr if it is being filtered on (will need a setting added for this)
                if (!prefs.getBoolean("display_location", true))
                    extras_location.setVisibility(View.GONE);
                if (!prefs.getBoolean("display_grade", true))
                    extras_grade.setVisibility(View.GONE);
                if (!prefs.getBoolean("display_heat", true))
                    extras_heat.setVisibility(View.GONE);
                if (!prefs.getBoolean("display_po", true))
                    extras_po.setVisibility(View.GONE);

                // show material info fragment if clicked
                view.setOnClickListener(v -> {
                    this.expanded = !this.expanded;
//                    displayMaterial(barcode.id);

                    extrasView.setVisibility(this.expanded ? View.VISIBLE : View.GONE);
                });
                view.setOnLongClickListener(v -> {
                    displayMaterial(barcode);

                    return true;
                });
            }
        }

    }

    // TODO: filter fragment (need fields for material master, location, and heat number)
}