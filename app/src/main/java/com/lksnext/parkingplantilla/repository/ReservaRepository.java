package com.lksnext.parkingplantilla.repository;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.Hora;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.domain.Usuario;
import com.lksnext.parkingplantilla.viewmodel.ReservaViewModel;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservaRepository {

    private final List<Reserva> reservas = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference reservasRef = db.collection("reservas");

    // Cantidad fija de plazas por tipo
    private static final int TOTAL_COCHE = 50;
    private static final int TOTAL_HIBRIDO = 2;
    private static final int TOTAL_MOTO = 20;
    private static final int TOTAL_DISCAPACITADO = 15;

    public interface FirestoreCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public boolean guardarReserva(Reserva nueva) {
        return guardarReserva(nueva, null);
    }

    // Nuevo método sobrecargado para edición
    public boolean guardarReserva(Reserva nueva, Reserva ignorar) {
        // Verificar si el usuario está logueado
        if (DataRepository.getInstance().getCurrentUser() == null) {
            return false; // No permitir reservar si no está logueado
        }
        // Validar duración máxima (9 horas)
        if (!validarDuracionMaxima(nueva.getHoraInicio(), nueva.getHoraFin())) return false;
        // Validar fecha dentro de hoy + 7 días
        if (!validarFechaPermitida(nueva.getFecha())) return false;
        // Buscar plazas ocupadas para ese tipo y fecha
        List<Integer> ocupadas = new ArrayList<>();
        for (Reserva r : reservas) {
            if (ignorar != null && r.equals(ignorar)) continue;
            if (!r.getFecha().equals(nueva.getFecha())) continue;
            Plaza plazaExistente = r.getPlazaId();
            Plaza plazaNueva = nueva.getPlazaId();
            if (!plazaExistente.getTipo().equalsIgnoreCase(plazaNueva.getTipo())) continue;
            // Permitir varias reservas del mismo usuario en la misma hora si hay plazas libres
            if (horasSolapan(r.getHoraInicio(), r.getHoraFin(), nueva.getHoraInicio(), nueva.getHoraFin())) {
                ocupadas.add((int) plazaExistente.getId());
            }
        }
        int total = getTotalPlazasPorTipo(nueva.getPlazaId().getTipo());
        int plazaLibre = -1;
        for (int i = 1; i <= total; i++) {
            if (!ocupadas.contains(i)) {
                plazaLibre = i;
                break;
            }
        }
        if (plazaLibre == -1) return false; // No hay plazas libres
        nueva.getPlazaId().setId(plazaLibre);
        reservas.add(nueva);
        return true;
    }

    public void guardarReservaFirestore(Reserva nueva, FirestoreCallback callback) {
        // Comprobar usuario autenticado
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            if (callback != null) callback.onFailure(new Exception("Usuario no autenticado"));
            android.util.Log.e("ReservaRepository", "Usuario no autenticado al guardar reserva");
            return;
        }
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid)
            .collection("reservas")
            .whereEqualTo("fecha", nueva.getFecha())
            .whereEqualTo("plazaId.tipo", nueva.getPlazaId().getTipo())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Integer> ocupadas = new ArrayList<>();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Reserva r = doc.toObject(Reserva.class);
                    if (horasSolapan(r.getHoraInicio(), r.getHoraFin(), nueva.getHoraInicio(), nueva.getHoraFin())) {
                        ocupadas.add((int) r.getPlazaId().getId());
                    }
                }
                int total = getTotalPlazasPorTipo(nueva.getPlazaId().getTipo());
                int plazaLibre = -1;
                for (int i = 1; i <= total; i++) {
                    if (!ocupadas.contains(i)) {
                        plazaLibre = i;
                        break;
                    }
                }
                if (plazaLibre == -1) {
                    if (callback != null) callback.onFailure(new Exception("No hay plazas libres disponibles"));
                    android.util.Log.e("ReservaRepository", "No hay plazas libres disponibles");
                    return;
                }
                nueva.getPlazaId().setId(plazaLibre);
                db.collection("users").document(uid)
                    .collection("reservas")
                    .add(nueva)
                    .addOnSuccessListener(documentReference -> {
                        if (callback != null) callback.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) callback.onFailure(e);
                        android.util.Log.e("ReservaRepository", "Error al guardar reserva", e);
                    });
            })
            .addOnFailureListener(e -> {
                if (callback != null) callback.onFailure(e);
                android.util.Log.e("ReservaRepository", "Error al consultar reservas", e);
            });
    }

    private int getTotalPlazasPorTipo(String tipo) {
        switch (tipo.toLowerCase()) {
            case "coche": return TOTAL_COCHE;
            case "hibrido": return TOTAL_HIBRIDO;
            case "moto": return TOTAL_MOTO;
            case "discapacitado": return TOTAL_DISCAPACITADO;
            default: return 0;
        }
    }

    public List<Reserva> getReservasPorFechaYTipo(String fecha, String tipoPlaza) {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : reservas) {
            if (r.getFecha().equals(fecha) && r.getPlazaId().getTipo().equalsIgnoreCase(tipoPlaza)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    public List<Reserva> getReservasPorUsuario(String usuario) {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : reservas) {
            if (r.getUsuario().equals(usuario)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    public void getReservasPorUsuarioFirestore(String usuario, final ReservaListCallback callback) {
        // Leer reservas desde la subcolección del usuario
        db.collection("users").document(usuario)
            .collection("reservas")
            .get()
            .addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<com.google.firebase.firestore.QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        List<Reserva> resultado = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Reserva reserva = doc.toObject(Reserva.class);
                            reserva.setId(doc.getId()); // Asignar el ID del documento Firestore
                            resultado.add(reserva);
                        }
                        callback.onReservasLoaded(resultado);
                    } else {
                        callback.onError(task.getException());
                    }
                }
            });
    }

    public interface ReservaListCallback {
        void onReservasLoaded(List<Reserva> reservas);
        void onError(Exception e);
    }

    public void cancelarReserva(Reserva reserva) {
        reservas.remove(reserva);
    }

    public void cancelarReservaFirestore(Reserva reserva, FirestoreCallback callback) {
        db.collection("users").document(reserva.getUsuario())
            .collection("reservas")
            .whereEqualTo("fecha", reserva.getFecha())
            .whereEqualTo("plazaId.tipo", reserva.getPlazaId().getTipo())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    queryDocumentSnapshots.getDocuments().get(0).getReference().delete()
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(callback::onFailure);
                } else {
                    callback.onFailure(new Exception("Reserva no encontrada"));
                }
            })
            .addOnFailureListener(callback::onFailure);
    }

    public List<Reserva> getTodasReservas() {
        return new ArrayList<>(reservas);
    }

    // ====================== Helpers ========================

    // Validar duración máxima (9 horas = 540 minutos)
    private boolean validarDuracionMaxima(Hora inicio, Hora fin) {
        long diferencia = fin.toMinutosFin() - inicio.toMinutosInicio();
        return diferencia > 0 && diferencia <= 540;
    }

    private boolean horasSolapan(Hora ini1, Hora fin1, Hora ini2, Hora fin2) {
        return ini1.toMinutosInicio() < fin2.toMinutosFin() && ini2.toMinutosInicio() < fin1.toMinutosFin();
    }

    // Validar que la fecha no sea más de 7 días en el futuro
    private boolean validarFechaPermitida(String fechaStr) {
        java.time.LocalDate hoy = LocalDate.now();
        java.time.LocalDate fechaReserva = java.time.LocalDate.parse(fechaStr);
        java.time.Period periodo = java.time.Period.between(hoy, fechaReserva);
        return !fechaReserva.isBefore(hoy) && periodo.getDays() <= 7 && periodo.getMonths() == 0 && periodo.getYears() == 0;
    }

    public interface EditarCallback {
        void onResult(boolean exito);
    }

    public void editarReserva(Reserva original, Reserva nueva, EditarCallback callback) {
        // Elimina la reserva original
        reservas.remove(original);
        // Intenta guardar la nueva reserva (misma lógica que guardarReserva)
        boolean exito = guardarReserva(nueva, original);
        if (exito) {
            callback.onResult(true);
        } else {
            // Si falla, vuelve a añadir la original
            reservas.add(original);
            callback.onResult(false);
        }
    }

    // Obtener plazas libres desde Firestore
    // Consulta solo por fecha y filtra tipo en Java para evitar problemas de mayúsculas/minúsculas
    public void getPlazasLibresFirestore(String tipo, String fecha, int minutosActuales, ReservaViewModel.PlazasLibresCallback callback) {
        String tipoLower = tipo.toLowerCase();
        db.collectionGroup("reservas")
            .whereEqualTo("fecha", fecha)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                int total = getTotalPlazasPorTipo(tipoLower);
                int ocupadas = 0;
                int docs = 0;
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    docs++;
                    try {
                        Reserva r = doc.toObject(Reserva.class);
                        if (r == null || r.getHoraFin() == null || r.getPlazaId() == null || r.getPlazaId().getTipo() == null) {
                            android.util.Log.w("ReservaRepository", "Documento con campos nulos: " + doc.getId());
                            continue;
                        }
                        // Compara tipo ignorando mayúsculas/minúsculas
                        if (!r.getPlazaId().getTipo().equalsIgnoreCase(tipo)) continue;
                        if (r.getHoraFin().toMinutosFin() > minutosActuales) {
                            ocupadas++;
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ReservaRepository", "Error parseando reserva: " + doc.getId(), e);
                    }
                }
                android.util.Log.i("ReservaRepository", "Tipo: " + tipo + ", Fecha: " + fecha + ", Total docs: " + docs + ", Ocupadas: " + ocupadas + ", Total plazas: " + total);
                callback.onResult(total - ocupadas);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("ReservaRepository", "Error Firestore getPlazasLibresFirestore para tipo " + tipo + ", fecha " + fecha, e);
                callback.onResult(-1);
            });
    }

    // Editar reserva en Firestore
    public void editarReservaFirestore(Reserva original, Reserva nueva, FirestoreCallback callback) {
        // Buscar y eliminar la original, luego guardar la nueva
        db.collection("users").document(original.getUsuario())
            .collection("reservas")
            .whereEqualTo("fecha", original.getFecha())
            .whereEqualTo("plazaId.tipo", original.getPlazaId().getTipo())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    queryDocumentSnapshots.getDocuments().get(0).getReference().delete()
                        .addOnSuccessListener(aVoid -> guardarReservaFirestore(nueva, callback))
                        .addOnFailureListener(callback::onFailure);
                } else {
                    guardarReservaFirestore(nueva, callback);
                }
            })
            .addOnFailureListener(callback::onFailure);
    }

    public void editarReservaFirestore(String reservaId, Reserva nueva, ReservaRepository.FirestoreCallback callback) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid)
            .collection("reservas").document(reservaId)
            .set(nueva)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(callback::onFailure);
    }

    public void cancelarReservaFirestore(String reservaId, ReservaRepository.FirestoreCallback callback) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid)
            .collection("reservas").document(reservaId)
            .delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(callback::onFailure);
    }

    public void leerDatosUsuarioFirestore(UsuarioCallback callback) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                Usuario usuario = documentSnapshot.toObject(Usuario.class);
                callback.onUsuarioLoaded(usuario);
            })
            .addOnFailureListener(e -> callback.onUsuarioLoaded(null));
    }

    public interface UsuarioCallback {
        void onUsuarioLoaded(Usuario usuario);
    }
}
