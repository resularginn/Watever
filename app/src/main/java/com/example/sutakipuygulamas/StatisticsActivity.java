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

        // --- HAFTALIK GRAFİK (Pazartesi Başlangıçlı) ---
        Calendar cal = Calendar.getInstance();

        // Takvimi bu haftanın Pazartesine ayarla
        // (Eğer bugün Pazar(1) ise, Android takvimine göre yeni haftadır, biz onu önceki Pzt'ye çekeceğiz)
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        // Calendar.MONDAY = 2, Calendar.SUNDAY = 1

        int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday); // Şu anki tarihten Pazartesiye geri git

        int[] weeklyIntakes = new int[7];
        String[] dayLabels = {"Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"};
        int maxVal = 0;
        long totalWeekly = 0;
        int activeDays = 0;

        // 7 Günlük Döngü (Pzt -> Paz)
        for (int i = 0; i < 7; i++) {
            String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            int amount = pref.getInt(dateKey + "_total", 0);

            weeklyIntakes[i] = amount;
            totalWeekly += amount;
            if(amount > 0) activeDays++;

            if (amount > maxVal) maxVal = amount;

            cal.add(Calendar.DAY_OF_YEAR, 1); // Bir sonraki güne geç
        }

        // --- ORTALAMA HESABI ---
        // Eğer hiç içilmediyse 0'a bölme hatası olmasın
        // Ortalamayı 7 güne mi yoksa içilen günlere mi böleceğin sana kalmış.
        // Genelde "Günlük" ortalama için 7'ye bölünür.
        long average = totalWeekly / 7;
        ((TextView)findViewById(R.id.tv_weekly_avg)).setText(String.format(Locale.getDefault(), "%,d", average));

        // --- BU AY HESABI ---
        calculateMonthlyTotal();

        // --- SERİ HESABI ---
        calculateStreak();

        // --- GRAFİĞİ ÇİZ ---
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
        int containerHeight = dpToPx(140); // Grafik alanı yüksekliğinden yazı payını düşüyoruz
        int barHeight = (int) (((float) amount / max) * containerHeight);
        if (amount > 0 && barHeight < dpToPx(5)) barHeight = dpToPx(5); // Min yükseklik

        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(dpToPx(12), barHeight); // Bar kalınlığı
        barParams.setMargins(0,0,0, dpToPx(8));
        bar.setLayoutParams(barParams);
        bar.setBackgroundResource(R.drawable.bg_bar_blue);

        // Eğer miktar 0 ise barı gri veya görünmez yapabilirsin, şimdilik boş kalsın.
        if (amount == 0) bar.setBackgroundColor(0x00000000); // Görünmez

        // Gün Yazısı
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(12);
        tv.setTextColor(0xFF94A3B8); // Gri renk
        tv.setGravity(Gravity.CENTER);

        column.addView(bar);
        column.addView(tv);
        chartContainer.addView(column);
    }

    private void calculateMonthlyTotal() {
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int maxDay = cal.get(Calendar.DAY_OF_MONTH); // Bugün ayın kaçıysa oraya kadar topla (veya ay sonuna kadar döngü kur)

        // Ayın 1'ine git
        cal.set(Calendar.DAY_OF_MONTH, 1);

        long totalMonth = 0;

        // Bugüne kadar olanları topla
        for(int i = 0; i < maxDay; i++) {
            String key = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            totalMonth += pref.getInt(key + "_total", 0);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Litreye çevir (Örn: 15400 ml -> 15,4 L)
        double liters = totalMonth / 1000.0;
        ((TextView)findViewById(R.id.tv_month_total)).setText(String.format(Locale.getDefault(), "%.1f L", liters));
    }

    private void calculateStreak() {
        // Geriye doğru say, 0 bulana kadar
        Calendar cal = Calendar.getInstance();
        int streak = 0;

        // Bugün içtiyse seriye dahil et, içmediyse dünden kontrol et
        // Basit mantık: Dünden geriye gidelim.
        // (Gelişmiş mantıkta bugünü de kontrol edebilirsin)

        for(int i=0; i<365; i++) { // 1 yıl geriye bakmak yeterli
            String key = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            int val = pref.getInt(key + "_total", 0);

            // Bugün su içilmemişse ve döngü başındaysak seriyi kırma, belki gün bitmedi.
            // Ama biz katı kural uygulayalım: 0 ise seri biter.
            if (val >= pref.getInt("goal", 2500)) { // Sadece hedefe ulaşınca mı seri? Yoksa su içince mi?
                // Genelde "Hedefe Ulaşma" serisidir. Ama resimde sadece "Seri" diyor.
                // Biz "Su içtiği günler" serisi yapalım (val > 0)
                streak++;
            } else if (i > 0) {
                // Dün içmediyse seri biter. Bugün içmediyse henüz gün bitmediği için pas geçilebilir ama basit tutalım.
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