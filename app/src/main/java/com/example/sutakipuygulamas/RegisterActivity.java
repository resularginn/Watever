package com.example.sutakipuygulamas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etName = findViewById(R.id.et_name);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        EditText etPasswordConfirm = findViewById(R.id.et_password_confirm);
        Button btnRegister = findViewById(R.id.btn_register);
        TextView btnGoToLogin = findViewById(R.id.btn_go_to_login);

        // Zaten hesabın var mı? Giriş Yap'a basınca geri dön.
        btnGoToLogin.setOnClickListener(v -> finish());

        // KAYIT OL BUTONU
        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etPasswordConfirm.getText().toString().trim();

            // 1. Basit Kontroller
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Şifreler eşleşmiyor!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Verileri Kaydet (Ad, Mail, Şifre)
            SharedPreferences.Editor editor = getSharedPreferences("WateverData", MODE_PRIVATE).edit();
            editor.putString("userName", name); // <-- İSMİ BURADA KAYDEDİYORUZ
            editor.putString("userEmail", email);
            editor.putString("userPassword", password);
            // DİKKAT: "isRegistered" burada TRUE YAPILMAZ! SetupActivity yapacak.
            editor.apply();

            Toast.makeText(this, "Kayıt Başarılı! Şimdi profilini oluşturalım.", Toast.LENGTH_SHORT).show();

            // 3. Boy/Kilo Ekranına Yönlendir
            Intent intent = new Intent(this, SetupActivity.class);
            // Geri tuşuna basınca tekrar kayıt ekranına dönmesin diye flag ekleyebiliriz
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}