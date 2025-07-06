package com.lksnext.parkingplantilla.viewmodel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import com.google.firebase.auth.FirebaseUser;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.Callback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Field;

@ExtendWith(MockitoExtension.class)
class LoginViewModelTest {

    private LoginViewModel loginViewModel;

    @Mock
    private DataRepository dataRepositoryMock;

    @Mock
    private FirebaseUser firebaseUserMock;

    @Mock
    private Callback dummyCallback; // For resetPassword test

    @BeforeEach
    public void setUp() throws Exception {
        loginViewModel = new LoginViewModel();
        // Replace the internal dataRepository with our mock using reflection
        Field field = LoginViewModel.class.getDeclaredField("dataRepository");
        field.setAccessible(true);
        field.set(loginViewModel, dataRepositoryMock);
    }

    @Test
    public void testLoginUserSuccess() {
        String email = "test@example.com";
        String password = "password123";
        loginViewModel.loginUser(email, password);
        ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
        verify(dataRepositoryMock).login(email, password, captor.capture());
        captor.getValue().onSuccess();
        assertEquals(Boolean.TRUE, loginViewModel.isLogged().getValue());
        assertNull(loginViewModel.getLoginError().getValue());
    }

    @Test
    public void testLoginUserFailure() {
        String email = "test@example.com";
        String password = "wrongPassword";
        String errorMsg = "Invalid credentials";
        loginViewModel.loginUser(email, password);
        ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
        verify(dataRepositoryMock).login(email, password, captor.capture());
        captor.getValue().onFailure(errorMsg);
        assertEquals(Boolean.FALSE, loginViewModel.isLogged().getValue());
        assertEquals(errorMsg, loginViewModel.getLoginError().getValue());
    }

    // Caja blanca: cobertura de condiciones y ramas para loginUser
    @Test
    public void testLoginUserSuccessAndFailure_nullAndEmpty() {
        // Caso éxito
        String email = "user@example.com";
        String password = "pass";
        loginViewModel.loginUser(email, password);
        ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
        verify(dataRepositoryMock).login(eq(email), eq(password), captor.capture());
        captor.getValue().onSuccess();
        assertEquals(Boolean.TRUE, loginViewModel.isLogged().getValue());
        assertNull(loginViewModel.getLoginError().getValue());
        // Caso fallo
        String errorMsg = "fail";
        loginViewModel.loginUser(email, password);
        verify(dataRepositoryMock, atLeastOnce()).login(eq(email), eq(password), captor.capture());
        captor.getValue().onFailure(errorMsg);
        assertEquals(Boolean.FALSE, loginViewModel.isLogged().getValue());
        assertEquals(errorMsg, loginViewModel.getLoginError().getValue());
        // Caso valores nulos
        loginViewModel.loginUser(null, null);
        verify(dataRepositoryMock, atLeastOnce()).login(isNull(), isNull(), captor.capture());
        captor.getValue().onFailure(errorMsg);
        assertEquals(Boolean.FALSE, loginViewModel.isLogged().getValue());
        // Caso valores vacíos
        loginViewModel.loginUser("", "");
        verify(dataRepositoryMock, atLeastOnce()).login(eq(""), eq(""), captor.capture());
        captor.getValue().onFailure(errorMsg);
        assertEquals(Boolean.FALSE, loginViewModel.isLogged().getValue());
    }

    // Caja negra: valores límite para loginUser
    @Test
    public void testLoginUser_longValues() {
        String longEmail = "a".repeat(1000);
        String longPassword = "b".repeat(1000);
        loginViewModel.loginUser(longEmail, longPassword);
        ArgumentCaptor<Callback> captor = ArgumentCaptor.forClass(Callback.class);
        verify(dataRepositoryMock).login(eq(longEmail), eq(longPassword), captor.capture());
        captor.getValue().onSuccess();
        assertEquals(Boolean.TRUE, loginViewModel.isLogged().getValue());
    }

    @Test
    public void testGetCurrentUser() {
        when(dataRepositoryMock.getCurrentUser()).thenReturn(firebaseUserMock);
        FirebaseUser currentUser = loginViewModel.getCurrentUser();
        assertEquals(firebaseUserMock, currentUser);
    }

    // Caja blanca: cobertura de ramas para signOut
    @Test
    public void testSignOut_multipleCalls() {
        loginViewModel.signOut();
        assertEquals(Boolean.FALSE, loginViewModel.isLogged().getValue());
        // Llamada repetida
        loginViewModel.signOut();
        assertEquals(Boolean.FALSE, loginViewModel.isLogged().getValue());
    }

    // Caja negra: partición de equivalencia para resetPassword
    @Test
    public void testResetPassword_equivalencia() {
        // Email válido
        loginViewModel.resetPassword("reset@example.com", dummyCallback);
        verify(dataRepositoryMock).sendPasswordResetEmail(eq("reset@example.com"), eq(dummyCallback));
        // Email vacío
        loginViewModel.resetPassword("", dummyCallback);
        verify(dataRepositoryMock).sendPasswordResetEmail(eq(""), eq(dummyCallback));
        // Email nulo
        loginViewModel.resetPassword(null, dummyCallback);
        verify(dataRepositoryMock).sendPasswordResetEmail(isNull(), eq(dummyCallback));
    }

    // Caja blanca: cobertura de ramas para getCurrentUser
    @Test
    public void testGetCurrentUser_nullAndNotNull() {
        when(dataRepositoryMock.getCurrentUser()).thenReturn(firebaseUserMock);
        assertEquals(firebaseUserMock, loginViewModel.getCurrentUser());
        when(dataRepositoryMock.getCurrentUser()).thenReturn(null);
        assertNull(loginViewModel.getCurrentUser());
    }
}
