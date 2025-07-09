package com.lksnext.parkingplantilla.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.repository.FakeReservaRepository;
import com.lksnext.parkingplantilla.util.LiveDataTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class ReservaViewModelTest {
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    private FakeReservaRepository fakeRepo;
    private FakeReservaViewModel viewModel;

    @Before
    public void setUp() {
        fakeRepo = new FakeReservaRepository();
        viewModel = new FakeReservaViewModel();
    }

    @Test
    public void hacerReserva_exito_actualizaLiveData() throws InterruptedException {
        viewModel.setReservaDebeSerExitosa(true);
        viewModel.hacerReserva(new Reserva());
        Boolean result = LiveDataTestUtil.getValue(viewModel.getReservaExitosa());
        assertTrue(result);
    }

    @Test
    public void hacerReserva_fallo_actualizaLiveData() throws InterruptedException {
        viewModel.setReservaDebeSerExitosa(false);
        viewModel.hacerReserva(new Reserva());
        Boolean result = LiveDataTestUtil.getValue(viewModel.getReservaExitosa());
        assertFalse(result);
    }

    @Test
    public void cargarReservasUsuario_exito_actualizaLiveData() throws InterruptedException {
        ArrayList<Reserva> reservas = new ArrayList<>();
        reservas.add(new Reserva());
        reservas.add(new Reserva());
        viewModel.setReservasFake(reservas);
        viewModel.cargarReservasUsuario();
        List<Reserva> result = LiveDataTestUtil.getValue(viewModel.getReservasUsuario());
        assertEquals(2, result.size());
    }

    @Test
    public void cargarReservasUsuario_fallo_actualizaLiveDataNull() throws InterruptedException {
        viewModel.setReservasFake(null);
        viewModel.cargarReservasUsuario();
        List<Reserva> result = LiveDataTestUtil.getValue(viewModel.getReservasUsuario());
        assertNull(result);
    }

    @Test
    public void reservaExitosa_inicial_esNull() throws Exception {
        assertNull(LiveDataTestUtil.getValue(viewModel.getReservaExitosa()));
    }

    @Test
    public void reservasUsuario_inicial_esNull() throws Exception {
        assertNull(LiveDataTestUtil.getValue(viewModel.getReservasUsuario()));
    }

    @Test
    public void hacerReserva_multipleCalls_changesState() throws Exception {
        viewModel.setReservaDebeSerExitosa(true);
        viewModel.hacerReserva(new Reserva());
        assertTrue(LiveDataTestUtil.getValue(viewModel.getReservaExitosa()));
        viewModel.setReservaDebeSerExitosa(false);
        viewModel.hacerReserva(new Reserva());
        assertFalse(LiveDataTestUtil.getValue(viewModel.getReservaExitosa()));
    }

    @Test
    public void cargarReservasUsuario_listaVacia() throws Exception {
        viewModel.setReservasFake(new ArrayList<>());
        viewModel.cargarReservasUsuario();
        List<Reserva> result = LiveDataTestUtil.getValue(viewModel.getReservasUsuario());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
