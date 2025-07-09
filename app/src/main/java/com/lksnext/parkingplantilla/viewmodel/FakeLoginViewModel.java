package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.lksnext.parkingplantilla.data.FakeDataRepository;

public class FakeLoginViewModel extends ViewModel {
    MutableLiveData<Boolean> logged = new MutableLiveData<>(null);
    private final MutableLiveData<String> loginError = new MutableLiveData<>();
    private FakeDataRepository dataRepository;

    public FakeLoginViewModel(FakeDataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public LiveData<Boolean> isLogged() {
        return logged;
    }

    public LiveData<String> getLoginError() {
        return loginError;
    }

    public void loginUser(String email, String password) {
        dataRepository.login(email, password, new com.lksnext.parkingplantilla.domain.Callback() {
            @Override
            public void onSuccess() {
                logged.setValue(Boolean.TRUE);
                loginError.setValue(null);
            }
            @Override
            public void onFailure(String errorMessage) {
                logged.setValue(Boolean.FALSE);
                loginError.setValue(errorMessage);
            }
        });
    }
}

