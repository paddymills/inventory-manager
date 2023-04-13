package edu.psu.pjm6196.inventorymanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import edu.psu.pjm6196.inventorymanager.db.Barcode;
import edu.psu.pjm6196.inventorymanager.db.BarcodeViewModel;

public class BarcodesListActivity extends AppCompatActivity {

    private BarcodeViewModel barcodeViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcodes_list);

        setSupportActionBar(findViewById(R.id.toolbar));

        RecyclerView recyclerView = findViewById(R.id.listBarcodes);
        BarcodeListAdapter adapter = new BarcodeListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        barcodeViewModel = new ViewModelProvider(this).get(BarcodeViewModel.class);
        barcodeViewModel.getAllBarcodes().observe(this, adapter::setBarcodes);
    }


    public class BarcodeListAdapter extends RecyclerView.Adapter<BarcodeListAdapter.BarcodeViewHolder> {

        class BarcodeViewHolder extends RecyclerView.ViewHolder {
            private final TextView titleView;
            private Barcode barcode;

            private BarcodeViewHolder(View view) {
                super(view);
                titleView = view.findViewById(R.id.barcodeTitle);

                // set listeners
                titleView.setOnClickListener(v -> {
                    Log.d("BarcodeViewHolder", "BarcodeViewHolder: clicked");
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
}