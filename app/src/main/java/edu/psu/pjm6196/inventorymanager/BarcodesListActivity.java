package edu.psu.pjm6196.inventorymanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class BarcodesListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcodes_list);
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

        @Override
        public BarcodeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View viewItem = inflater.inflate(R.layout.barcode_list_item, parent, false);
            return new BarcodeViewHolder(viewItem);
        }

        @Override
        public void onBindViewHolder(BarcodeViewHolder holder, int position) {
            if ( barcodes == null ) {
                holder.titleView.setText("...fetching...");
            }

            else {
                Barcode current = barcodes.get(position);
                holder.barcode = current;
                holder.titleView.setText(current.id_hash + "(" + current.material.material_master + ")");
            }
        }

        @Override
        public int getItemCount() {
            if ( barcodes == null )
                return 0;

            return barcodes.size();
        }

    }
}