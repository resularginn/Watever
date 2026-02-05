package com.example.sutakipuygulamas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SetupProfileActivity extends AppCompatActivity {

    private String selectedGender = "";
    private int grayColor = Color.parseColor("#94A3B8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        EditText etName = findViewById(R.id.et_name);
        EditText etHeight = findViewById(R.id.et_height);
        EditText etWeight = findViewById(R.id.et_weight);
        Button btnMale = findViewById(R.id.btn_male);
        Button btnFemale = findViewById(R.id.btn_female);
        Button btnComplete = findViewById(R.id.btn_complete_setup);

        resetGenderButtons(btnMale, btnFemale);

        btnMale.setOnClickListener(v -> {
            selectedGender = "Erkek";
            btnMale.setBackgroundResource(R.drawable.selector_gender_button);
            btnMale.setTextColor(Color.WHITE);
            btnFemale.setBackgroundResource(0);
            btnFemale.setTextColor(grayColor);
        });

        btnFemale.setOnClickListener(v -> {
            selectedGender = "Kadın";
            btnFemale.setBackgroundResource(R.drawable.selector_gender_button);
            btnFemale.setTextColor(Color.WHITE);
            btnMale.setBackgroundResource(0);
            btnMale.setTextColor(grayColor);
        });

        btnComplete.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String hStr = etHeight.getText().toString().trim();
            String wStr = etWeight.getText().toString().trim();

            if (name.isEmpty() || hStr.isEmpty() || wStr.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedGender.isEmpty()) {
                Toast.makeText(this, "Lütfen cinsiyet seçiniz", Toast.LENGTH_SHORT).show();
                return;
            }

            int weight = Integer.parseInt(wStr);
            int height = Integer.parseInt(hStr);
            int calculatedGoal = weight * 35;

            SharedPreferences pref = getSharedPreferences("WateverData", MODE_PRIVATE);
            pref.edit()
                    .putString("userName", name)
                    .putInt("weight", weight)
                    .putInt("height", height)
                    .putString("gender", selectedGender)
                    .putInt("goal", calculatedGoal)
                    .putBoolean("isRegistered", true)
                    .apply();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void resetGenderButtons(Button male, Button female) {
        male.setBackgroundResource(0);
        male.setTextColor(grayColor);
        female.setBackgroundResource(0);
        female.setTextColor(grayColor);
    }
}
