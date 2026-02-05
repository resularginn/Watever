package com.example.sutakipuygulamas;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences pref;
    private int dailyGoal;
    private int currentWater;
    private MainHistoryAdapter historyAdapter;
    private List<WaterEntry> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);

        pref = getSharedPreferences("WateverData", MODE_PRIVATE);

        if (!pref.getBoolean("isRegistered", false)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        loadTakipUI();
    }

    private void loadTakipUI() {
        setContentView(R.layout.activity_main);
        setupNavigation(1);

        dailyGoal = pref.getInt("goal", 2500);
        String todayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        currentWater = pref.getInt(todayKey + "_total", 0);

        TextView tvTitle = findViewById(R.id.tv_welcome_title);
        if (tvTitle != null) {
            String savedName = pref.getString("userName", "Kullanıcı");
            String firstName = savedName.split(" ")[0];
            if(firstName.length() > 0)
                firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1);
            tvTitle.setText("Selam, " + firstName + "! 👋");
        }

        updateDashboardUI();
        setupRecyclerView();
        updateHistoryList();

        findViewById(R.id.btn_add_200).setOnClickListener(v -> addWater(200, "BARDAK", R.drawable.ic_glass));
        findViewById(R.id.btn_add_330).setOnClickListener(v -> addWater(330, "KÜÇÜK ŞİŞE", R.drawable.ic_drop_fill));
        findViewById(R.id.btn_add_500).setOnClickListener(v -> addWater(500, "BÜYÜK ŞİŞE", R.drawable.ic_wave));
    }


    private void loadStatisticsUI() {
        setContentView(R.layout.activity_statistics);
        setupNavigation(2);
        calculateAndDrawStats();
    }


    private void loadAyarlarUI() {
        setContentView(R.layout.activity_settings);
        setupNavigation(3);
        updateSettingsUI();
        setupSettingsLogic();
    }
    

    private void updateDashboardUI() {
        dailyGoal = pref.getInt("goal", 2500);

        TextView tvCurrent = findViewById(R.id.tv_current_water);
        if (tvCurrent != null) tvCurrent.setText(String.valueOf(currentWater));

        TextView tvGoalLabel = findViewById(R.id.tv_goal_label);
        if (tvGoalLabel != null) tvGoalLabel.setText("Hedef: " + dailyGoal + " ml");

        ProgressBar pb = findViewById(R.id.progress_bar);
        TextView tvPercent = findViewById(R.id.tv_percent);
        TextView tvRemaining = findViewById(R.id.tv_remaining);

        if (pb != null) {
            if (dailyGoal == 0) dailyGoal = 2500;
            int targetProgress = (currentWater * 100) / dailyGoal;
            targetProgress = Math.min(targetProgress, 100);

            ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), targetProgress);
            animation.setDuration(300);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();

            if (tvPercent != null) tvPercent.setText("%" + targetProgress);
        }

        if (tvRemaining != null) {
            int remaining = Math.max(0, dailyGoal - currentWater);
            tvRemaining.setText("Kalan: " + remaining + " ml");
        }
    }

    private void setupSettingsLogic() {
        findViewById(R.id.btn_edit_name).setOnClickListener(v -> showNameDialog());
        findViewById(R.id.btn_edit_goal).setOnClickListener(v -> showEditDialog("Hedef", "goal"));
        findViewById(R.id.btn_edit_weight).setOnClickListener(v -> showEditDialog("Ağırlık", "weight"));
        findViewById(R.id.btn_edit_height).setOnClickListener(v -> showEditDialog("Boy", "height"));
        findViewById(R.id.btn_edit_gender).setOnClickListener(v -> showGenderDialog());

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Her Şeyi Sıfırla")
                    .setMessage("Tüm veriler silinecek. Emin misin?")
                    .setPositiveButton("Evet, Sıfırla", (d, w) -> {
                        pref.edit().clear().apply();
                        startActivity(new Intent(this, OnboardingActivity.class));
                        finish();
                    })
                    .setNegativeButton("İptal", null)
                    .show();
        });
    }

    private void updateSettingsUI() {
        if (findViewById(R.id.set_goal) == null) return;
        ((TextView)findViewById(R.id.set_name)).setText(pref.getString("userName", "Kullanıcı"));
        ((TextView)findViewById(R.id.set_goal)).setText(pref.getInt("goal", 2500) + " ml");
        ((TextView)findViewById(R.id.set_weight)).setText(pref.getInt("weight", 70) + " kg");
        ((TextView)findViewById(R.id.set_height)).setText(pref.getInt("height", 170) + " cm");
        ((TextView)findViewById(R.id.set_gender)).setText(pref.getString("gender", "Erkek"));
    }

    private void calculateAndDrawStats() {
        LinearLayout chartContainer = findViewById(R.id.chart_container);
        if (chartContainer == null) return;
        chartContainer.removeAllViews();

        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday);

        int[] intakes = new int[7];
        String[] labels = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"};
        int maxVal = 0;
        long totalWeekly = 0;

        for (int i = 0; i < 7; i++) {
            String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            int amount = pref.getInt(dateKey + "_total", 0);
            intakes[i] = amount;
            totalWeekly += amount;
            if (amount > maxVal) maxVal = amount;
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        TextView tvAvg = findViewById(R.id.tv_weekly_avg);
        if(tvAvg != null) tvAvg.setText(String.format(Locale.getDefault(), "%,d", totalWeekly / 7));

        int scaleMax = Math.max(maxVal, dailyGoal);
        for (int i = 0; i < 7; i++) {
            drawBar(chartContainer, intakes[i], scaleMax, labels[i]);
        }
        calculateMonthlyTotal();
        calculateStreak();
    }

    private void drawBar(LinearLayout container, int amount, int max, String label) {
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        column.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

        column.setOnClickListener(v -> {
            Toast.makeText(this, label + " Günü: " + amount + " ml", Toast.LENGTH_SHORT).show();
        });

        View bar = new View(this);
        int barHeight = (int) (((float) amount / max) * dpToPx(140));
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(dpToPx(12), Math.max(dpToPx(2), barHeight));
        barParams.setMargins(0, 0, 0, dpToPx(8));
        bar.setLayoutParams(barParams);
        bar.setBackgroundResource(R.drawable.bg_bar_blue);
        if (amount == 0) bar.setVisibility(View.INVISIBLE);

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(12);
        tv.setTextColor(0xFF94A3B8);
        tv.setGravity(Gravity.CENTER);
        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        column.addView(bar); column.addView(tv);
        container.addView(column);
    }


    private void showNameDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("İsim Soyisim");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setText(pref.getString("userName", ""));
        b.setView(input);
        b.setPositiveButton("Kaydet", (d, w) -> {
            if(!input.getText().toString().isEmpty()){
                pref.edit().putString("userName", input.getText().toString().trim()).apply();
                updateSettingsUI();
            }
        });
        b.show();
    }

    private void showEditDialog(String t, String k) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(t);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(pref.getInt(k, 0)));
        b.setView(input);
        b.setPositiveButton("Kaydet", (d, w) -> {
            if(!input.getText().toString().isEmpty()){
                pref.edit().putInt(k, Integer.parseInt(input.getText().toString())).apply();
                updateSettingsUI();
            }
        });
        b.show();
    }

    private void showGenderDialog() {
        String[] opts = {"Erkek", "Kadın"};
        new AlertDialog.Builder(this).setTitle("Cinsiyet").setItems(opts, (d, w) -> {
            pref.edit().putString("gender", opts[w]).apply();
            updateSettingsUI();
        }).show();
    }


    private void addWater(int amt, String type, int icon) {
        currentWater += amt;
        String key = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        pref.edit().putInt(key + "_total", currentWater).apply();

        String historyKey = key + "_history";
        String jsonHistory = pref.getString(historyKey, "[]");
        try {
            JSONArray historyArray = new JSONArray(jsonHistory);
            JSONObject entry = new JSONObject();
            entry.put("amount", amt); entry.put("type", type); entry.put("time", new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date())); entry.put("icon", icon);
            historyArray.put(entry);
            pref.edit().putString(historyKey, historyArray.toString()).apply();
        } catch (JSONException e) { e.printStackTrace(); }

        updateDashboardUI();
        updateHistoryList();
    }

    private void setupNavigation(int active) {
        View n1 = findViewById(R.id.nav_takip), n2 = findViewById(R.id.nav_istatistik), n3 = findViewById(R.id.nav_ayarlar);
        if(n1 != null) n1.setOnClickListener(v -> loadTakipUI());
        if(n2 != null) n2.setOnClickListener(v -> loadStatisticsUI());
        if(n3 != null) n3.setOnClickListener(v -> loadAyarlarUI());

        int blue = ContextCompat.getColor(this, R.color.blue_primary), gray = Color.parseColor("#94A3B8");
        ((ImageView)findViewById(R.id.nav_icon_takip)).setColorFilter(active==1?blue:gray);
        ((ImageView)findViewById(R.id.nav_icon_istatistik)).setColorFilter(active==2?blue:gray);
        ((ImageView)findViewById(R.id.nav_icon_ayarlar)).setColorFilter(active==3?blue:gray);
    }

    private void calculateMonthlyTotal() {
        Calendar c = Calendar.getInstance();
        int today = c.get(Calendar.DAY_OF_MONTH);
        c.set(Calendar.DAY_OF_MONTH, 1);
        long total = 0;
        for(int i=0; i<today; i++) {
            total += pref.getInt(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.getTime()) + "_total", 0);
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        ((TextView)findViewById(R.id.tv_month_total)).setText(String.format(Locale.getDefault(), "%.1f L", total/1000.0));
    }

    private void calculateStreak() {
        Calendar c = Calendar.getInstance();
        int streak = 0;
        for(int i=0; i<365; i++) {
            if(pref.getInt(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.getTime()) + "_total", 0) > 0) streak++;
            else if(i>0) break;
            c.add(Calendar.DAY_OF_YEAR, -1);
        }
        ((TextView)findViewById(R.id.tv_streak)).setText(streak + " Gün");
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.recycler_view_history);
        if(rv != null) {
            historyList = new ArrayList<>();
            historyAdapter = new MainHistoryAdapter(historyList);
            rv.setLayoutManager(new LinearLayoutManager(this));
            rv.setAdapter(historyAdapter);
        }
    }

    private void updateHistoryList() {
        if(historyAdapter == null) return;
        String todayKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String jsonHistory = pref.getString(todayKey + "_history", "[]");
        historyList.clear();
        try {
            JSONArray array = new JSONArray(jsonHistory);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                historyList.add(new WaterEntry(obj.getInt("amount"), obj.getString("type"), obj.getString("time"), obj.optInt("icon", R.drawable.ic_drop_fill)));
            }
        } catch (JSONException e) { e.printStackTrace(); }
        Collections.reverse(historyList);
        historyAdapter.notifyDataSetChanged();
    }

    private int dpToPx(int dp) { return (int)(dp * getResources().getDisplayMetrics().density); }


    private class MainHistoryAdapter extends RecyclerView.Adapter<MainHistoryAdapter.ViewHolder> {
        private List<WaterEntry> list;

        public MainHistoryAdapter(List<WaterEntry> l) { this.list = l; }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int v) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_history, p, false));
        }

        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) {
            WaterEntry e = list.get(p);

            h.tvAmount.setText(e.amount + " ml");
            h.tvType.setText(e.type);
            h.tvTime.setText(e.time);
            h.imgIcon.setImageResource(e.iconResId);


            h.btnDelete.setOnClickListener(v -> {
                int pos = h.getAdapterPosition();

   
                if (pos != RecyclerView.NO_POSITION && pos < list.size()) {

   
                    WaterEntry deletedItem = list.get(pos);
                    currentWater = Math.max(0, currentWater - deletedItem.amount);
                    list.remove(pos);
                    notifyItemRemoved(pos);

                    if (pos < list.size()) {
                        notifyItemRangeChanged(pos, list.size() - pos);
                    }

                    updateDatabaseAndUI();
                }
            });
        }

        private void updateDatabaseAndUI() {
            String key = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            pref.edit().putInt(key + "_total", currentWater).apply();
            updateDashboardUI();

            JSONArray arr = new JSONArray();
            try {
                List<WaterEntry> saveList = new ArrayList<>(list);
                Collections.reverse(saveList);
                for(WaterEntry we : saveList) {
                    JSONObject obj = new JSONObject();
                    obj.put("amount", we.amount);
                    obj.put("type", we.type);
                    obj.put("time", we.time);
                    obj.put("icon", we.iconResId);
                    arr.put(obj);
                }
                pref.edit().putString(key + "_history", arr.toString()).apply();
            } catch (Exception ex) {}
        }

        @Override public int getItemCount() { return list.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvAmount, tvType, tvTime;
            ImageView imgIcon, btnDelete;

            ViewHolder(View v) {
                super(v);
                tvAmount = v.findViewById(R.id.tv_amount);
                tvType = v.findViewById(R.id.tv_type);
                tvTime = v.findViewById(R.id.tv_time);
                imgIcon = v.findViewById(R.id.img_icon);
                btnDelete = v.findViewById(R.id.btn_delete);
            }
        }
    }

    public static class WaterEntry { public int amount; public String type, time; public int iconResId; public WaterEntry(int a, String t, String ti, int i) { amount=a; type=t; time=ti; iconResId=i; } }
}
