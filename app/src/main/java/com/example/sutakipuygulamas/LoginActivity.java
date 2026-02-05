package com.example.sutakipuygulamas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnLogin = findViewById(R.id.btn_login);
        TextView tvRegisterLink = findViewById(R.id.tv_register_link); // XML'deki ID'yi buraya yazdık

        // "Kayıt Ol" yazısına tıklanınca Kayıt Ekranına git
        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // "Giriş Yap" butonuna tıklanınca
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show();
            } else {
                // Normalde burada sunucu kontrolü yapılır. Biz başarılı varsayıyoruz.
                Toast.makeText(this, "Giriş Başarılı!", Toast.LENGTH_SHORT).show();

                // Kullanıcı adını SharedPreferences'a "Kullanıcı" olarak kaydedelim (Giriş yapanın adı belli olmadığı için)
                // Eğer gerçek bir backend olsaydı ismi oradan çekecektik.
                SharedPreferences.Editor editor = getSharedPreferences("WateverData", MODE_PRIVATE).edit();
                if (!getSharedPreferences("WateverData", MODE_PRIVATE).contains("userName")) {
                    editor.putString("userName", "Kullanıcı");
                    editor.apply();
                }

                // Kurulum (Setup) ekranına git
                startActivity(new Intent(this, SetupActivity.class));
                finish();
            }
        });
    }
}