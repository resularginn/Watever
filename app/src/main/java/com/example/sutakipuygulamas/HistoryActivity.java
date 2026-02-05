package com.example.sutakipuygulamas;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<MainActivity.WaterEntry> waterList = new ArrayList<>();
    private int currentWater = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        SharedPreferences pref = getSharedPreferences("WateverData", MODE_PRIVATE);
        loadWaterData(pref);

        recyclerView = findViewById(R.id.recycler_view_full_history);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new HistoryAdapter(waterList, this::deleteWaterEntry);
            recyclerView.setAdapter(adapter);
        }
    }

    private void deleteWaterEntry(int position) {
        if (position >= 0 && position < waterList.size()) {
            MainActivity.WaterEntry entry = waterList.get(position);

        
            currentWater -= entry.amount;
            if (currentWater < 0) currentWater = 0;

     
            waterList.remove(position);

       
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, waterList.size());

       
            saveWaterData();

            Toast.makeText(this, "Kayıt silindi", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveWaterData() {
        SharedPreferences pref = getSharedPreferences("WateverData", MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        try {
            JSONArray jsonArray = new JSONArray();
            for (MainActivity.WaterEntry entry : waterList) {
                JSONObject obj = new JSONObject();
                obj.put("amount", entry.amount);
                obj.put("type", entry.type);
                obj.put("time", entry.time);
                obj.put("icon", entry.iconResId);
                jsonArray.put(obj);
            }

        
            pref.edit()
                    .putInt(today + "_total", currentWater)
                    .putString(today + "_history", jsonArray.toString())
                    .apply();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadWaterData(SharedPreferences pref) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        currentWater = pref.getInt(today + "_total", 0);
        waterList.clear();

        String listJson = pref.getString(today + "_history", "[]");
        try {
            JSONArray jsonArray = new JSONArray(listJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                int iconId = obj.has("icon") ? obj.getInt("icon") : R.drawable.ic_glass;
                waterList.add(new MainActivity.WaterEntry(obj.getInt("amount"), obj.getString("type"), obj.getString("time"), iconId));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }


    public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<MainActivity.WaterEntry> list;
        private OnItemDeleteListener deleteListener;

        public HistoryAdapter(List<MainActivity.WaterEntry> list, OnItemDeleteListener deleteListener) {
            this.list = list;
            this.deleteListener = deleteListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MainActivity.WaterEntry entry = list.get(position);
            holder.tvAmount.setText(entry.amount + " ml");
            holder.tvType.setText(entry.type);
            holder.tvTime.setText(entry.time);
            holder.imgIcon.setImageResource(entry.iconResId);

            holder.btnDelete.setOnClickListener(v -> {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    deleteListener.onDelete(currentPos);
                }
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAmount, tvType, tvTime;
            ImageView btnDelete, imgIcon;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvAmount = itemView.findViewById(R.id.tv_amount);
                tvType = itemView.findViewById(R.id.tv_type);
                tvTime = itemView.findViewById(R.id.tv_time);
                btnDelete = itemView.findViewById(R.id.btn_delete);
                imgIcon = itemView.findViewById(R.id.img_icon);
            }
        }
    }
    public interface OnItemDeleteListener { void onDelete(int position); }
}
