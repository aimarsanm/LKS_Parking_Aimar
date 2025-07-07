package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.repository.ReservaRepository;

import java.util.List;

public class ReservaViewModel extends ViewModel {

    private ReservaRepository repository;
    // Constructor por defecto
    public ReservaViewModel() {
        this.repository = new ReservaRepository();
    }
    // Constructor para tests
    public ReservaViewModel(ReservaRepository repository) {
        this.repository = repository;
    }
    // Permitir inyección para tests legacy
    void setRepository(ReservaRepository mockRepo) {
        this.repository = mockRepo;
    }

    private final MutableLiveData<Boolean> reservaExitosa = new MutableLiveData<>();
    private final MutableLiveData<List<Reserva>> reservasUsuario = new MutableLiveData<>();

    public LiveData<Boolean> getReservaExitosa() {
        return reservaExitosa;
    }

    public LiveData<List<Reserva>> getReservasUsuario() {
        return reservasUsuario;
    }

    public void hacerReserva(Reserva reserva) {
        boolean exito = repository.guardarReserva(reserva);
        reservaExitosa.setValue(exito);
    }

    // Cambia este método para usar el UID del usuario autenticado
    public void cargarReservasUsuario() {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // No hay usuario autenticado, no intentes cargar reservas
            reservasUsuario.setValue(null);
            return;
        }
        String uid = user.getUid();
        repository.getReservasPorUsuarioFirestore(uid, new ReservaRepository.ReservaListCallback() {
            @Override
            public void onReservasLoaded(List<Reserva> reservas) {
                reservasUsuario.setValue(reservas);
            }
            @Override
            public void onError(Exception e) {
                reservasUsuario.setValue(null);
            }
        });
    }

    // --- NUEVO: obtener plazas libres desde Firestore ---
    public interface PlazasLibresCallback {
        void onResult(int libres);
    }

    // Corrijo la llamada para que coincida con la firma del repositorio y elimino el parámetro userEmail innecesario
    public void getPlazasLibresFirestore(String tipo, String fecha, int minutosActuales, PlazasLibresCallback callback) {
        repository.getPlazasLibresFirestore(tipo, fecha, minutosActuales, callback);
    }

    // --- NUEVO: hacer reserva en Firestore ---
    public void hacerReserva(Reserva reserva, ReservaRepository.FirestoreCallback callback) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        reserva.setUsuario(uid); // Guarda el UID como usuario
        repository.guardarReservaFirestore(reserva, new ReservaRepository.FirestoreCallback() {
            @Override
            public void onSuccess() {
                cargarReservasUsuario();
                reservaExitosa.setValue(true);
                callback.onSuccess();
            }
            @Override
            public void onFailure(Exception e) {
                reservaExitosa.setValue(false);
                callback.onFailure(e);
            }
        });
    }

    // Corrijo la ambigüedad eliminando la sobrecarga local de cancelarReserva
    // Elimina o comenta el método local que llama a repository.cancelarReserva
    // public void cancelarReserva(Reserva reserva) {
    //     repository.cancelarReserva(reserva);
    //     cargarReservasUsuario(reserva.getUsuario());
    // }

    // --- NUEVO: editar reserva en Firestore ---
    public void editarReserva(Reserva original, Reserva nueva, ReservaRepository.FirestoreCallback callback) {
        repository.editarReservaFirestore(original, nueva, new ReservaRepository.FirestoreCallback() {
            @Override
            public void onSuccess() {
                cargarReservasUsuario(); // Cambiado: sin argumentos
                callback.onSuccess();
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // --- NUEVO: cancelar reserva en Firestore ---
    public void cancelarReserva(Reserva reserva, ReservaRepository.FirestoreCallback callback) {
        repository.cancelarReservaFirestore(reserva, new ReservaRepository.FirestoreCallback() {
            @Override
            public void onSuccess() {
                cargarReservasUsuario(); // Cambiado: sin argumentos
                callback.onSuccess();
            }
            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    // --- NUEVO: métodos para editar y cancelar reservas por ID ---
    public void editarReservaFirestore(String reservaId, Reserva nueva, ReservaRepository.FirestoreCallback callback) {
        repository.editarReservaFirestore(reservaId, nueva, callback);
    }

    public void cancelarReservaFirestore(String reservaId, ReservaRepository.FirestoreCallback callback) {
        repository.cancelarReservaFirestore(reservaId, callback);
    }

    // Filtrar reservas actuales e histórico del último mes
    public void cargarReservasUsuarioUltimoMes() {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            reservasUsuario.setValue(null);
            return;
        }
        String uid = user.getUid();
        repository.getReservasPorUsuarioFirestore(uid, new ReservaRepository.ReservaListCallback() {
            @Override
            public void onReservasLoaded(List<Reserva> reservas) {
                java.time.LocalDate hoy = java.time.LocalDate.now();
                java.time.LocalDate haceUnMes = hoy.minusMonths(1);
                List<Reserva> filtradas = new java.util.ArrayList<>();
                for (Reserva r : reservas) {
                    java.time.LocalDate fechaReserva = java.time.LocalDate.parse(r.getFecha());
                    if (!fechaReserva.isBefore(haceUnMes) && !fechaReserva.isAfter(hoy.plusDays(7))) {
                        filtradas.add(r);
                    }
                }
                reservasUsuario.setValue(filtradas);
            }
            @Override
            public void onError(Exception e) {
                reservasUsuario.setValue(null);
            }
        });
    }

    // Llamar a esto tras crear una reserva para programar notificaciones
    public void programarNotificacionesReserva(android.content.Context context, Reserva reserva) {
        com.lksnext.parkingplantilla.util.ReservaNotificationUtil.scheduleNotifications(context, reserva);
    }
}
