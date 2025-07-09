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
}