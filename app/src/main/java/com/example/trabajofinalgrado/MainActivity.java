package com.example.trabajofinalgrado;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import android.util.Patterns;


// TODO por qué uso cada tipo de layout?

public class MainActivity extends AppCompatActivity {

    private ClientesDBHelper dbHelper;
    private ArrayAdapter<String> adaptador;
    private ListView listaClientes;
    private List<Cliente> listaDatosClientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new ClientesDBHelper(this);
        listaClientes = findViewById(R.id.listaClientes);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean oscuro = prefs.getBoolean("tema_oscuro", false);
        if (oscuro) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        SearchView searchView = findViewById(R.id.searchView);
        // para los problemas, antes habia que hacer click para que funcionara
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adaptador.getFilter().filter(newText);
                return true;
            }
        });

        Button btnVerHistorial = findViewById(R.id.btnVerHistorial);
        btnVerHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistorialActivity.class);
            startActivity(intent);
        });

        Button btnInsertar = findViewById(R.id.btnInsertar);
        btnInsertar.setOnClickListener(v -> {
            mostrarDialogoInsertar();
        });

        mostrarClientes();
    }

    private void mostrarClientes() {
        listaDatosClientes = dbHelper.obtenerClientes();
        List<String> nombres = new ArrayList<>();
        for (Cliente c : listaDatosClientes) {
            nombres.add(c.toString());
        }
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombres);
        listaClientes.setAdapter(adaptador);

        listaClientes.setOnItemClickListener((parent, view, position, id) -> {
            Cliente cliente = listaDatosClientes.get(position);
            Intent intent = new Intent(this, PedidosActivity.class);
            intent.putExtra("clienteId", cliente.id);
            startActivity(intent);
        });

        listaClientes.setOnItemLongClickListener((parent, view, position, id) -> {
            Cliente cliente = listaDatosClientes.get(position);
            mostrarDialogoEditarEliminar(cliente);
            return true;
        });
    }

    private void mostrarDialogoEditarEliminar(Cliente cliente) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones para " + cliente.nombre);
        builder.setItems(new CharSequence[]{"Modificar", "Eliminar"}, (dialog, which) -> {
            if (which == 0) {
                mostrarDialogoModificar(cliente);
            } else {
                if (dbHelper.clienteTienePedidos(cliente.id)) {
                    new AlertDialog.Builder(this)
                            .setTitle("Cliente con pedidos")
                            .setMessage("Este cliente tiene pedidos asociados. ¿Quieres eliminarlo junto con sus pedidos?")
                            .setPositiveButton("Eliminar todo", (d, w) -> {
                                dbHelper.eliminarCliente(cliente.id);
                                Toast.makeText(this, "Cliente eliminado", Toast.LENGTH_SHORT).show();
                                mostrarClientes();
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                } else {
                    dbHelper.eliminarCliente(cliente.id);
                    Toast.makeText(this, "Cliente eliminado", Toast.LENGTH_SHORT).show();
                    mostrarClientes();
                }
            }
        });
        builder.show();
    }

    private void mostrarDialogoModificar(Cliente cliente) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        EditText inputNombre = new EditText(this);
        inputNombre.setHint("Nombre");
        inputNombre.setText(cliente.nombre);
        layout.addView(inputNombre);

        EditText inputEmail = new EditText(this);
        inputEmail.setHint("Email");
        inputEmail.setText(cliente.email);
        layout.addView(inputEmail);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Modificar cliente");
        builder.setView(layout);
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevoNombre = inputNombre.getText().toString();
            String nuevoEmail = inputEmail.getText().toString();
            dbHelper.modificarCliente(cliente.id, nuevoNombre, nuevoEmail);
            Toast.makeText(this, "Cliente actualizado", Toast.LENGTH_SHORT).show();
            mostrarClientes();
        });
        builder.setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        inputNombre.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void mostrarDialogoInsertar() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        EditText inputNombre = new EditText(this);
        inputNombre.setHint("Nombre");
        layout.addView(inputNombre);

        EditText inputEmail = new EditText(this);
        inputEmail.setHint("Email");
        layout.addView(inputEmail);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nuevo cliente");
        builder.setView(layout);
        builder.setPositiveButton("Insertar", (dialog, which) -> {
            String nombre = inputNombre.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();

            if(nombre.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Debe rellenar todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Introduce un email válido", Toast.LENGTH_SHORT).show();
                return;
            }

            dbHelper.insertarCliente(nombre, email);
            Toast.makeText(this, "Cliente insertado", Toast.LENGTH_SHORT).show();
            mostrarClientes();
        });
        builder.setNegativeButton("Cancelar", null);

        // Podría dar problemas?
        AlertDialog dialog = builder.create();
        inputNombre.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_configuracion) {
            Intent intent = new Intent(this, ConfiguracionActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}