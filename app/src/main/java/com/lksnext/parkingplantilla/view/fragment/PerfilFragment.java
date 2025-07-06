package com.lksnext.parkingplantilla.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.view.activity.LoginActivity;

public class PerfilFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);
        View layoutLogin = view.findViewById(R.id.layoutLogin);
        View layoutPerfil = view.findViewById(R.id.layoutPerfil);
        TextView tvNombre = view.findViewById(R.id.tvNombre);
        TextView tvApellido = view.findViewById(R.id.tvApellido);
        TextView tvEmail = view.findViewById(R.id.tvEmail);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnLogin = view.findViewById(R.id.loginButton);
        TextView tvRegister = view.findViewById(R.id.createAccount);
        com.google.android.material.textfield.TextInputEditText emailText = view.findViewById(R.id.emailText);
        com.google.android.material.textfield.TextInputEditText passwordText = view.findViewById(R.id.passwordText);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            layoutLogin.setVisibility(View.VISIBLE);
            layoutPerfil.setVisibility(View.GONE);
        } else {
            layoutLogin.setVisibility(View.GONE);
            layoutPerfil.setVisibility(View.VISIBLE);
            // Obtener datos del usuario desde Firestore
            DataRepository.getInstance().leerDatosUsuarioFirestore(new DataRepository.UsuarioCallback() {
                @Override
                public void onUsuarioLoaded(com.lksnext.parkingplantilla.domain.Usuario usuario) {
                    if (usuario != null) {
                        tvNombre.setText(getString(R.string.perfil_nombre, usuario.getNombre()));
                        tvApellido.setText(getString(R.string.perfil_apellido, usuario.getApellido()));
                        tvEmail.setText(getString(R.string.perfil_email, usuario.getEmail()));
                    }
                }
            });
        }

        btnLogin.setOnClickListener(v -> {
            String email = emailText.getText() != null ? emailText.getText().toString() : "";
            String password = passwordText.getText() != null ? passwordText.getText().toString() : "";
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                android.widget.Toast.makeText(requireContext(), "Introduce un email válido", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty() || password.length() < 6) {
                android.widget.Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Al iniciar sesión correctamente, navega al fragmento home
                        androidx.navigation.Navigation.findNavController(requireView()).navigate(R.id.home);
                    } else {
                        android.widget.Toast.makeText(requireContext(), "Login incorrecto", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            // Refresca el fragment usando NavController
            androidx.navigation.Navigation.findNavController(requireView()).navigate(R.id.perfil);
        });

        tvRegister.setOnClickListener(v -> {
            // Navega a la pantalla de registro
            Intent intent = new Intent(requireContext(), com.lksnext.parkingplantilla.view.activity.RegisterActivity.class);
            startActivity(intent);
        });

        // Al volver del registro, si el usuario está logueado, refresca el fragment para mostrar el perfil
        if (user == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            // Refresca el fragment usando NavController
            androidx.navigation.Navigation.findNavController(requireView()).navigate(R.id.perfil);
        }

        return view;
    }
}
