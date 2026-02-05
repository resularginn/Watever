package com.example.sutakipuygulamas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class SetupActivity extends AppCompatActivity {

    private Boolean isMale = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        EditText etWeight = findViewById(R.id.et_weight);
        EditText etHeight = findViewById(R.id.et_height);
        Button btnMale = findViewById(R.id.btn_male);
        Button btnFemale = findViewById(R.id.btn_female);
        Button btnCalculate = findViewById(R.id.btn_calculate);

        updateGenderUI(btnMale, btnFemale);

        btnMale.setOnClickListener(v -> {
            isMale = true;
            updateGenderUI(btnMale, btnFemale);
        });

        btnFemale.setOnClickListener(v -> {
            isMale = false;
            updateGenderUI(btnMale, btnFemale);
        });

        btnCalculate.setOnClickListener(v -> {
            String wStr = etWeight.getText().toString().trim();
            String hStr = etHeight.getText().toString().trim();

            // 1. KONTROL: Kilo (En üstteki alan)
            if (wStr.isEmpty()) {
                Toast.makeText(this, "Lütfen kilonuzu girin!", Toast.LENGTH_SHORT).show();
                return; // İşlemi durdur
            }

            // 2. KONTROL: Boy (Ortadaki alan)
            if (hStr.isEmpty()) {
                Toast.makeText(this, "Lütfen boyunuzu girin!", Toast.LENGTH_SHORT).show();
                return; // İşlemi durdur
            }

            // 3. KONTROL: Cinsiyet (En alttaki seçim)
            if (isMale == null) {
                Toast.makeText(this, "Lütfen cinsiyetinizi seçin!", Toast.LENGTH_SHORT).show();
                return; // İşlemi durdur
            }

            // Hata yoksa hesaplamaya geç
            try {
                int weight = Integer.parseInt(wStr);
                int height = Integer.parseInt(hStr);

                // Basit su hedefi formülü
                double goal = weight * 35;
                if (isMale) { goal = goal * 1.10; }

                SharedPreferences.Editor editor = getSharedPreferences("WateverData", MODE_PRIVATE).edit();
                editor.putInt("weight", weight);
                editor.putInt("height", height);
                editor.putInt("goal", (int) goal);
                editor.putString("gender", isMale ? "Erkek" : "Kadın");
                editor.putBoolean("isRegistered", true);
                editor.apply();

                startActivity(new Intent(this, MainActivity.class));
                finish();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Lütfen geçerli sayılar girin!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateGenderUI(Button male, Button female) {
        int gray = ContextCompat.getColor(this, R.color.text_gray);
        int white = ContextCompat.getColor(this, R.color.white);

        boolean maleSelected = (isMale != null && isMale);
        boolean femaleSelected = (isMale != null && !isMale);

        male.setSelected(maleSelected);
        male.setTextColor(maleSelected ? white : gray);

        female.setSelected(femaleSelected);
        female.setTextColor(femaleSelected ? white : gray);
    }
}