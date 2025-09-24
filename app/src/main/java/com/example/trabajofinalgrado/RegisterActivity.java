package com.example.trabajofinalgrado;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Patterns;

public class RegisterActivity extends AppCompatActivity {
    EditText editTextEmail, editTextPassword;
    Button btnRegister;
    ClientesDBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnRegister = findViewById(R.id.btnRegister);
        db = new ClientesDBHelper(this);

        btnRegister.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Introduce un correo electrónico válido", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.insertarUsuario(email, password)) {
                Toast.makeText(this, "Registro exitoso. Puedes iniciar sesión.", Toast.LENGTH_SHORT).show();
                finish(); // volver a LoginActivity
            } else {
                Toast.makeText(this, "El correo ya está registrado", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
