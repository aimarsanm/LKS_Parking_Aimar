package com.lksnext.parkingplantilla.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.databinding.ActivityLoginBinding;
import com.lksnext.parkingplantilla.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {
    NavController navController;
    BottomNavigationView bottomNavigationView;
    private ActivityLoginBinding binding;
    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //prueba
        //Asignamos la vista/interfaz login (layout)
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Asignamos el viewModel de login
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Show BottomNavigationView in LoginActivity
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.flFragment);
            if (navHostFragment != null) {
                NavController navController = navHostFragment.getNavController();
                NavigationUI.setupWithNavController(bottomNavigationView, navController);
            }
        }

        //Acciones a realizar cuando el usuario clica el boton de login
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailText.getText().toString();
            String password = binding.passwordText.getText().toString();
            loginViewModel.loginUser(email, password);
        });

        //Acciones a realizar cuando el usuario clica el boton de crear cuenta (se cambia de pantalla)
        binding.createAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        //Observamos la variable logged, la cual nos informara cuando el usuario intente hacer login y se
        //cambia de pantalla en caso de login correcto
        loginViewModel.isLogged().observe(this, logged -> {
            if (logged != null) {
                if (logged) {
                    //Login Correcto
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    //Login incorrecto
                    android.widget.Toast.makeText(this, "Email o contraseña incorrectos", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginViewModel.getLoginError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                android.widget.Toast.makeText(this, errorMsg, android.widget.Toast.LENGTH_LONG).show();
            }
        });

        // Acción para el enlace de recuperación de contraseña
        binding.textView.setOnClickListener(v -> {
            String email = binding.emailText.getText().toString();
            if (email.isEmpty()) {
                android.widget.Toast.makeText(this, "Introduce tu email para recuperar la contraseña", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                loginViewModel.resetPassword(email, new com.lksnext.parkingplantilla.domain.Callback() {
                    @Override
                    public void onSuccess() {
                        android.widget.Toast.makeText(LoginActivity.this, "Email de recuperación enviado", android.widget.Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onFailure(String errorMessage) {
                        android.widget.Toast.makeText(LoginActivity.this, "Error: " + errorMessage, android.widget.Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}