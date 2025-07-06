package com.lksnext.parkingplantilla.viewmodel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.repository.ReservaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ReservaViewModelTest {

    @org.junit.Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private ReservaViewModel viewModel;

    @Mock
    private ReservaRepository repository;
    @Mock
    private Observer<Boolean> reservaExitosaObserver;
    @Mock
    private Observer<List<Reserva>> reservasUsuarioObserver;
    @Mock
    private ReservaRepository.FirestoreCallback firestoreCallback; // <-- Añadido

    @BeforeEach
    void setUp() throws Exception {
        viewModel = new ReservaViewModel();
        java.lang.reflect.Field field = ReservaViewModel.class.getDeclaredField("repository");
        field.setAccessible(true);
        field.set(viewModel, repository);
    }

    @Test
    void testHacerReserva_setsReservaExitosaTrueOnSuccess() {
        Reserva reserva = mock(Reserva.class);
        when(repository.guardarReserva(reserva)).thenReturn(true);
        viewModel.getReservaExitosa().observeForever(reservaExitosaObserver);
        viewModel.hacerReserva(reserva);
        verify(reservaExitosaObserver).onChanged(true);
    }

    @Test
    void testHacerReserva_setsReservaExitosaFalseOnFailure() {
        Reserva reserva = mock(Reserva.class);
        when(repository.guardarReserva(reserva)).thenReturn(false);
        viewModel.getReservaExitosa().observeForever(reservaExitosaObserver);
        viewModel.hacerReserva(reserva);
        verify(reservaExitosaObserver).onChanged(false);
    }

    @Test
    void testGetReservasUsuario_setsValueOnSuccess() {
        String uid = "user123";
        List<Reserva> reservas = Arrays.asList(mock(Reserva.class));
        ReservaRepository.ReservaListCallback[] callbackHolder = new ReservaRepository.ReservaListCallback[1];
        doAnswer(invocation -> {
            callbackHolder[0] = invocation.getArgument(1);
            return null;
        }).when(repository).getReservasPorUsuarioFirestore(eq(uid), any());
        viewModel.getReservasUsuario().observeForever(reservasUsuarioObserver);
        com.google.firebase.auth.FirebaseUser user = mock(com.google.firebase.auth.FirebaseUser.class);
        when(user.getUid()).thenReturn(uid);
        mockStaticFirebaseAuth(user);
        viewModel.cargarReservasUsuario();
        callbackHolder[0].onReservasLoaded(reservas);
        verify(reservasUsuarioObserver).onChanged(reservas);
    }

    @Test
    void testCargarReservasUsuario_usuarioNoAutenticado() {
        viewModel.getReservasUsuario().observeForever(reservasUsuarioObserver);
        mockStaticFirebaseAuth(null);
        viewModel.cargarReservasUsuario();
        verify(reservasUsuarioObserver).onChanged(null);
    }

    @Test
    void testGetPlazasLibresFirestore() {
        doAnswer(invocation -> {
            ReservaViewModel.PlazasLibresCallback cb = invocation.getArgument(3);
            cb.onResult(5);
            return null;
        }).when(repository).getPlazasLibresFirestore(anyString(), anyString(), anyInt(), any());
        viewModel.getPlazasLibresFirestore("tipo", "fecha", 10, mock(ReservaViewModel.PlazasLibresCallback.class));
        verify(repository).getPlazasLibresFirestore(eq("tipo"), eq("fecha"), eq(10), any());
    }

    @Test
    void testHacerReservaFirestore_success() {
        Reserva reserva = mock(Reserva.class);
        String uid = "user123";
        com.google.firebase.auth.FirebaseUser user = mock(com.google.firebase.auth.FirebaseUser.class);
        when(user.getUid()).thenReturn(uid);
        mockStaticFirebaseAuth(user);
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(1);
            cb.onSuccess();
            return null;
        }).when(repository).guardarReservaFirestore(any(Reserva.class), any());
        viewModel.getReservaExitosa().observeForever(reservaExitosaObserver);
        viewModel.hacerReserva(reserva, firestoreCallback);
        verify(reservaExitosaObserver).onChanged(true);
        verify(firestoreCallback).onSuccess();
    }

    @Test
    void testHacerReservaFirestore_failure() {
        Reserva reserva = mock(Reserva.class);
        String uid = "user123";
        com.google.firebase.auth.FirebaseUser user = mock(com.google.firebase.auth.FirebaseUser.class);
        when(user.getUid()).thenReturn(uid);
        mockStaticFirebaseAuth(user);
        Exception ex = new Exception("fail");
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(1);
            cb.onFailure(ex);
            return null;
        }).when(repository).guardarReservaFirestore(any(Reserva.class), any());
        viewModel.getReservaExitosa().observeForever(reservaExitosaObserver);
        viewModel.hacerReserva(reserva, firestoreCallback);
        verify(reservaExitosaObserver).onChanged(false);
        verify(firestoreCallback).onFailure(ex);
    }

    @Test
    void testEditarReserva_success() {
        Reserva original = mock(Reserva.class);
        Reserva nueva = mock(Reserva.class);
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(2);
            cb.onSuccess();
            return null;
        }).when(repository).editarReservaFirestore(any(Reserva.class), any(Reserva.class), any());
        viewModel.editarReserva(original, nueva, firestoreCallback);
        verify(firestoreCallback).onSuccess();
    }

    @Test
    void testEditarReserva_failure() {
        Reserva original = mock(Reserva.class);
        Reserva nueva = mock(Reserva.class);
        Exception ex = new Exception("fail");
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(2);
            cb.onFailure(ex);
            return null;
        }).when(repository).editarReservaFirestore(any(Reserva.class), any(Reserva.class), any());
        viewModel.editarReserva(original, nueva, firestoreCallback);
        verify(firestoreCallback).onFailure(ex);
    }

    @Test
    void testCancelarReserva_success() {
        Reserva reserva = mock(Reserva.class);
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(1);
            cb.onSuccess();
            return null;
        }).when(repository).cancelarReservaFirestore(any(Reserva.class), any());
        viewModel.cancelarReserva(reserva, firestoreCallback);
        verify(firestoreCallback).onSuccess();
    }

    @Test
    void testCancelarReserva_failure() {
        Reserva reserva = mock(Reserva.class);
        Exception ex = new Exception("fail");
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(1);
            cb.onFailure(ex);
            return null;
        }).when(repository).cancelarReservaFirestore(any(Reserva.class), any());
        viewModel.cancelarReserva(reserva, firestoreCallback);
        verify(firestoreCallback).onFailure(ex);
    }

    @Test
    void testEditarReservaFirestore() {
        String reservaId = "id";
        Reserva nueva = mock(Reserva.class);
        viewModel.editarReservaFirestore(reservaId, nueva, firestoreCallback);
        verify(repository).editarReservaFirestore(eq(reservaId), eq(nueva), eq(firestoreCallback));
    }

    @Test
    void testCancelarReservaFirestore() {
        String reservaId = "id";
        viewModel.cancelarReservaFirestore(reservaId, firestoreCallback);
        verify(repository).cancelarReservaFirestore(eq(reservaId), eq(firestoreCallback));
    }

    // --- CAJA BLANCA: Cobertura de condiciones y ramas ---
    @Test
    void testHacerReserva_localTodosLosCaminos() {
        Reserva reserva = mock(Reserva.class);
        // Éxito
        when(repository.guardarReserva(reserva)).thenReturn(true);
        viewModel.getReservaExitosa().observeForever(reservaExitosaObserver);
        viewModel.hacerReserva(reserva);
        verify(reservaExitosaObserver).onChanged(true);
        // Falla
        when(repository.guardarReserva(reserva)).thenReturn(false);
        viewModel.hacerReserva(reserva);
        verify(reservaExitosaObserver).onChanged(false);
    }

    @Test
    void testCargarReservasUsuario_ramas() {
        // Usuario autenticado
        String uid = "user123";
        List<Reserva> reservas = Arrays.asList(mock(Reserva.class));
        ReservaRepository.ReservaListCallback[] callbackHolder = new ReservaRepository.ReservaListCallback[1];
        doAnswer(invocation -> {
            callbackHolder[0] = invocation.getArgument(1);
            return null;
        }).when(repository).getReservasPorUsuarioFirestore(eq(uid), any());
        viewModel.getReservasUsuario().observeForever(reservasUsuarioObserver);
        com.google.firebase.auth.FirebaseUser user = mock(com.google.firebase.auth.FirebaseUser.class);
        when(user.getUid()).thenReturn(uid);
        mockStaticFirebaseAuth(user);
        viewModel.cargarReservasUsuario();
        callbackHolder[0].onReservasLoaded(reservas);
        verify(reservasUsuarioObserver, atLeastOnce()).onChanged(reservas);
        // Usuario no autenticado
        mockStaticFirebaseAuth(null);
        viewModel.cargarReservasUsuario();
        verify(reservasUsuarioObserver, atLeastOnce()).onChanged(null);
    }

    @Test
    void testCargarReservasUsuarioUltimoMes_ramas() {
        String uid = "user123";
        List<Reserva> reservas = new ArrayList<>();
        Reserva r1 = mock(Reserva.class);
        Reserva r2 = mock(Reserva.class);
        when(r1.getFecha()).thenReturn(java.time.LocalDate.now().toString());
        when(r2.getFecha()).thenReturn(java.time.LocalDate.now().minusMonths(2).toString());
        reservas.add(r1);
        reservas.add(r2);
        ReservaRepository.ReservaListCallback[] callbackHolder = new ReservaRepository.ReservaListCallback[1];
        doAnswer(invocation -> {
            callbackHolder[0] = invocation.getArgument(1);
            return null;
        }).when(repository).getReservasPorUsuarioFirestore(eq(uid), any());
        viewModel.getReservasUsuario().observeForever(reservasUsuarioObserver);
        com.google.firebase.auth.FirebaseUser user = mock(com.google.firebase.auth.FirebaseUser.class);
        when(user.getUid()).thenReturn(uid);
        mockStaticFirebaseAuth(user);
        viewModel.cargarReservasUsuarioUltimoMes();
        callbackHolder[0].onReservasLoaded(reservas);
        verify(reservasUsuarioObserver).onChanged(argThat(list -> list.size() == 1));
    }

    // --- CAJA NEGRA: Partición de equivalencia y valores límite ---
    @Test
    void testGetPlazasLibresFirestore_equivalenciaYLimites() {
        ReservaViewModel.PlazasLibresCallback cb = mock(ReservaViewModel.PlazasLibresCallback.class);
        // Partición válida
        doAnswer(invocation -> {
            ReservaViewModel.PlazasLibresCallback cb1 = invocation.getArgument(3);
            cb1.onResult(10);
            return null;
        }).when(repository).getPlazasLibresFirestore(eq("normal"), eq("2025-07-06"), eq(60), any());
        viewModel.getPlazasLibresFirestore("normal", "2025-07-06", 60, cb);
        verify(cb).onResult(10);
        // Valor límite inferior
        doAnswer(invocation -> {
            ReservaViewModel.PlazasLibresCallback cb2 = invocation.getArgument(3);
            cb2.onResult(0);
            return null;
        }).when(repository).getPlazasLibresFirestore(eq("normal"), eq("2025-07-06"), eq(0), any());
        viewModel.getPlazasLibresFirestore("normal", "2025-07-06", 0, cb);
        verify(cb).onResult(0);
        // Valor límite superior
        doAnswer(invocation -> {
            ReservaViewModel.PlazasLibresCallback cb3 = invocation.getArgument(3);
            cb3.onResult(Integer.MAX_VALUE);
            return null;
        }).when(repository).getPlazasLibresFirestore(eq("normal"), eq("2025-07-06"), eq(Integer.MAX_VALUE), any());
        viewModel.getPlazasLibresFirestore("normal", "2025-07-06", Integer.MAX_VALUE, cb);
        verify(cb).onResult(Integer.MAX_VALUE);
    }

    @Test
    void testEditarCancelarReservaFirestore_limites() {
        String minId = "";
        String maxId = String.valueOf(Character.MAX_VALUE).repeat(100);
        Reserva nueva = mock(Reserva.class);
        viewModel.editarReservaFirestore(minId, nueva, firestoreCallback);
        viewModel.editarReservaFirestore(maxId, nueva, firestoreCallback);
        verify(repository).editarReservaFirestore(eq(minId), eq(nueva), eq(firestoreCallback));
        verify(repository).editarReservaFirestore(eq(maxId), eq(nueva), eq(firestoreCallback));
        viewModel.cancelarReservaFirestore(minId, firestoreCallback);
        viewModel.cancelarReservaFirestore(maxId, firestoreCallback);
        verify(repository).cancelarReservaFirestore(eq(minId), eq(firestoreCallback));
        verify(repository).cancelarReservaFirestore(eq(maxId), eq(firestoreCallback));
    }

    @Test
    void testEditarCancelarReserva_coberturaCondiciones() {
        Reserva original = mock(Reserva.class);
        Reserva nueva = mock(Reserva.class);
        Exception ex = new Exception("fail");
        // editarReserva éxito
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(2);
            cb.onSuccess();
            return null;
        }).when(repository).editarReservaFirestore(any(Reserva.class), any(Reserva.class), any());
        viewModel.editarReserva(original, nueva, firestoreCallback);
        verify(firestoreCallback, atLeastOnce()).onSuccess();
        // editarReserva fallo
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(2);
            cb.onFailure(ex);
            return null;
        }).when(repository).editarReservaFirestore(any(Reserva.class), any(Reserva.class), any());
        viewModel.editarReserva(original, nueva, firestoreCallback);
        verify(firestoreCallback, atLeastOnce()).onFailure(ex);
        // cancelarReserva éxito
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(1);
            cb.onSuccess();
            return null;
        }).when(repository).cancelarReservaFirestore(any(Reserva.class), any());
        viewModel.cancelarReserva(original, firestoreCallback);
        verify(firestoreCallback, atLeastOnce()).onSuccess();
        // cancelarReserva fallo
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(1);
            cb.onFailure(ex);
            return null;
        }).when(repository).cancelarReservaFirestore(any(Reserva.class), any());
        viewModel.cancelarReserva(original, firestoreCallback);
        verify(firestoreCallback, atLeastOnce()).onFailure(ex);
    }

    // Utilidad para simular FirebaseAuth.getInstance().getCurrentUser()
    private void mockStaticFirebaseAuth(com.google.firebase.auth.FirebaseUser user) {
        com.google.firebase.auth.FirebaseAuth mockAuth = mock(com.google.firebase.auth.FirebaseAuth.class);
        when(mockAuth.getCurrentUser()).thenReturn(user);
        try {
            java.lang.reflect.Field instanceField = com.google.firebase.auth.FirebaseAuth.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            instanceField.set(null, mockAuth);
        } catch (Exception ignored) {}
    }
}