package com.example.trabajofinalgrado;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PedidosActivity extends AppCompatActivity {

    private ClientesDBHelper dbHelper;
    private int clienteId;
    private ListView listaPedidos;
    private ArrayAdapter<Pedido> adaptador;
    private List<Pedido> listaPedidosObj;
    private String fechaInicioSeleccionada;
    private String fechaFinSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);

        clienteId = getIntent().getIntExtra("clienteId", -1);
        dbHelper = new ClientesDBHelper(this);
        listaPedidos = findViewById(R.id.listaPedidos);
        Button btnAgregarPedido = findViewById(R.id.btnAgregarPedido);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean oscuro = prefs.getBoolean("tema_oscuro", false);
        if (oscuro) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnAgregarPedido.setOnClickListener(v -> mostrarDialogoAgregarPedido());
        listaPedidos.setOnItemLongClickListener((parent, view, position, id) -> {
            Pedido pedido = listaPedidosObj.get(position);
            mostrarDialogoOpciones(pedido);
            return true;
        });

        Button btnFiltrarRango = findViewById(R.id.btnFiltrarRango);
        btnFiltrarRango.setOnClickListener(v -> mostrarDialogoRangoFechas());

        Button btnLimpiarFiltros = findViewById(R.id.btnLimpiarFiltros);
        btnLimpiarFiltros.setOnClickListener(v -> {
            mostrarPedidos();
            Toast.makeText(this, "Filtros eliminados", Toast.LENGTH_SHORT).show();
        });

        Button btnEnviarResumen = findViewById(R.id.btnEnviarResumen);
        btnEnviarResumen.setOnClickListener(v -> enviarResumenPorEmail());

        mostrarPedidos();
    }

    private void mostrarPedidos() {
        listaPedidosObj = dbHelper.obtenerPedidosPorCliente(clienteId);
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaPedidosObj);
        listaPedidos.setAdapter(adaptador);
    }

    private void mostrarDialogoAgregarPedido() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        EditText inputDescripcion = new EditText(this);
        inputDescripcion.setHint("Descripción");
        layout.addView(inputDescripcion);

        EditText inputFecha = new EditText(this);
        inputFecha.setHint("Fecha (DD-MM-YYYY)");
        layout.addView(inputFecha);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Nuevo Pedido")
                .setView(layout)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String descripcion = inputDescripcion.getText().toString().trim();
                String fecha = inputFecha.getText().toString().trim();

                if (descripcion.isEmpty() || fecha.isEmpty()) {
                    Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!fecha.matches("\\d{2}-\\d{2}-\\d{4}")) {
                    Toast.makeText(this, "La fecha debe ser en formato DD-MM-YYYY", Toast.LENGTH_SHORT).show();
                    return;
                }

                dbHelper.insertarPedido(descripcion, fecha, clienteId);
                Toast.makeText(this, "Pedido guardado", Toast.LENGTH_SHORT).show();
                mostrarPedidos();
                dialog.dismiss();
            });
        });

        inputDescripcion.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void mostrarDialogoOpciones(Pedido pedido) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona una opción")
                .setItems(new CharSequence[]{"Modificar", "Eliminar"}, (dialog, which) -> {
                    if (which == 0) {
                        mostrarDialogoModificar(pedido);
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("Confirmar eliminación")
                                .setMessage("¿Eliminar el pedido: " + pedido.getDescripcion() + "?")
                                .setPositiveButton("Sí", (d, w) -> {
                                    dbHelper.eliminarPedido(pedido.getId());
                                    Toast.makeText(this, "Pedido eliminado", Toast.LENGTH_SHORT).show();
                                    mostrarPedidos();
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                })
                .show();
    }

    private void mostrarDialogoModificar(Pedido pedido) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialogo_modificar_pedido, null);
        EditText edtDescripcion = dialogView.findViewById(R.id.edtDescripcion);
        EditText edtFecha = dialogView.findViewById(R.id.edtFecha);

        edtDescripcion.setText(pedido.getDescripcion());
        edtFecha.setText(pedido.getFecha());

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Modificar Pedido")
                .setView(dialogView)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String nuevaDescripcion = edtDescripcion.getText().toString().trim();
                String nuevaFecha = edtFecha.getText().toString().trim();

                if (nuevaDescripcion.isEmpty() || nuevaFecha.isEmpty()) {
                    Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!nuevaFecha.matches("\\d{2}-\\d{2}-\\d{4}")) {
                    Toast.makeText(this, "La fecha debe ser en formato DD-MM-YYYY", Toast.LENGTH_SHORT).show();
                    return;
                }

                dbHelper.modificarPedido(pedido.getId(), nuevaDescripcion, nuevaFecha);
                Toast.makeText(this, "Pedido modificado", Toast.LENGTH_SHORT).show();
                mostrarPedidos();
                dialog.dismiss();
            });
        });

        edtDescripcion.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void mostrarDialogoRangoFechas() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog startDatePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    fechaInicioSeleccionada = String.format(Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth, month + 1, year);

                    DatePickerDialog endDatePicker = new DatePickerDialog(this,
                            (view2, year2, month2, dayOfMonth2) -> {
                                fechaFinSeleccionada = String.format(Locale.getDefault(), "%02d-%02d-%04d", dayOfMonth2, month2 + 1, year2);
                                filtrarPedidosPorRango(fechaInicioSeleccionada, fechaFinSeleccionada);
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH));
                    endDatePicker.setTitle("Selecciona fecha final");
                    endDatePicker.show();

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        startDatePicker.setTitle("Selecciona fecha inicial");
        startDatePicker.show();
    }

    private void filtrarPedidosPorRango(String fechaInicio, String fechaFin) {
        List<Pedido> pedidosFiltrados = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            Date fechaInicioDate = sdf.parse(fechaInicio);
            Date fechaFinDate = sdf.parse(fechaFin);

            for (Pedido pedido : listaPedidosObj) {
                Date fechaPedidoDate = sdf.parse(pedido.getFecha());
                if (fechaPedidoDate != null &&
                        !fechaPedidoDate.before(fechaInicioDate) &&
                        !fechaPedidoDate.after(fechaFinDate)) {
                    pedidosFiltrados.add(pedido);
                }
            }

        } catch (ParseException e) {
            Toast.makeText(this, "Error al analizar las fechas. Asegúrate de que el formato sea dd-MM-yyyy.", Toast.LENGTH_LONG).show();
        }

        adaptador.clear();
        adaptador.addAll(pedidosFiltrados);
        adaptador.notifyDataSetChanged();
    }

    private void enviarResumenPorEmail() {
        Cliente cliente = dbHelper.obtenerClientePorId(clienteId);

        if (cliente == null || cliente.email == null || cliente.email.isEmpty()) {
            Toast.makeText(this, "El cliente no tiene email disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder resumen = new StringBuilder();
        resumen.append("Resumen de pedidos para ").append(cliente.nombre).append(":\n\n");

        for (Pedido pedido : listaPedidosObj) {
            resumen.append("- ").append(pedido.getDescripcion())
                    .append(" (").append(pedido.getFecha()).append(")\n");
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{cliente.email});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Resumen de tus pedidos");
        intent.putExtra(Intent.EXTRA_TEXT, resumen.toString());

        try {
            startActivity(Intent.createChooser(intent, "Enviar email con:"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No se encontró una app de correo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }

        if (item.getItemId() == R.id.action_configuracion) {
            Intent intent = new Intent(this, ConfiguracionActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
