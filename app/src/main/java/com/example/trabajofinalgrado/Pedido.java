package com.example.trabajofinalgrado;

public class Pedido {
    public int id;
    public String descripcion;
    public String fecha;
    public int clienteId;

    public Pedido(int id, String descripcion, String fecha, int clienteId) {
        this.id = id;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.clienteId = clienteId;
    }

    @Override
    public String toString() {
        return fecha + ": " + descripcion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }
}