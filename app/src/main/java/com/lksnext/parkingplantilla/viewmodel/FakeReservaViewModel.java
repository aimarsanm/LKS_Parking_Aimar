package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.lksnext.parkingplantilla.domain.Reserva;
import java.util.List;
import java.util.ArrayList;

public class FakeReservaViewModel extends ViewModel {
    private final MutableLiveData<Boolean> reservaExitosa = new MutableLiveData<>();
    private final MutableLiveData<List<Reserva>> reservasUsuario = new MutableLiveData<>();
    private boolean reservaDebeSerExitosa = true;
    private List<Reserva> reservasFake = new ArrayList<>();

    public void setReservaDebeSerExitosa(boolean exito) {
        this.reservaDebeSerExitosa = exito;
    }
    public void setReservasFake(List<Reserva> reservas) {
        this.reservasFake = reservas;
    }

    public LiveData<Boolean> getReservaExitosa() {
        return reservaExitosa;
    }
    public LiveData<List<Reserva>> getReservasUsuario() {
        return reservasUsuario;
    }

    public void hacerReserva(Reserva reserva) {
        reservaExitosa.setValue(reservaDebeSerExitosa);
    }

    public void cargarReservasUsuario() {
        reservasUsuario.setValue(reservasFake);
    }
}

