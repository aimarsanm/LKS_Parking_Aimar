package com.lksnext.parkingplantilla.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.google.firebase.auth.FirebaseUser;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.Callback;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LoginViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private LoginViewModel loginViewModel;

    @Mock
    private DataRepository dataRepositoryMock;

    @Mock
    private FirebaseUser firebaseUserMock;

    private MockedStatic<DataRepository> dataRepositoryStaticMock;
    private MockedStatic<com.google.firebase.auth.FirebaseAuth> firebaseAuthStaticMock;
    private com.google.firebase.auth.FirebaseAuth firebaseAuthMock;

    @Before
    public void setUp() {
        dataRepositoryStaticMock = Mockito.mockStatic(DataRepository.class);
        dataRepositoryStaticMock.when(DataRepository::getInstance).thenReturn(dataRepositoryMock);
        firebaseAuthMock = mock(com.google.firebase.auth.FirebaseAuth.class);
        firebaseAuthStaticMock = Mockito.mockStatic(com.google.firebase.auth.FirebaseAuth.class);
        firebaseAuthStaticMock.when(com.google.firebase.auth.FirebaseAuth::getInstance).thenReturn(firebaseAuthMock);
        loginViewModel = new LoginViewModel();
        loginViewModel.setDataRepository(dataRepositoryMock);
    }

    @org.junit.After
    public void tearDown() {
        if (dataRepositoryStaticMock != null) dataRepositoryStaticMock.close();
        if (firebaseAuthStaticMock != null) firebaseAuthStaticMock.close();
    }

    @Test
    public void testLoginUserSuccess() {
        String email = "test@example.com";
        String password = "password123";

        // Simulate successful login callback
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(2);
            callback.onSuccess();
            return null;
        }).when(dataRepositoryMock).login(eq(email), eq(password), any(Callback.class));

        loginViewModel.loginUser(email, password);

        assertEquals(Boolean.TRUE, loginViewModel.isLogged().getValue());
        assertNull(loginViewModel.getLoginError().getValue());
    }

    @Test
    public void testLoginUserFailure() {
        String email = "test@example.com";
        String password = "wrongPassword";
        String errorMsg = "Invalid credentials";

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(2);
            callback.onFailure(errorMsg);
            return null;
        }).when(dataRepositoryMock).login(eq(email), eq(password), any(Callback.class));

        loginViewModel.loginUser(email, password);

        assertEquals(Boolean.FALSE, loginViewModel.isLogged().getValue());
        assertEquals(errorMsg, loginViewModel.getLoginError().getValue());
    }

    @Test
    public void testLoginUserSuccessAndFailure_nullAndEmpty() {
        String email = "user@example.com";
        String password = "pass";

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(2);
            callback.onSuccess();
            return null;
        }).when(dataRepositoryMock).login(eq(email), eq(password), any(Callback.class));

        loginViewModel.loginUser(email, password);
        assertEquals(Boolean.TRUE, loginViewModel.isLogged().getValue());
        assertNull(loginViewModel.getLoginError().getValue());

        String errorMsg = "fail";
        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(2);
            callback.onFailure(errorMsg);
            return null;
        }).when(dataRepositoryMock).login(eq(email), eq(password), any(Callback.class));

        loginViewModel.loginUser(email, password);
        assertEquals(Boolean.FALSE, loginViewModel.isLogged().getValue());
        assertEquals(errorMsg, loginViewModel.getLoginError().getValue());

        loginViewModel.loginUser(null, null);
        verify(dataRepositoryMock).login(isNull(), isNull(), any(Callback.class));

        loginViewModel.loginUser("", "");
        verify(dataRepositoryMock).login(eq(""), eq(""), any(Callback.class));
    }

    @Test
    public void testLoginUser_longValues() {
        String longEmail = "a".repeat(1000);
        String longPassword = "b".repeat(1000);

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(2);
            callback.onSuccess();
            return null;
        }).when(dataRepositoryMock).login(eq(longEmail), eq(longPassword), any(Callback.class));

        loginViewModel.loginUser(longEmail, longPassword);

        assertEquals(Boolean.TRUE, loginViewModel.isLogged().getValue());
    }

    @Test
    public void testGetCurrentUser() {
        when(dataRepositoryMock.getCurrentUser()).thenReturn(firebaseUserMock);

        FirebaseUser currentUser = loginViewModel.getCurrentUser();
        assertEquals(firebaseUserMock, currentUser);
    }

    @Test
    public void testSignOut_multipleCalls() {
        loginViewModel.signOut();
        assertEquals(Boolean.FALSE, loginViewModel.isLogged().getValue());
        verify(firebaseAuthMock, times(1)).signOut();
        loginViewModel.signOut();
        assertEquals(Boolean.FALSE, loginViewModel.isLogged().getValue());
        verify(firebaseAuthMock, times(2)).signOut();
    }

    @Test
    public void testResetPassword_equivalencia() {
        String email = "reset@example.com";

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onSuccess();
            return null;
        }).when(dataRepositoryMock).sendPasswordResetEmail(eq(email), any(Callback.class));
/*
        loginViewModel.resetPassword(email, dummyCallback -> {});
        verify(dataRepositoryMock).sendPasswordResetEmail(eq(email), any(Callback.class));

        loginViewModel.resetPassword("", dummyCallback -> {});
        verify(dataRepositoryMock).sendPasswordResetEmail(eq(""), any(Callback.class));

        loginViewModel.resetPassword(null, dummyCallback -> {});
        verify(dataRepositoryMock).sendPasswordResetEmail(isNull(), any(Callback.class));*/
    }

    @Test
    public void testGetCurrentUser_nullAndNotNull() {
        when(dataRepositoryMock.getCurrentUser()).thenReturn(firebaseUserMock);
        assertEquals(firebaseUserMock, loginViewModel.getCurrentUser());

        when(dataRepositoryMock.getCurrentUser()).thenReturn(null);
        assertNull(loginViewModel.getCurrentUser());
    }
}