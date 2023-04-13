package edu.psu.pjm6196.inventorymanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import edu.psu.pjm6196.inventorymanager.db.Barcode;
import edu.psu.pjm6196.inventorymanager.db.BarcodeDatabase;
import edu.psu.pjm6196.inventorymanager.db.BarcodeViewModel;

public class BarcodesListActivity extends AppCompatActivity {

    private BarcodeViewModel barcodeViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcodes_list);

        // setup toolbar
        setSupportActionBar(findViewById(R.id.toolbar));

        RecyclerView recyclerView = findViewById(R.id.listBarcodes);
        BarcodeListAdapter adapter = new BarcodeListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        barcodeViewModel = new ViewModelProvider(this).get(BarcodeViewModel.class);
        barcodeViewModel.getAllBarcodes().observe(this, adapter::setBarcodes);
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

    public void editMaterial(int barcode_id) {
        // TODO: tell AddBarcodeActivity which barcode_id to use
        startActivity(new Intent(this, AddBarcodeActivity.class));
    }

    public class BarcodeListAdapter extends RecyclerView.Adapter<BarcodeListAdapter.BarcodeViewHolder> {

        class BarcodeViewHolder extends RecyclerView.ViewHolder {
            private final TextView titleView;
            private Barcode barcode;

            private BarcodeViewHolder(View view) {
                super(view);
                titleView = view.findViewById(R.id.barcodeTitle);

                // show material info fragment if clicked
                view.setOnClickListener(v -> displayMaterial(barcode.id));
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