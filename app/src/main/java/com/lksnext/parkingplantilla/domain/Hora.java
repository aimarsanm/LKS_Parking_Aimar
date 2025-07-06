package com.lksnext.parkingplantilla.domain;
//aa
public class Hora {

    private int horaInicio;
    private int minutoInicio;
    private int horaFin;
    private int minutoFin;

    // Para compatibilidad con Firestore: campo 'minuto'
    private int minuto;
    public int getMinutoFirestore() { return minuto; }
    public void setMinuto(int minuto) { this.minuto = minuto; }

    public Hora() {}

    public Hora(int horaInicio, int minutoInicio) {
        this.horaInicio = horaInicio;
        this.minutoInicio = minutoInicio;
    }

    public Hora(int horaInicio,int minutoInicio ,int horaFin, int minutoFin) {
        this.horaInicio = horaInicio;
        this.minutoInicio = minutoInicio;
        this.horaFin = horaFin;
        this.minutoFin = minutoFin;
    }

    public int getHoraInicio() {
        return horaInicio;
    }

    public int getMinuto() {
        return minutoInicio;
    }

    public int getHoraFin() {
        return horaFin;
    }

    public int getMinutoFin() {
        return minutoFin;
    }

    public int toMinutosInicio() {
        return horaInicio * 60 + minutoInicio;
    }

    public int toMinutosFin() {
        return horaFin * 60 + minutoFin;
    }

    public String toHoraMinutos() {
        return String.format("%02d:%02d", horaInicio, minutoInicio);
    }

    public String toHoraMinutosFin() {
        return String.format("%02d:%02d", horaFin, minutoFin);
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d - %02d:%02d", horaInicio, minutoInicio, horaFin, minutoFin);
    }
}
