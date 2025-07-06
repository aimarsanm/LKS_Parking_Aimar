package com.lksnext.parkingplantilla.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.viewmodel.ReservaViewModel;

public class HomeFragment extends Fragment {

    private ReservaViewModel reservaViewModel;
    private ImageView estadoCoche, estadoHibrido, estadoMoto, estadoDiscapacitado;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        reservaViewModel = new ViewModelProvider(requireActivity()).get(ReservaViewModel.class);

        estadoCoche = root.findViewById(R.id.estado_coche);
        estadoHibrido = root.findViewById(R.id.estado_hibrido);
        estadoMoto = root.findViewById(R.id.estado_moto);
        estadoDiscapacitado = root.findViewById(R.id.estado_discapacitado);

        actualizarPlazasLibres();

        reservaViewModel.getReservaExitosa().observe(getViewLifecycleOwner(), exito -> {
            if (exito != null && exito) {
                actualizarPlazasLibres();
            }
        });
        // También actualizar al cancelar reserva
        reservaViewModel.getReservasUsuario().observe(getViewLifecycleOwner(), reservas -> {
            actualizarPlazasLibres();
        });

        return root;
    }

    private void actualizarPlazasLibres() {
        String fechaHoy = obtenerFechaHoy();
        int minutosActuales = obtenerMinutosActuales();
        reservaViewModel.getPlazasLibresFirestore("Coche", fechaHoy, minutosActuales, libres -> {
            if (libres > 0) {
                estadoCoche.setImageResource(R.drawable.circle_green);
            } else {
                estadoCoche.setImageResource(R.drawable.circle_red);
            }
        });
        reservaViewModel.getPlazasLibresFirestore("Hibrido", fechaHoy, minutosActuales, libres -> {
            if (libres > 0) {
                estadoHibrido.setImageResource(R.drawable.circle_green);
            } else {
                estadoHibrido.setImageResource(R.drawable.circle_red);
            }
        });
        reservaViewModel.getPlazasLibresFirestore("Moto", fechaHoy, minutosActuales, libres -> {
            if (libres > 0) {
                estadoMoto.setImageResource(R.drawable.circle_green);
            } else {
                estadoMoto.setImageResource(R.drawable.circle_red);
            }
        });
        reservaViewModel.getPlazasLibresFirestore("Discapacitado", fechaHoy, minutosActuales, libres -> {
            if (libres > 0) {
                estadoDiscapacitado.setImageResource(R.drawable.circle_green);
            } else {
                estadoDiscapacitado.setImageResource(R.drawable.circle_red);
            }
        });
    }

    private String obtenerFechaHoy() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new java.util.Date());
    }

    private int obtenerMinutosActuales() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        return cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE);
    }
}