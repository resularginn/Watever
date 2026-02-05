package com.example.sutakipuygulamas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. KONTROL: Kullanıcı zaten kayıtlı mı?
        SharedPreferences pref = getSharedPreferences("WateverData", MODE_PRIVATE);
        if (pref.getBoolean("isRegistered", false)) {
            // Evet, kayıtlı. O zaman bu ekranı ve Login ekranını atla, direkt Main'e git.
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Kayıtlı değilse bu tatlı ekranı göster
        setContentView(R.layout.activity_onboarding);

        Button btnStart = findViewById(R.id.btn_start);
        // OnboardingActivity içindeki buton tıklama olayı:
        btnStart.setOnClickListener(v -> {
            startActivity(new Intent(OnboardingActivity.this, SetupProfileActivity.class));
            finish();
        });
    }
}