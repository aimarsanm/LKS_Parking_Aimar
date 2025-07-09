package com.lksnext.parkingplantilla.repository;

import com.lksnext.parkingplantilla.domain.Reserva;
import java.util.List;

public class FakeReservaRepository {
    private boolean guardarReservaExito = true;
    private List<Reserva> reservasUsuario;
    private Exception error;

    public void setGuardarReservaExito(boolean exito) {
        this.guardarReservaExito = exito;
    }
    public void setReservasUsuario(List<Reserva> reservas) {
        this.reservasUsuario = reservas;
    }
    public void setError(Exception error) {
        this.error = error;
    }

    public boolean guardarReserva(Reserva reserva) {
        return guardarReservaExito;
    }

    public void getReservasPorUsuarioFirestore(String uid, ReservaRepository.ReservaListCallback callback) {
        if (error != null) {
            callback.onError(error);
        } else {
            callback.onReservasLoaded(reservasUsuario);
        }
    }
}

