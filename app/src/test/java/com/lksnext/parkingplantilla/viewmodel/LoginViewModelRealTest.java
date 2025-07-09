package com.lksnext.parkingplantilla.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.Callback;
import com.lksnext.parkingplantilla.util.LiveDataTestUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class LoginViewModelRealTest {
    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    private DataRepository mockRepo;
    private LoginViewModel viewModel;

    @Before
    public void setUp() {
        mockRepo = mock(DataRepository.class);
        viewModel = new LoginViewModel(mockRepo);
    }

    @Test
    public void loginUser_success_setsLoggedTrue() throws Exception {
        doAnswer(invocation -> {
            Callback cb = (Callback) invocation.getArguments()[2];
            cb.onSuccess();
            return null;
        }).when(mockRepo).login(anyString(), anyString(), any(Callback.class));
        viewModel.loginUser("test@mail.com", "1234");
        Boolean result = LiveDataTestUtil.getValue(viewModel.isLogged());
        assertTrue(result);
        assertNull(LiveDataTestUtil.getValue(viewModel.getLoginError()));
    }

    @Test
    public void loginUser_failure_setsLoggedFalse_andError() throws Exception {
        doAnswer(invocation -> {
            Callback cb = (Callback) invocation.getArguments()[2];
            cb.onFailure("error");
            return null;
        }).when(mockRepo).login(anyString(), anyString(), any(Callback.class));
        viewModel.loginUser("test@mail.com", "wrong");
        Boolean result = LiveDataTestUtil.getValue(viewModel.isLogged());
        assertFalse(result);
        assertEquals("error", LiveDataTestUtil.getValue(viewModel.getLoginError()));
    }

    @Test
    public void loginUser_whiteBox_multipleTransitions() throws Exception {
        doAnswer(invocation -> {
            Callback cb = (Callback) invocation.getArguments()[2];
            cb.onSuccess();
            return null;
        }).when(mockRepo).login(Mockito.eq("user1@mail.com"), anyString(), any(Callback.class));
        doAnswer(invocation -> {
            Callback cb = (Callback) invocation.getArguments()[2];
            cb.onFailure("fail1");
            return null;
        }).when(mockRepo).login(Mockito.eq("user2@mail.com"), anyString(), any(Callback.class));
        doAnswer(invocation -> {
            Callback cb = (Callback) invocation.getArguments()[2];
            cb.onSuccess();
            return null;
        }).when(mockRepo).login(Mockito.eq("user3@mail.com"), anyString(), any(Callback.class));
        viewModel.loginUser("user1@mail.com", "pass1");
        assertTrue(LiveDataTestUtil.getValue(viewModel.isLogged()));
        viewModel.loginUser("user2@mail.com", "fail");
        assertFalse(LiveDataTestUtil.getValue(viewModel.isLogged()));
        assertEquals("fail1", LiveDataTestUtil.getValue(viewModel.getLoginError()));
        viewModel.loginUser("user3@mail.com", "pass3");
        assertTrue(LiveDataTestUtil.getValue(viewModel.isLogged()));
    }

    @Test
    public void loginUser_blackBox_boundaryValues() throws Exception {
        doAnswer(invocation -> {
            Callback cb = (Callback) invocation.getArguments()[2];
            cb.onFailure("empty");
            return null;
        }).when(mockRepo).login(Mockito.eq(""), Mockito.eq(""), any(Callback.class));
        viewModel.loginUser("", "");
        assertFalse(LiveDataTestUtil.getValue(viewModel.isLogged()));
        assertEquals("empty", LiveDataTestUtil.getValue(viewModel.getLoginError()));
        String longEmail = "a".repeat(256) + "@mail.com";
        String longPass = "b".repeat(256);
        doAnswer(invocation -> {
            Callback cb = (Callback) invocation.getArguments()[2];
            cb.onSuccess();
            return null;
        }).when(mockRepo).login(Mockito.eq(longEmail), Mockito.eq(longPass), any(Callback.class));
        viewModel.loginUser(longEmail, longPass);
        assertTrue(LiveDataTestUtil.getValue(viewModel.isLogged()));
    }

    @Test
    public void signOut_setsLoggedFalse() throws Exception {
        // Prepara un mock de FirebaseAuth
        com.google.firebase.auth.FirebaseAuth mockAuth = mock(com.google.firebase.auth.FirebaseAuth.class);
        viewModel.setFirebaseAuth(mockAuth);
        // Llama a signOut
        viewModel.signOut();
        Boolean result = LiveDataTestUtil.getValue(viewModel.isLogged());
        assertFalse(result);
        // Verifica que se llamó a signOut en el mock
        Mockito.verify(mockAuth).signOut();
    }

    @Test
    public void resetPassword_callsRepository() throws Exception {
        // Prepara un callback
        Callback callback = mock(Callback.class);
        // Llama a resetPassword
        viewModel.resetPassword("mail@mail.com", callback);
        // Verifica que se llamó a sendPasswordResetEmail en el repo
        Mockito.verify(mockRepo).sendPasswordResetEmail(Mockito.eq("mail@mail.com"), Mockito.eq(callback));
    }

    @Test
    public void getCurrentUser_returnsRepositoryUser() {
        com.google.firebase.auth.FirebaseUser mockUser = mock(com.google.firebase.auth.FirebaseUser.class);
        when(mockRepo.getCurrentUser()).thenReturn(mockUser);
        assertEquals(mockUser, viewModel.getCurrentUser());
    }
}
