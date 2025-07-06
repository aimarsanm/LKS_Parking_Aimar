package com.lksnext.parkingplantilla.view.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.databinding.ActivityMainBinding;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtén el NavController correctamente usando NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.flFragment);
        navController = navHostFragment.getNavController();

        // Configura BottomNavigationView
        BottomNavigationView bottomNavigationView = binding.bottomNavigationView;
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Configura AppBar (opcional)
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.home,
                R.id.ajustes,
                R.id.perfil,
                R.id.listareservas)
                .build();

        // Navega automáticamente al HomeFragment si el registro lo solicita
        if (getIntent() != null && getIntent().getBooleanExtra("navigateToHome", false)) {
            navController.popBackStack(); // Limpia el backstack si es necesario
            navController.navigate(R.id.home);
        }

        // Crear canal de notificaciones al iniciar la app
        com.lksnext.parkingplantilla.util.ReservaNotificationUtil.createNotificationChannel(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}