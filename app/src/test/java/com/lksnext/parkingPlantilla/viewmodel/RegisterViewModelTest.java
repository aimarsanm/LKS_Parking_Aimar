package com.lksnext.parkingplantilla.viewmodel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.Callback;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RegisterViewModelTest {

    @Test
    public void testRegisterSuccess() {
        try (MockedStatic<DataRepository> dataRepoStatic = Mockito.mockStatic(DataRepository.class)) {
            DataRepository mockDataRepo = Mockito.mock(DataRepository.class);
            dataRepoStatic.when(DataRepository::getInstance).thenReturn(mockDataRepo);
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(4);
                callback.onSuccess();
                return null;
            }).when(mockDataRepo).registerWithUserData(anyString(), anyString(), anyString(), anyString(), any(Callback.class));
            RegisterViewModel viewModel = new RegisterViewModel();
            viewModel.register("John", "Doe", "john@example.com", "password");
            Boolean success = viewModel.getRegisterSuccess().getValue();
            String error = viewModel.getRegisterError().getValue();
            assertTrue(success != null && success, "Register should succeed");
            assertNull(error, "Error should be null");
        }
    }

    @Test
    public void testRegisterFailure() {
        try (MockedStatic<DataRepository> dataRepoStatic = Mockito.mockStatic(DataRepository.class)) {
            DataRepository mockDataRepo = Mockito.mock(DataRepository.class);
            dataRepoStatic.when(DataRepository::getInstance).thenReturn(mockDataRepo);
            final String errorMessage = "Registration failed.";
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(4);
                callback.onFailure(errorMessage);
                return null;
            }).when(mockDataRepo).registerWithUserData(anyString(), anyString(), anyString(), anyString(), any(Callback.class));
            RegisterViewModel viewModel = new RegisterViewModel();
            viewModel.register("Jane", "Doe", "jane@example.com", "password");
            Boolean success = viewModel.getRegisterSuccess().getValue();
            String error = viewModel.getRegisterError().getValue();
            assertFalse(success != null && success, "Register should fail");
            assertEquals(errorMessage, error, "Error message should match");
        }
    }

    @Test
    public void testRegisterSuccess_nullValues() {
        try (MockedStatic<DataRepository> dataRepoStatic = Mockito.mockStatic(DataRepository.class)) {
            DataRepository mockDataRepo = Mockito.mock(DataRepository.class);
            dataRepoStatic.when(DataRepository::getInstance).thenReturn(mockDataRepo);
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(4);
                callback.onSuccess();
                return null;
            }).when(mockDataRepo).registerWithUserData(anyString(), anyString(), anyString(), anyString(), any(Callback.class));
            RegisterViewModel viewModel = new RegisterViewModel();
            // Prueba con valores nulos
            viewModel.register(null, null, null, null);
            Boolean success = viewModel.getRegisterSuccess().getValue();
            String error = viewModel.getRegisterError().getValue();
            assertTrue(success != null && success, "Register should succeed with nulls (simulado)");
            assertNull(error, "Error should be null");
        }
    }

    @Test
    public void testRegisterFailure_emptyValues() {
        try (MockedStatic<DataRepository> dataRepoStatic = Mockito.mockStatic(DataRepository.class)) {
            DataRepository mockDataRepo = Mockito.mock(DataRepository.class);
            dataRepoStatic.when(DataRepository::getInstance).thenReturn(mockDataRepo);
            final String errorMessage = "Registration failed.";
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(4);
                callback.onFailure(errorMessage);
                return null;
            }).when(mockDataRepo).registerWithUserData(anyString(), anyString(), anyString(), anyString(), any(Callback.class));
            RegisterViewModel viewModel = new RegisterViewModel();
            // Prueba con valores vacíos
            viewModel.register("", "", "", "");
            Boolean success = viewModel.getRegisterSuccess().getValue();
            String error = viewModel.getRegisterError().getValue();
            assertFalse(success != null && success, "Register should fail with empty values (simulado)");
            assertEquals(errorMessage, error, "Error message should match");
        }
    }

    @Test
    public void testRegisterSuccess_longValues() {
        try (MockedStatic<DataRepository> dataRepoStatic = Mockito.mockStatic(DataRepository.class)) {
            DataRepository mockDataRepo = Mockito.mock(DataRepository.class);
            dataRepoStatic.when(DataRepository::getInstance).thenReturn(mockDataRepo);
            doAnswer(invocation -> {
                Callback callback = invocation.getArgument(4);
                callback.onSuccess();
                return null;
            }).when(mockDataRepo).registerWithUserData(anyString(), anyString(), anyString(), anyString(), any(Callback.class));
            RegisterViewModel viewModel = new RegisterViewModel();
            String longStr = "a".repeat(1000);
            viewModel.register(longStr, longStr, longStr, longStr);
            Boolean success = viewModel.getRegisterSuccess().getValue();
            String error = viewModel.getRegisterError().getValue();
            assertTrue(success != null && success, "Register should succeed with long values (simulado)");
            assertNull(error, "Error should be null");
        }
    }

    @Test
    public void testRegisterFailure_nullCallback() {
        try (MockedStatic<DataRepository> dataRepoStatic = Mockito.mockStatic(DataRepository.class)) {
            DataRepository mockDataRepo = Mockito.mock(DataRepository.class);
            dataRepoStatic.when(DataRepository::getInstance).thenReturn(mockDataRepo);
            // No llama a callback
            doAnswer(invocation -> null).when(mockDataRepo).registerWithUserData(anyString(), anyString(), anyString(), anyString(), any(Callback.class));
            RegisterViewModel viewModel = new RegisterViewModel();
            viewModel.register("a", "b", "c", "d");
            // No debe cambiar nada
            assertNull(viewModel.getRegisterSuccess().getValue());
            assertNull(viewModel.getRegisterError().getValue());
        }
    }
}
