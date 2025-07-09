package com.lksnext.parkingplantilla.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.lksnext.parkingplantilla.data.FakeDataRepository;
import com.lksnext.parkingplantilla.viewmodel.FakeRegisterViewModel;
import com.lksnext.parkingplantilla.util.LiveDataTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.robolectric.RobolectricTestRunner;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class RegisterViewModelTest {

    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    private FakeDataRepository fakeRepo;
    private FakeRegisterViewModel viewModel;

    @Before
    public void setUp() {
        fakeRepo = new FakeDataRepository();
        viewModel = new FakeRegisterViewModel(fakeRepo);
    }

    @Test
    public void register_success_setsRegisterSuccessTrue() throws Exception {
        fakeRepo.setRegisterShouldSucceed(true);
        viewModel.register("Nombre", "Apellido", "mail@mail.com", "1234");
        assertTrue(LiveDataTestUtil.getValue(viewModel.getRegisterSuccess()));
        assertNull(LiveDataTestUtil.getValue(viewModel.getRegisterError()));
    }

    @Test
    public void register_failure_setsRegisterSuccessFalse_andError() throws Exception {
        fakeRepo.setRegisterShouldSucceed(false);
        fakeRepo.setErrorMessage("error");
        viewModel.register("Nombre", "Apellido", "mail@mail.com", "1234");
        assertFalse(LiveDataTestUtil.getValue(viewModel.getRegisterSuccess()));
        assertEquals("error", LiveDataTestUtil.getValue(viewModel.getRegisterError()));
    }

    @Test
    public void register_setsRegisterSuccessNull_whenNoCallback() throws Exception {
        // Estado inicial nulo (no se ha llamado a register)
        assertNull(LiveDataTestUtil.getValue(viewModel.getRegisterSuccess()));
    }

    @Test
    public void register_multipleCalls_changesState() throws Exception {
        fakeRepo.setRegisterShouldSucceed(true);
        viewModel.register("Nombre", "Apellido", "mail@mail.com", "1234");
        assertTrue(LiveDataTestUtil.getValue(viewModel.getRegisterSuccess()));
        fakeRepo.setRegisterShouldSucceed(false);
        fakeRepo.setErrorMessage("fail");
        viewModel.register("Nombre", "Apellido", "mail@mail.com", "fail");
        assertFalse(LiveDataTestUtil.getValue(viewModel.getRegisterSuccess()));
        assertEquals("fail", LiveDataTestUtil.getValue(viewModel.getRegisterError()));
    }

    @Test
    public void register_whiteBox_initialState_isNull() throws Exception {
        assertNull(LiveDataTestUtil.getValue(viewModel.getRegisterSuccess()));
        assertNull(LiveDataTestUtil.getValue(viewModel.getRegisterError()));
    }

    @Test
    public void register_whiteBox_multipleTransitions() throws Exception {
        fakeRepo.setRegisterShouldSucceed(true);
        viewModel.register("A", "B", "a@mail.com", "p1");
        assertTrue(LiveDataTestUtil.getValue(viewModel.getRegisterSuccess()));
        fakeRepo.setRegisterShouldSucceed(false);
        fakeRepo.setErrorMessage("fail");
        viewModel.register("C", "D", "b@mail.com", "p2");
        assertFalse(LiveDataTestUtil.getValue(viewModel.getRegisterSuccess()));
        assertEquals("fail", LiveDataTestUtil.getValue(viewModel.getRegisterError()));
        fakeRepo.setRegisterShouldSucceed(true);
        viewModel.register("E", "F", "c@mail.com", "p3");
        assertTrue(LiveDataTestUtil.getValue(viewModel.getRegisterSuccess()));
    }

    @Test
    public void register_blackBox_equivalencePartition() throws Exception {
        // Partition: datos válidos
        fakeRepo.setRegisterShouldSucceed(true);
        viewModel.register("Nombre", "Apellido", "valido@mail.com", "valido");
        assertTrue(LiveDataTestUtil.getValue(viewModel.getRegisterSuccess()));
        // Partition: datos inválidos
        fakeRepo.setRegisterShouldSucceed(false);
        fakeRepo.setErrorMessage("invalido");
        viewModel.register("Nombre", "Apellido", "invalido@mail.com", "bad");
        assertFalse(LiveDataTestUtil.getValue(viewModel.getRegisterSuccess()));
        assertEquals("invalido", LiveDataTestUtil.getValue(viewModel.getRegisterError()));
    }

    @Test
    public void register_blackBox_boundaryValues() throws Exception {
        // Boundary: campos vacíos
        fakeRepo.setRegisterShouldSucceed(false);
        fakeRepo.setErrorMessage("empty");
        viewModel.register("", "", "", "");
        assertFalse(LiveDataTestUtil.getValue(viewModel.getRegisterSuccess()));
        assertEquals("empty", LiveDataTestUtil.getValue(viewModel.getRegisterError()));
        // Boundary: campos muy largos
        String longName = "a".repeat(256);
        String longSurname = "b".repeat(256);
        String longEmail = "c".repeat(256) + "@mail.com";
        String longPass = "d".repeat(256);
        fakeRepo.setRegisterShouldSucceed(true);
        viewModel.register(longName, longSurname, longEmail, longPass);
        assertTrue(LiveDataTestUtil.getValue(viewModel.getRegisterSuccess()));
    }
}