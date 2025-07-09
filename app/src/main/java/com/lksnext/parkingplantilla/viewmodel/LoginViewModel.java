package com.lksnext.parkingplantilla.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.lksnext.parkingplantilla.data.DataRepository;

public class LoginViewModel extends ViewModel {

    // Aquí puedes declarar los LiveData y métodos necesarios para la vista de inicio de sesión
    MutableLiveData<Boolean> logged = new MutableLiveData<>(null);
    private final MutableLiveData<String> loginError = new MutableLiveData<>();
    private DataRepository dataRepository = DataRepository.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public LoginViewModel() {
        this.dataRepository = DataRepository.getInstance();
    }
    public LoginViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    void setDataRepository(DataRepository mockRepo) {
        this.dataRepository = mockRepo;
    }

    public void setFirebaseAuth(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
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

    // Obtener el usuario autenticado actual
    public com.google.firebase.auth.FirebaseUser getCurrentUser() {
        return dataRepository.getCurrentUser();
    }

    // Cerrar sesión
    public void signOut() {
        firebaseAuth.signOut();
        logged.setValue(Boolean.FALSE);
    }

    // Resetear contraseña
    public void resetPassword(String email, com.lksnext.parkingplantilla.domain.Callback callback) {
        dataRepository.sendPasswordResetEmail(email, callback);
    }
}
