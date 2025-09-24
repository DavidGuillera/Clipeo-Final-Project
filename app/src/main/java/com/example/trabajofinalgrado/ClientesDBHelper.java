package com.example.trabajofinalgrado;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClientesDBHelper extends SQLiteOpenHelper {

    public ClientesDBHelper(Context context) {
        super(context, "clientes.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS clientes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT," +
                "email TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS pedidos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "descripcion TEXT," +
                "fecha TEXT," +
                "cliente_id INTEGER," +
                "FOREIGN KEY(cliente_id) REFERENCES clientes(id))");

        db.execSQL("CREATE TABLE IF NOT EXISTS log_acciones (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "tipo TEXT, " +
                "accion TEXT, " +
                "descripcion TEXT, " +
                "fecha TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE, " +
                "password TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS clientes");
        onCreate(db);
    }

    public void insertarCliente(String nombre, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nombre);
        values.put("email", email);
        db.insert("clientes", null, values);
        registrarAccion("Cliente", "Creación", nombre);
    }

    public List<Cliente> obtenerClientes() {
        List<Cliente> clientes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, nombre, email FROM clientes", null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String nombre = cursor.getString(1);
                String email = cursor.getString(2);
                clientes.add(new Cliente(id, nombre, email));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return clientes;
    }

    public void eliminarCliente(int id) {
        String nombre = obtenerNombreClientePorId(id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("clientes", "id = ?", new String[]{String.valueOf(id)});
        registrarAccion("Cliente", "Eliminación", nombre);
    }

    public void modificarCliente(int id, String nuevoNombre, String nuevoEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombre", nuevoNombre);
        values.put("email", nuevoEmail);
        db.update("clientes", values, "id = ?", new String[]{String.valueOf(id)});
        registrarAccion("Cliente", "Modificación", nuevoNombre);
    }

    public void insertarPedido(String descripcion, String fecha, int clienteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("descripcion", descripcion);
        values.put("fecha", fecha);
        values.put("cliente_id", clienteId);
        db.insert("pedidos", null, values);
        registrarAccion("Pedido", "Creación", descripcion);
    }

    public List<Pedido> obtenerPedidosPorCliente(int clienteId) {
        List<Pedido> pedidos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, descripcion, fecha FROM pedidos WHERE cliente_id = ?", new String[]{String.valueOf(clienteId)});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String descripcion = cursor.getString(1);
                String fecha = cursor.getString(2);
                pedidos.add(new Pedido(id, descripcion, fecha, clienteId));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pedidos;
    }

    public Cliente obtenerClientePorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM clientes WHERE id = ?", new String[]{String.valueOf(id)});
        if (cursor.moveToFirst()) {
            String nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            cursor.close();
            return new Cliente(id, nombre, email);
        }
        cursor.close();
        return null;
    }

    public void modificarPedido(int id, String nuevaDescripcion, String nuevaFecha) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("descripcion", nuevaDescripcion);
        values.put("fecha", nuevaFecha);
        db.update("pedidos", values, "id = ?", new String[]{String.valueOf(id)});
        registrarAccion("Pedido", "Modificación", nuevaDescripcion);
    }
    public void eliminarPedido(int id) {
        String descripcion = obtenerDescripcionPedidoPorId(id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("pedidos", "id = ?", new String[]{String.valueOf(id)});
        registrarAccion("Pedido", "Eliminación", descripcion);
    }

    public boolean clienteTienePedidos(int clienteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM pedidos WHERE cliente_id = ?", new String[]{String.valueOf(clienteId)});
        boolean tienePedidos = false;
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            tienePedidos = count > 0;
        }
        cursor.close();
        return tienePedidos;
    }

    public void registrarAccion(String tipo, String accion, String descripcion) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tipo", tipo);
        values.put("accion", accion);
        values.put("descripcion", descripcion);
        values.put("fecha", new SimpleDateFormat("dd-MM-YYYY HH:mm", Locale.getDefault()).format(new Date()));
        db.insert("log_acciones", null, values);
    }

    public String obtenerNombreClientePorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT nombre FROM clientes WHERE id = ?", new String[]{String.valueOf(id)});
        String nombre = "(desconocido)";
        if (cursor.moveToFirst()) {
            nombre = cursor.getString(0);
        }
        cursor.close();
        return nombre;
    }
    public String obtenerDescripcionPedidoPorId(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT descripcion FROM pedidos WHERE id = ?", new String[]{String.valueOf(id)});
        String descripcion = "(desconocido)";
        if (cursor.moveToFirst()) {
            descripcion = cursor.getString(0);
        }
        cursor.close();
        return descripcion;
    }

    public List<String> obtenerHistorialAcciones() {
        List<String> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT tipo, accion, descripcion, fecha FROM log_acciones ORDER BY id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                String tipo = cursor.getString(0);
                String accion = cursor.getString(1);
                String descripcion = cursor.getString(2);
                String fecha = cursor.getString(3);
                lista.add("[" + fecha + "] " + tipo + " - " + accion + ": " + descripcion);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    public boolean validarUsuario(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM usuarios WHERE email=? AND password=?", new String[]{email, password});
        boolean existe = cursor.getCount() > 0;
        cursor.close();
        return existe;
    }

    public String obtenerContrasena(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT password FROM usuarios WHERE email=?", new String[]{email});
        if (cursor.moveToFirst()) {
            String pass = cursor.getString(0);
            cursor.close();
            return pass;
        }
        cursor.close();
        return null;
    }

    public boolean actualizarContrasena(String email, String nuevaContrasena) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", nuevaContrasena);
        int rows = db.update("usuarios", values, "email=?", new String[]{email});
        return rows > 0;
    }

    public boolean insertarUsuario(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("password", password);
        long result = -1;
        try {
            result = db.insertOrThrow("usuarios", null, values);
        } catch (SQLiteConstraintException e) {
            return false;
        }
        return result != -1;
    }
}