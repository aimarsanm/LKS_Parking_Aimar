package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lksnext.parkingplantilla.data.DataRepository;

public class RegisterViewModel extends ViewModel {
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();
    public MutableLiveData<Boolean> getRegisterSuccess() { return registerSuccess; }

    private final MutableLiveData<String> registerError = new MutableLiveData<>();
    public LiveData<String> getRegisterError() { return registerError; }

    private DataRepository dataRepository;

    public RegisterViewModel() {
        this.dataRepository = DataRepository.getInstance();
    }
    public RegisterViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    // Permitir inyección para tests
    void setDataRepository(DataRepository mockRepo) {
        this.dataRepository = mockRepo;
    }

    public void register(String nombre, String apellido, String email, String password) {
        dataRepository.registerWithUserData(email, password, nombre, apellido, new com.lksnext.parkingplantilla.domain.Callback() {
            @Override
            public void onSuccess() {
                registerSuccess.setValue(true);
                registerError.setValue(null);
            }
            @Override
            public void onFailure(String errorMessage) {
                registerSuccess.setValue(false);
                registerError.setValue(errorMessage);
            }
        });
    }
}
