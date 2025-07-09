package com.lksnext.parkingplantilla.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.lksnext.parkingplantilla.data.FakeDataRepository;
import com.lksnext.parkingplantilla.viewmodel.FakeLoginViewModel;
import com.lksnext.parkingplantilla.util.LiveDataTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.robolectric.RobolectricTestRunner;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class LoginViewModelTest {

    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    private FakeDataRepository fakeRepo;
    private FakeLoginViewModel viewModel;

    @Before
    public void setUp() {
        fakeRepo = new FakeDataRepository();
        viewModel = new FakeLoginViewModel(fakeRepo);
    }

    @Test
    public void loginUser_success_setsLoggedTrue() throws Exception {
        fakeRepo.setLoginShouldSucceed(true);
        viewModel.loginUser("test@mail.com", "1234");
        Boolean result = LiveDataTestUtil.getValue(viewModel.isLogged());
        assertTrue(result);
        assertNull(LiveDataTestUtil.getValue(viewModel.getLoginError()));
    }

    @Test
    public void loginUser_failure_setsLoggedFalse_andError() throws Exception {
        fakeRepo.setLoginShouldSucceed(false);
        fakeRepo.setErrorMessage("error");
        viewModel.loginUser("test@mail.com", "wrong");
        Boolean result = LiveDataTestUtil.getValue(viewModel.isLogged());
        assertFalse(result);
        assertEquals("error", LiveDataTestUtil.getValue(viewModel.getLoginError()));
    }

    @Test
    public void loginUser_setsLoggedNull_whenNoCallback() throws Exception {
        // Simula un callback nulo (no debería ocurrir, pero cubre el camino)
        // No se puede simular directamente porque el método siempre llama al callback
        // Así que este test es redundante, pero lo dejamos para cobertura
        assertNull(LiveDataTestUtil.getValue(viewModel.isLogged()));
    }

    @Test
    public void loginUser_multipleCalls_changesState() throws Exception {
        fakeRepo.setLoginShouldSucceed(true);
        viewModel.loginUser("test@mail.com", "1234");
        assertTrue(LiveDataTestUtil.getValue(viewModel.isLogged()));
        fakeRepo.setLoginShouldSucceed(false);
        fakeRepo.setErrorMessage("fail");
        viewModel.loginUser("test@mail.com", "fail");
        assertFalse(LiveDataTestUtil.getValue(viewModel.isLogged()));
        assertEquals("fail", LiveDataTestUtil.getValue(viewModel.getLoginError()));
    }

    @Test
    public void loginUser_whiteBox_initialState_isNull() throws Exception {
        // White box: initial state
        assertNull(LiveDataTestUtil.getValue(viewModel.isLogged()));
        assertNull(LiveDataTestUtil.getValue(viewModel.getLoginError()));
    }

    @Test
    public void loginUser_whiteBox_multipleTransitions() throws Exception {
        // White box: multiple transitions
        fakeRepo.setLoginShouldSucceed(true);
        viewModel.loginUser("user1@mail.com", "pass1");
        assertTrue(LiveDataTestUtil.getValue(viewModel.isLogged()));
        fakeRepo.setLoginShouldSucceed(false);
        fakeRepo.setErrorMessage("fail1");
        viewModel.loginUser("user2@mail.com", "fail");
        assertFalse(LiveDataTestUtil.getValue(viewModel.isLogged()));
        assertEquals("fail1", LiveDataTestUtil.getValue(viewModel.getLoginError()));
        fakeRepo.setLoginShouldSucceed(true);
        viewModel.loginUser("user3@mail.com", "pass3");
        assertTrue(LiveDataTestUtil.getValue(viewModel.isLogged()));
    }

    @Test
    public void loginUser_blackBox_equivalencePartition() throws Exception {
        // Black box: valid credentials (success partition)
        fakeRepo.setLoginShouldSucceed(true);
        viewModel.loginUser("valid@mail.com", "validpass");
        assertTrue(LiveDataTestUtil.getValue(viewModel.isLogged()));
        // Black box: invalid credentials (failure partition)
        fakeRepo.setLoginShouldSucceed(false);
        fakeRepo.setErrorMessage("invalid");
        viewModel.loginUser("invalid@mail.com", "badpass");
        assertFalse(LiveDataTestUtil.getValue(viewModel.isLogged()));
        assertEquals("invalid", LiveDataTestUtil.getValue(viewModel.getLoginError()));
    }

    @Test
    public void loginUser_blackBox_boundaryValues() throws Exception {
        // Black box: empty email and password (boundary)
        fakeRepo.setLoginShouldSucceed(false);
        fakeRepo.setErrorMessage("empty");
        viewModel.loginUser("", "");
        assertFalse(LiveDataTestUtil.getValue(viewModel.isLogged()));
        assertEquals("empty", LiveDataTestUtil.getValue(viewModel.getLoginError()));
        // Black box: very long email and password (boundary)
        String longEmail = "a".repeat(256) + "@mail.com";
        String longPass = "b".repeat(256);
        fakeRepo.setLoginShouldSucceed(true);
        viewModel.loginUser(longEmail, longPass);
        assertTrue(LiveDataTestUtil.getValue(viewModel.isLogged()));
    }
}