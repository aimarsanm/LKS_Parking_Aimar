package com.lksnext.parkingplantilla.viewmodel;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RegisterViewModelTest {

    @Rule
    public androidx.arch.core.executor.testing.InstantTaskExecutorRule instantTaskExecutorRule = new androidx.arch.core.executor.testing.InstantTaskExecutorRule();

    // Utilidad para obtener el valor de LiveData de forma síncrona en tests
    private <T> T getOrAwaitValue(androidx.lifecycle.LiveData<T> liveData) throws InterruptedException {
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        final Object[] data = new Object[1];
        androidx.lifecycle.Observer<T> observer = new androidx.lifecycle.Observer<T>() {
            @Override
            public void onChanged(T t) {
                data[0] = t;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        liveData.observeForever(observer);
        latch.await(2, java.util.concurrent.TimeUnit.SECONDS);
        //noinspection unchecked
        return (T) data[0];
    }

    private MockedStatic<com.lksnext.parkingplantilla.data.DataRepository> dataRepositoryStaticMock;
    private com.lksnext.parkingplantilla.data.DataRepository dataRepositoryMock;

    @org.junit.Before
    public void setUp() {
        dataRepositoryStaticMock = Mockito.mockStatic(com.lksnext.parkingplantilla.data.DataRepository.class);
        dataRepositoryMock = Mockito.mock(com.lksnext.parkingplantilla.data.DataRepository.class);
        dataRepositoryStaticMock.when(com.lksnext.parkingplantilla.data.DataRepository::getInstance).thenReturn(dataRepositoryMock);
    }

    @org.junit.After
    public void tearDown() {
        if (dataRepositoryStaticMock != null) dataRepositoryStaticMock.close();
    }

    @Test
    public void testRegisterSuccess() {
        RegisterViewModel viewModel = new RegisterViewModel();
        viewModel.setDataRepository(dataRepositoryMock);
        doAnswer(invocation -> {
            com.lksnext.parkingplantilla.domain.Callback callback = invocation.getArgument(4);
            callback.onSuccess();
            return null;
        }).when(dataRepositoryMock).registerWithUserData(any(), any(), any(), any(), any(com.lksnext.parkingplantilla.domain.Callback.class));
        viewModel.register("John", "Doe", "john@example.com", "password");
        Boolean success = viewModel.getRegisterSuccess().getValue();
        String error = viewModel.getRegisterError().getValue();
        assertTrue(success != null && success);
        assertNull(error);
    }

    @Test
    public void testRegisterFailure() {
        RegisterViewModel viewModel = new RegisterViewModel();
        viewModel.setDataRepository(dataRepositoryMock);
        final String errorMessage = "Registration failed.";
        doAnswer(invocation -> {
            com.lksnext.parkingplantilla.domain.Callback callback = invocation.getArgument(4);
            callback.onFailure(errorMessage);
            return null;
        }).when(dataRepositoryMock).registerWithUserData(any(), any(), any(), any(), any(com.lksnext.parkingplantilla.domain.Callback.class));
        viewModel.register("Jane", "Doe", "jane@example.com", "password");
        Boolean success = viewModel.getRegisterSuccess().getValue();
        String error = viewModel.getRegisterError().getValue();
        assertFalse(success != null && success);
        assertEquals(errorMessage, error);
    }

    @Test
    public void testRegisterSuccess_nullValues() throws InterruptedException {
        RegisterViewModel viewModel = new RegisterViewModel();
        viewModel.setDataRepository(dataRepositoryMock);
        doAnswer(invocation -> {
            com.lksnext.parkingplantilla.domain.Callback callback = invocation.getArgument(4);
            callback.onSuccess();
            return null;
        }).when(dataRepositoryMock).registerWithUserData(any(), any(), any(), any(), any(com.lksnext.parkingplantilla.domain.Callback.class));
        viewModel.register(null, null, null, null);
        Boolean success = getOrAwaitValue(viewModel.getRegisterSuccess());
        String error = getOrAwaitValue(viewModel.getRegisterError());
        assertTrue(success != null && success);
        assertNull(error);
    }

    @Test
    public void testRegisterFailure_emptyValues() {
        RegisterViewModel viewModel = new RegisterViewModel();
        viewModel.setDataRepository(dataRepositoryMock);
        final String errorMessage = "Registration failed.";
        doAnswer(invocation -> {
            com.lksnext.parkingplantilla.domain.Callback callback = invocation.getArgument(4);
            callback.onFailure(errorMessage);
            return null;
        }).when(dataRepositoryMock).registerWithUserData(any(), any(), any(), any(), any(com.lksnext.parkingplantilla.domain.Callback.class));
        viewModel.register("", "", "", "");
        Boolean success = viewModel.getRegisterSuccess().getValue();
        String error = viewModel.getRegisterError().getValue();
        assertFalse(success != null && success);
        assertEquals(errorMessage, error);
    }

    @Test
    public void testRegisterSuccess_longValues() {
        RegisterViewModel viewModel = new RegisterViewModel();
        viewModel.setDataRepository(dataRepositoryMock);
        doAnswer(invocation -> {
            com.lksnext.parkingplantilla.domain.Callback callback = invocation.getArgument(4);
            callback.onSuccess();
            return null;
        }).when(dataRepositoryMock).registerWithUserData(any(), any(), any(), any(), any(com.lksnext.parkingplantilla.domain.Callback.class));
        String longStr = new String(new char[1000]).replace('\0', 'a');
        viewModel.register(longStr, longStr, longStr, longStr);
        Boolean success = viewModel.getRegisterSuccess().getValue();
        String error = viewModel.getRegisterError().getValue();
        assertTrue(success != null && success);
        assertNull(error);
    }

    @Test
    public void testRegisterFailure_nullCallback() {
        RegisterViewModel viewModel = new RegisterViewModel();
        viewModel.setDataRepository(dataRepositoryMock);
        doAnswer(invocation -> null).when(dataRepositoryMock).registerWithUserData(anyString(), anyString(), anyString(), anyString(), any(com.lksnext.parkingplantilla.domain.Callback.class));
        viewModel.register("a", "b", "c", "d");
        assertNull(viewModel.getRegisterSuccess().getValue());
        assertNull(viewModel.getRegisterError().getValue());
    }
}
