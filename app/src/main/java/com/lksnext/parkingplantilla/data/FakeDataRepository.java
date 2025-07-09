package com.lksnext.parkingplantilla.data;

public class FakeDataRepository {
    private boolean loginShouldSucceed = true;
    private boolean registerShouldSucceed = true;
    private String errorMessage = "error";

    public void setLoginShouldSucceed(boolean value) {
        this.loginShouldSucceed = value;
    }
    public void setRegisterShouldSucceed(boolean value) {
        this.registerShouldSucceed = value;
    }
    public void setErrorMessage(String msg) {
        this.errorMessage = msg;
    }

    public void login(String email, String password, com.lksnext.parkingplantilla.domain.Callback callback) {
        if (loginShouldSucceed) {
            callback.onSuccess();
        } else {
            callback.onFailure(errorMessage);
        }
    }

    public void registerWithUserData(String email, String password, String nombre, String apellido, com.lksnext.parkingplantilla.domain.Callback callback) {
        if (registerShouldSucceed) {
            callback.onSuccess();
        } else {
            callback.onFailure(errorMessage);
        }
    }
    // Agrega otros métodos fake si tus tests los necesitan
}
