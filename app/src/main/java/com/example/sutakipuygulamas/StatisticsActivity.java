package com.example.sutakipuygulamas;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StatisticsActivity extends AppCompatActivity {

    private LinearLayout chartContainer;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        pref = getSharedPreferences("WateverData", MODE_PRIVATE);
        chartContainer = findViewById(R.id.chart_container);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        calculateAndDrawStats();
    }

    private void calculateAndDrawStats() {
        chartContainer.removeAllViews();

        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

        int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday);

        int[] weeklyIntakes = new int[7];
        String[] dayLabels = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"};
        int maxVal = 0;
        long totalWeekly = 0;
        int activeDays = 0;

        for (int i = 0; i < 7; i++) {
            String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            int amount = pref.getInt(dateKey + "_total", 0);

            weeklyIntakes[i] = amount;
            totalWeekly += amount;
            if(amount > 0) activeDays++;

            if (amount > maxVal) maxVal = amount;

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        long average = totalWeekly / 7;
        ((TextView)findViewById(R.id.tv_weekly_avg)).setText(String.format(Locale.getDefault(), "%,d", average));

        calculateMonthlyTotal();

        calculateStreak();

        int goal = pref.getInt("goal", 2500);
        int scaleMax = Math.max(maxVal, goal);
        if(scaleMax == 0) scaleMax = 2500;

        for (int i = 0; i < 7; i++) {
            drawBar(weeklyIntakes[i], scaleMax, dayLabels[i]);
        }
    }

    private void drawBar(int amount, int max, String label) {
        // Sütun Container
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        column.setLayoutParams(params);
        column.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

        // Mavi Bar
        View bar = new View(this);
        int containerHeight = dpToPx(140);
        int barHeight = (int) (((float) amount / max) * containerHeight);
        if (amount > 0 && barHeight < dpToPx(5)) barHeight = dpToPx(5);

        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(dpToPx(12), barHeight);
        barParams.setMargins(0,0,0, dpToPx(8));
        bar.setLayoutParams(barParams);
        bar.setBackgroundResource(R.drawable.bg_bar_blue);

        if (amount == 0) bar.setBackgroundColor(0x00000000);

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(12);
        tv.setTextColor(0xFF94A3B8);
        tv.setGravity(Gravity.CENTER);

        column.addView(bar);
        column.addView(tv);
        chartContainer.addView(column);
    }

    private void calculateMonthlyTotal() {
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int maxDay = cal.get(Calendar.DAY_OF_MONTH);

        cal.set(Calendar.DAY_OF_MONTH, 1);

        long totalMonth = 0;

        for(int i = 0; i < maxDay; i++) {
            String key = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            totalMonth += pref.getInt(key + "_total", 0);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        double liters = totalMonth / 1000.0;
        ((TextView)findViewById(R.id.tv_month_total)).setText(String.format(Locale.getDefault(), "%.1f L", liters));
    }

    private void calculateStreak() {
        Calendar cal = Calendar.getInstance();
        int streak = 0;

        for(int i=0; i<365; i++) {
            String key = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            int val = pref.getInt(key + "_total", 0);

            if (val >= pref.getInt("goal", 2500)) {
                streak++;
            } else if (i > 0) {
                break;
            }
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }

        ((TextView)findViewById(R.id.tv_streak)).setText(streak + " Gün");
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
