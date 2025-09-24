package com.example.trabajofinalgrado;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText editTextEmail, editTextPassword;
    Button btnLogin;
    TextView textViewOlvidarContrasena;
    ClientesDBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.btnLogin);
        textViewOlvidarContrasena = findViewById(R.id.textViewOlvidarContrasena);
        db = new ClientesDBHelper(this);

        btnLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (db.validarUsuario(email, password)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
            }
        });

        textViewOlvidarContrasena.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Introduce tu correo para recuperar la contraseña", Toast.LENGTH_SHORT).show();
            } else {
                String password = db.obtenerContrasena(email);
                if (password != null) {
                    EditText nuevaPassInput = new EditText(this);
                    nuevaPassInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    new AlertDialog.Builder(this)
                            .setTitle("Restablecer contraseña")
                            .setMessage("Introduce una nueva contraseña para tu cuenta:")
                            .setView(nuevaPassInput)
                            .setPositiveButton("Guardar", (dialog, which) -> {
                                String nuevaPass = nuevaPassInput.getText().toString().trim();
                                if (!nuevaPass.isEmpty()) {
                                    boolean actualizada = db.actualizarContrasena(email, nuevaPass);
                                    if (actualizada) {
                                        Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(this, "La contraseña no puede estar vacía", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                } else {
                    Toast.makeText(this, "Correo no registrado", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}