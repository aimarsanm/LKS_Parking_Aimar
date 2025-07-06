package com.lksnext.parkingplantilla.view.activity;

import android.os.Bundle;
import android.util.Patterns;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.parkingplantilla.databinding.ActivityRegisterBinding;
import com.lksnext.parkingplantilla.viewmodel.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel registerViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Asignamos la vista/interfaz de registro
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Asignamos el viewModel de register
        registerViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        binding.btnRegister.setOnClickListener(v -> {
            String nombre = binding.editNombre.getText() != null ? binding.editNombre.getText().toString() : "";
            String apellido = binding.editApellido.getText() != null ? binding.editApellido.getText().toString() : "";
            String email = binding.editEmail.getText() != null ? binding.editEmail.getText().toString() : "";
            String password = binding.editPassword.getText() != null ? binding.editPassword.getText().toString() : "";
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                android.widget.Toast.makeText(this, "Introduce un email válido", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty() || password.length() < 6) {
                android.widget.Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            registerViewModel.register(nombre, apellido, email, password);
        });

        registerViewModel.getRegisterSuccess().observe(this, success -> {
            if (success != null && success) {
                // Registro correcto, navegar a HomeFragment
                android.content.Intent intent = new android.content.Intent(this, com.lksnext.parkingplantilla.view.activity.MainActivity.class);
                intent.putExtra("navigateToHome", true);
                startActivity(intent);
                finish();
            }
        });

        registerViewModel.getRegisterError().observe(this, errorMsg -> {
            if (errorMsg != null) {
                android.widget.Toast.makeText(this, errorMsg, android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }
}