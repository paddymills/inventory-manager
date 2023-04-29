package edu.psu.pjm6196.inventorymanager;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import edu.psu.pjm6196.inventorymanager.db.Barcode;
import edu.psu.pjm6196.inventorymanager.db.BarcodeDatabase;
import edu.psu.pjm6196.inventorymanager.db.BarcodeViewModel;

public class BarcodesListActivity extends CustomAppCompatActivity {

    private BarcodeViewModel barcodeViewModel;
    private boolean filtered = false;
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.query_menu, menu);

        if (filtered)
            menu.getItem(0).setIcon(R.drawable.ic_nofilter);
        else
            menu.getItem(0).setIcon(R.drawable.ic_filter);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_filter) {
            return filterMaterial();
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayMaterial(int id) {
        BarcodeDatabase.getBarcode(id, barcode -> {
            Bundle args = new Bundle();
            args.putInt("barcode_id", barcode.id);
            args.putString("material", barcode.material.material_master);
            args.putString("grade", barcode.material.grade);
            args.putString("loc", barcode.material.location);
            args.putString("heat", barcode.material.heat_number);
            args.putString("po", barcode.material.po_number);

            BarcodeDisplayFragment barcodeDisplay = new BarcodeDisplayFragment();
            barcodeDisplay.setArguments(args);
            barcodeDisplay.show(getSupportFragmentManager(), "barcodeDisplay");
        });
    }

    public boolean filterMaterial() {
        // toggle filtering
        filtered = !filtered;

        // set icon
        MenuItem menu_item = findViewById(R.id.menu_filter);
        if (filtered)
            menu_item.setIcon(R.drawable.ic_nofilter);
        else
            menu_item.setIcon(R.drawable.ic_filter);

        // set recycler view to have filtered/non-filtered list
        RecyclerView recycler = findViewById(R.id.listBarcodes);
        BarcodeListAdapter adapter = new BarcodeListAdapter(this);
        recycler.setAdapter(adapter);
        barcodeViewModel = new ViewModelProvider(this).get(BarcodeViewModel.class);
        barcodeViewModel.filterBarcodes(filtered);

        barcodeViewModel.getAllBarcodes().observe(this, adapter::setBarcodes);

        // display toast of what happened
        String message = filtered ? "Barcodes filtered" : "Filter removed";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        return true;
    }

    public void editMaterial(int barcode_id) {
        Intent add_barcode = new Intent(this, AddBarcodeActivity.class);
        add_barcode.putExtra("barcode_id", barcode_id);

        startActivity(add_barcode);
    }

    public class BarcodeListAdapter extends RecyclerView.Adapter<BarcodeListAdapter.BarcodeViewHolder> {

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
                if ( !prefs.getBoolean("display_location", true) )
                      extras_location.setVisibility(View.GONE);
                if ( !prefs.getBoolean("display_grade", true) )
                    extras_grade.setVisibility(View.GONE);
                if ( !prefs.getBoolean("display_heat", true) )
                    extras_heat.setVisibility(View.GONE);
                if ( !prefs.getBoolean("display_po", true) )
                    extras_po.setVisibility(View.GONE);

                // show material info fragment if clicked
                view.setOnClickListener(v -> {
                    this.expanded = !this.expanded;
//                    displayMaterial(barcode.id);

                    extrasView.setVisibility(this.expanded ? View.VISIBLE : View.GONE);
                });
                view.setOnLongClickListener(v -> {
                    displayMaterial(barcode.id);

                    return true;
                });
            }
        }

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
            if ( barcodes == null ) {
                holder.titleView.setText(R.string.fetch_data_title);
            }

            else {
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
            if ( barcodes == null )
                return 0;

            return barcodes.size();
        }

        void setBarcodes(List<Barcode> barcodes) {
            this.barcodes = barcodes;
            notifyDataSetChanged();
        }

    }

    public static class BarcodeDisplayFragment extends DialogFragment {
        int barcode_id;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            barcode_id = getArguments().getInt("barcode_id");
            final String material = getArguments().getString("material");
            final String grade = getArguments().getString("grade");
            final String loc = getArguments().getString("loc");
            final String heat = getArguments().getString("heat");
            final String po = getArguments().getString("po");

            builder
                .setTitle("Material Info")
                .setMessage(
                    "Material: " + material + "\n" +
                    "Grade: " + grade + "\n" +
                    "Location: " + loc + "\n" +
                    "Heat #:" + heat + "\n" +
                    "PO #:" + po
                )
                .setPositiveButton("Edit", (dialog, id) -> ((BarcodesListActivity) getActivity()).editMaterial(barcode_id))
//                    .setNegativeButton("Delete", (dialog, id) -> {});
                .setNeutralButton("Return", (dialog, id) -> {});

            return builder.create();
        }
    }
}