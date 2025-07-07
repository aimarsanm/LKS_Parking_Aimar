package com.lksnext.parkingplantilla.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.repository.ReservaRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@RunWith(MockitoJUnitRunner.class)
public class ReservaViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private Observer<Boolean> reservaExitosaObserver;
    @Mock
    private Observer<List<Reserva>> reservasUsuarioObserver;
    @Mock
    private ReservaRepository.FirestoreCallback firestoreCallback;
    private MockedStatic<com.google.firebase.auth.FirebaseAuth> firebaseAuthStaticMock;
    private MockedStatic<com.lksnext.parkingplantilla.repository.ReservaRepository> reservaRepositoryStaticMock;
    private MockedStatic<com.lksnext.parkingplantilla.data.DataRepository> dataRepositoryStaticMock;
    private com.lksnext.parkingplantilla.data.DataRepository dataRepositoryMock;

    @Before
    public void setUp() throws Exception {
        firebaseAuthStaticMock = Mockito.mockStatic(com.google.firebase.auth.FirebaseAuth.class);
        com.google.firebase.auth.FirebaseAuth mockAuth = mock(com.google.firebase.auth.FirebaseAuth.class);
        firebaseAuthStaticMock.when(com.google.firebase.auth.FirebaseAuth::getInstance).thenReturn(mockAuth);
        com.google.firebase.auth.FirebaseUser mockUser = mock(com.google.firebase.auth.FirebaseUser.class);
        when(mockUser.getUid()).thenReturn("user123");
        when(mockAuth.getCurrentUser()).thenReturn(mockUser); // Usuario autenticado por defecto
        // Mock estático de DataRepository.getInstance()
        dataRepositoryStaticMock = Mockito.mockStatic(com.lksnext.parkingplantilla.data.DataRepository.class);
        dataRepositoryMock = mock(com.lksnext.parkingplantilla.data.DataRepository.class);
        dataRepositoryStaticMock.when(com.lksnext.parkingplantilla.data.DataRepository::getInstance).thenReturn(dataRepositoryMock);
        when(dataRepositoryMock.getCurrentUser()).thenReturn(mockUser);
        // No stubbings globales del repositorio aquí
    }

    @org.junit.After
    public void tearDown() {
        if (firebaseAuthStaticMock != null) firebaseAuthStaticMock.close();
        if (dataRepositoryStaticMock != null) dataRepositoryStaticMock.close();
    }

    @Test
    public void testHacerReserva_setsReservaExitosaTrueOnSuccess() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        Reserva reserva = mock(Reserva.class);
        when(localRepo.guardarReserva(reserva)).thenReturn(true);
        localViewModel.getReservaExitosa().observeForever(reservaExitosaObserver);
        localViewModel.hacerReserva(reserva);
        verify(reservaExitosaObserver).onChanged(true);
    }

    @Test
    public void testHacerReserva_setsReservaExitosaFalseOnFailure() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        Reserva reserva = mock(Reserva.class);
        when(localRepo.guardarReserva(reserva)).thenReturn(false);
        localViewModel.getReservaExitosa().observeForever(reservaExitosaObserver);
        localViewModel.hacerReserva(reserva);
        verify(reservaExitosaObserver).onChanged(false);
    }

    // Utilidad para obtener el valor de LiveData de forma síncrona en tests
    private <T> T getOrAwaitValue(final LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T o) {
                data[0] = o;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        liveData.observeForever(observer);
        if (!latch.await(2, java.util.concurrent.TimeUnit.SECONDS)) {
            throw new RuntimeException("LiveData value was never set.");
        }
        //noinspection unchecked
        return (T) data[0];
    }

    @Test
    public void testGetReservasUsuario_setsValueOnSuccess() throws Exception {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        String uid = "user123";
        List<Reserva> reservas = new java.util.ArrayList<>();
        reservas.add(mock(Reserva.class));
        doAnswer(invocation -> {
            ReservaRepository.ReservaListCallback cb = invocation.getArgument(1);
            cb.onReservasLoaded(reservas);
            return null;
        }).when(localRepo).getReservasPorUsuarioFirestore(eq(uid), any());
        localViewModel.cargarReservasUsuario();
        List<Reserva> result = getOrAwaitValue(localViewModel.getReservasUsuario());
        assertEquals(reservas, result);
    }

    @Test
    public void testCargarReservasUsuario_ramas() throws Exception {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        // Usuario autenticado
        String uid = "user123";
        List<Reserva> reservas = new java.util.ArrayList<>();
        Reserva reservaMock = mock(Reserva.class);
        reservas.add(reservaMock);
        doAnswer(invocation -> {
            ReservaRepository.ReservaListCallback cb = invocation.getArgument(1);
            cb.onReservasLoaded(reservas);
            return null;
        }).when(localRepo).getReservasPorUsuarioFirestore(eq(uid), any());
        localViewModel.cargarReservasUsuario();
        List<Reserva> result = getOrAwaitValue(localViewModel.getReservasUsuario());
        assertEquals(reservas.size(), result.size());
        assertTrue(result.containsAll(reservas));
        // Usuario no autenticado
        com.google.firebase.auth.FirebaseAuth mockAuth = mock(com.google.firebase.auth.FirebaseAuth.class);
        firebaseAuthStaticMock.when(com.google.firebase.auth.FirebaseAuth::getInstance).thenReturn(mockAuth);
        when(mockAuth.getCurrentUser()).thenReturn(null);
        localViewModel.cargarReservasUsuario();
        List<Reserva> resultNull = getOrAwaitValue(localViewModel.getReservasUsuario());
        assertNull(resultNull);
    }

    @Test
    public void testCargarReservasUsuarioUltimoMes_ramas() throws Exception {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        String uid = "user123";
        List<Reserva> reservas = new ArrayList<>();
        Reserva r1 = mock(Reserva.class);
        Reserva r2 = mock(Reserva.class);
        when(r1.getFecha()).thenReturn(java.time.LocalDate.now().toString());
        when(r2.getFecha()).thenReturn(java.time.LocalDate.now().minusMonths(2).toString());
        when(r1.getId()).thenReturn("r1");
        when(r2.getId()).thenReturn("r2");
        reservas.add(r1);
        reservas.add(r2);
        doAnswer(invocation -> {
            ReservaRepository.ReservaListCallback cb = invocation.getArgument(1);
            cb.onReservasLoaded(reservas);
            return null;
        }).when(localRepo).getReservasPorUsuarioFirestore(eq(uid), any());
        localViewModel.cargarReservasUsuarioUltimoMes();
        List<Reserva> result = getOrAwaitValue(localViewModel.getReservasUsuario());
        assertEquals(1, result.size());
        assertNotNull(result.get(0).getFecha());
    }

    @Test
    public void testGetPlazasLibresFirestore() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        doAnswer(invocation -> {
            ReservaViewModel.PlazasLibresCallback cb = invocation.getArgument(3);
            cb.onResult(5);
            return null;
        }).when(localRepo).getPlazasLibresFirestore(anyString(), anyString(), anyInt(), any());
        localViewModel.getPlazasLibresFirestore("tipo", "fecha", 10, mock(ReservaViewModel.PlazasLibresCallback.class));
        verify(localRepo).getPlazasLibresFirestore(eq("tipo"), eq("fecha"), eq(10), any());
    }

    @Test
    public void testHacerReservaFirestore_success() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        Reserva reserva = mock(Reserva.class);
        String uid = "user123";
        com.google.firebase.auth.FirebaseUser user = mock(com.google.firebase.auth.FirebaseUser.class);
        when(user.getUid()).thenReturn(uid);
        mockStaticFirebaseAuth(user);
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(1);
            cb.onSuccess();
            return null;
        }).when(localRepo).guardarReservaFirestore(any(Reserva.class), any());
        localViewModel.getReservaExitosa().observeForever(reservaExitosaObserver);
        localViewModel.hacerReserva(reserva, firestoreCallback);
        verify(reservaExitosaObserver).onChanged(true);
        verify(firestoreCallback).onSuccess();
    }

    @Test
    public void testHacerReservaFirestore_failure() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
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
        }).when(localRepo).guardarReservaFirestore(any(Reserva.class), any());
        localViewModel.getReservaExitosa().observeForever(reservaExitosaObserver);
        localViewModel.hacerReserva(reserva, firestoreCallback);
        verify(reservaExitosaObserver).onChanged(false);
        verify(firestoreCallback).onFailure(ex);
    }

    @Test
    public void testEditarReserva_success() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        Reserva original = mock(Reserva.class);
        Reserva nueva = mock(Reserva.class);
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(2);
            cb.onSuccess();
            return null;
        }).when(localRepo).editarReservaFirestore(any(Reserva.class), any(Reserva.class), any());
        localViewModel.editarReserva(original, nueva, firestoreCallback);
        verify(firestoreCallback).onSuccess();
    }

    @Test
    public void testEditarReserva_failure() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        Reserva original = mock(Reserva.class);
        Reserva nueva = mock(Reserva.class);
        Exception ex = new Exception("fail");
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(2);
            cb.onFailure(ex);
            return null;
        }).when(localRepo).editarReservaFirestore(any(Reserva.class), any(Reserva.class), any());
        localViewModel.editarReserva(original, nueva, firestoreCallback);
        verify(firestoreCallback).onFailure(ex);
    }

    @Test
    public void testCancelarReserva_success() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        Reserva reserva = mock(Reserva.class);
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(1);
            cb.onSuccess();
            return null;
        }).when(localRepo).cancelarReservaFirestore(any(Reserva.class), any());
        localViewModel.cancelarReserva(reserva, firestoreCallback);
        verify(firestoreCallback).onSuccess();
    }

    @Test
    public void testCancelarReserva_failure() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        Reserva reserva = mock(Reserva.class);
        Exception ex = new Exception("fail");
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(1);
            cb.onFailure(ex);
            return null;
        }).when(localRepo).cancelarReservaFirestore(any(Reserva.class), any());
        localViewModel.cancelarReserva(reserva, firestoreCallback);
        verify(firestoreCallback).onFailure(ex);
    }

    @Test
    public void testEditarReservaFirestore() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        String reservaId = "id";
        Reserva nueva = mock(Reserva.class);
        localViewModel.editarReservaFirestore(reservaId, nueva, firestoreCallback);
        verify(localRepo).editarReservaFirestore(eq(reservaId), eq(nueva), eq(firestoreCallback));
    }

    @Test
    public void testCancelarReservaFirestore() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        String reservaId = "id";
        localViewModel.cancelarReservaFirestore(reservaId, firestoreCallback);
        verify(localRepo).cancelarReservaFirestore(eq(reservaId), eq(firestoreCallback));
    }

    // --- CAJA BLANCA: Cobertura de condiciones y ramas ---
    @Test
    public void testHacerReserva_localTodosLosCaminos() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        Reserva reserva = mock(Reserva.class);
        // Éxito
        when(localRepo.guardarReserva(reserva)).thenReturn(true);
        localViewModel.getReservaExitosa().observeForever(reservaExitosaObserver);
        localViewModel.hacerReserva(reserva);
        verify(reservaExitosaObserver).onChanged(true);
        // Falla
        when(localRepo.guardarReserva(reserva)).thenReturn(false);
        localViewModel.hacerReserva(reserva);
        verify(reservaExitosaObserver).onChanged(false);
    }

    // --- CAJA NEGRA: Partición de equivalencia y valores límite ---
    @Test
    public void testGetPlazasLibresFirestore_equivalenciaYLimites() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        ReservaViewModel.PlazasLibresCallback cb = mock(ReservaViewModel.PlazasLibresCallback.class);
        // Partición válida
        doAnswer(invocation -> {
            ReservaViewModel.PlazasLibresCallback cb1 = invocation.getArgument(3);
            cb1.onResult(10);
            return null;
        }).when(localRepo).getPlazasLibresFirestore(eq("normal"), eq("2025-07-06"), eq(60), any());
        localViewModel.getPlazasLibresFirestore("normal", "2025-07-06", 60, cb);
        verify(cb).onResult(10);
        // Valor límite inferior
        doAnswer(invocation -> {
            ReservaViewModel.PlazasLibresCallback cb2 = invocation.getArgument(3);
            cb2.onResult(0);
            return null;
        }).when(localRepo).getPlazasLibresFirestore(eq("normal"), eq("2025-07-06"), eq(0), any());
        localViewModel.getPlazasLibresFirestore("normal", "2025-07-06", 0, cb);
        verify(cb).onResult(0);
        // Valor límite superior
        doAnswer(invocation -> {
            ReservaViewModel.PlazasLibresCallback cb3 = invocation.getArgument(3);
            cb3.onResult(Integer.MAX_VALUE);
            return null;
        }).when(localRepo).getPlazasLibresFirestore(eq("normal"), eq("2025-07-06"), eq(Integer.MAX_VALUE), any());
        localViewModel.getPlazasLibresFirestore("normal", "2025-07-06", Integer.MAX_VALUE, cb);
        verify(cb).onResult(Integer.MAX_VALUE);
    }

    @Test
    public void testEditarCancelarReservaFirestore_limites() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        String minId = "";
        String maxId = String.valueOf(Character.MAX_VALUE).repeat(100);
        Reserva nueva = mock(Reserva.class);
        localViewModel.editarReservaFirestore(minId, nueva, firestoreCallback);
        localViewModel.editarReservaFirestore(maxId, nueva, firestoreCallback);
        verify(localRepo).editarReservaFirestore(eq(minId), eq(nueva), eq(firestoreCallback));
        verify(localRepo).editarReservaFirestore(eq(maxId), eq(nueva), eq(firestoreCallback));
        localViewModel.cancelarReservaFirestore(minId, firestoreCallback);
        localViewModel.cancelarReservaFirestore(maxId, firestoreCallback);
        verify(localRepo).cancelarReservaFirestore(eq(minId), eq(firestoreCallback));
        verify(localRepo).cancelarReservaFirestore(eq(maxId), eq(firestoreCallback));
    }

    @Test
    public void testEditarCancelarReserva_coberturaCondiciones() {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        Reserva original = mock(Reserva.class);
        Reserva nueva = mock(Reserva.class);
        Exception ex = new Exception("fail");
        // editarReserva éxito
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(2);
            cb.onSuccess();
            return null;
        }).when(localRepo).editarReservaFirestore(any(Reserva.class), any(Reserva.class), any());
        localViewModel.editarReserva(original, nueva, firestoreCallback);
        verify(firestoreCallback, atLeastOnce()).onSuccess();
        // editarReserva fallo
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(2);
            cb.onFailure(ex);
            return null;
        }).when(localRepo).editarReservaFirestore(any(Reserva.class), any(Reserva.class), any());
        localViewModel.editarReserva(original, nueva, firestoreCallback);
        verify(firestoreCallback, atLeastOnce()).onFailure(ex);
        // cancelarReserva éxito
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(1);
            cb.onSuccess();
            return null;
        }).when(localRepo).cancelarReservaFirestore(any(Reserva.class), any());
        localViewModel.cancelarReserva(original, firestoreCallback);
        verify(firestoreCallback, atLeastOnce()).onSuccess();
        // cancelarReserva fallo
        doAnswer(invocation -> {
            ReservaRepository.FirestoreCallback cb = invocation.getArgument(1);
            cb.onFailure(ex);
            return null;
        }).when(localRepo).cancelarReservaFirestore(any(Reserva.class), any());
        localViewModel.cancelarReserva(original, firestoreCallback);
        verify(firestoreCallback, atLeastOnce()).onFailure(ex);
    }

    // Utilidad para simular FirebaseAuth.getInstance().getCurrentUser()
    // ATENCIÓN: El acceso al campo INSTANCE de FirebaseAuth puede fallar en ejecución real, pero es útil para tests. Si falla, deberás buscar otra forma de mockear FirebaseAuth.
    private void mockStaticFirebaseAuth(com.google.firebase.auth.FirebaseUser user) {
        com.google.firebase.auth.FirebaseAuth mockAuth = mock(com.google.firebase.auth.FirebaseAuth.class);
        when(mockAuth.getCurrentUser()).thenReturn(user);
        try {
            java.lang.reflect.Field instanceField = com.google.firebase.auth.FirebaseAuth.class.getDeclaredField("INSTANCE");
            instanceField.setAccessible(true);
            instanceField.set(null, mockAuth);
        } catch (Exception ignored) {}
    }

    @Test
    public void testCargarReservasUsuario_usuarioNoAutenticado() throws Exception {
        ReservaRepository localRepo = mock(ReservaRepository.class);
        ReservaViewModel localViewModel = new ReservaViewModel(localRepo);
        // Cambia el mock para devolver null solo en este test
        com.google.firebase.auth.FirebaseAuth mockAuth = mock(com.google.firebase.auth.FirebaseAuth.class);
        firebaseAuthStaticMock.when(com.google.firebase.auth.FirebaseAuth::getInstance).thenReturn(mockAuth);
        when(mockAuth.getCurrentUser()).thenReturn(null);
        localViewModel.cargarReservasUsuario();
        List<Reserva> result = getOrAwaitValue(localViewModel.getReservasUsuario());
        assertNull(result);
    }
}