package com.lksnext.parkingplantilla.view.fragment;
//a
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.content.ContextCompat;

import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.Hora;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.viewmodel.ReservaViewModel;
import com.lksnext.parkingplantilla.view.activity.LoginActivity;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.Calendar;
import java.util.UUID;

public class CrearReservaFragment extends Fragment {

    private ReservaViewModel viewModel;
    private Spinner tipoPlazaSpinner;
    private EditText fechaInput, horaInicioInput, horaFinInput;
    private Button reservarBtn;

    private String fechaSeleccionada;
    private int horaInicio, minutoInicio, horaFin, minutoFin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crear_reserva, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tipoPlazaSpinner = view.findViewById(R.id.spinner_tipo_plaza);
        fechaInput = view.findViewById(R.id.input_fecha);
        horaInicioInput = view.findViewById(R.id.input_hora_inicio);
        horaFinInput = view.findViewById(R.id.input_hora_fin);
        reservarBtn = view.findViewById(R.id.btn_reservar);

        viewModel = new ViewModelProvider(requireActivity()).get(ReservaViewModel.class);

        configurarInputs();
        observarResultado();
    }

    private void configurarInputs() {
        fechaInput.setOnClickListener(v -> mostrarDatePicker());
        horaInicioInput.setOnClickListener(v -> mostrarTimePicker(true));
        horaFinInput.setOnClickListener(v -> mostrarTimePicker(false));

        reservarBtn.setOnClickListener(v -> {
            try {
                // Comprobar si el usuario está logueado
                if (DataRepository.getInstance().getCurrentUser() == null) {
                    SpannableString mensaje = new SpannableString("Debes iniciar sesión o registrarte para reservar");
                    mensaje.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.red)), 0, mensaje.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    Toast toast = Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG);
                    toast.show();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    startActivity(intent);
                    return;
                }
                String tipo = tipoPlazaSpinner.getSelectedItem() != null ? tipoPlazaSpinner.getSelectedItem().toString() : null;
                String usuario = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (fechaSeleccionada == null || tipo == null || (horaInicio == 0 && minutoInicio == 0 && horaFin == 0 && minutoFin == 0)) {
                    Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                int minutosInicio = horaInicio * 60 + minutoInicio;
                int minutosFin = horaFin * 60 + minutoFin;
                if (minutosFin <= minutosInicio) {
                    Toast.makeText(requireContext(), "La hora de fin debe ser mayor que la de inicio", Toast.LENGTH_SHORT).show();
                    return;
                }
                Plaza plaza = new Plaza(tipo);
                Reserva reserva = new Reserva(
                        fechaSeleccionada,
                        usuario,
                        java.util.UUID.randomUUID().toString(),
                        plaza,
                        new Hora(horaInicio, minutoInicio, horaFin, minutoFin)
                );
                viewModel.hacerReserva(reserva, new com.lksnext.parkingplantilla.repository.ReservaRepository.FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        if (DataRepository.getInstance().getCurrentUser() != null) {
                            androidx.navigation.Navigation.findNavController(requireView())
                                .navigate(R.id.listareservas);
                            viewModel.cargarReservasUsuario();
                        }
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "Reserva inválida o solapada", Toast.LENGTH_SHORT).show();
                    }
                });
                viewModel.programarNotificacionesReserva(requireContext(), reserva);
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(requireContext(), "Error inesperado: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void observarResultado() {
        viewModel.getReservaExitosa().observe(getViewLifecycleOwner(), exito -> {
            if (exito != null) {
                Toast.makeText(requireContext(),
                        exito ? "Reserva realizada con éxito" : "Reserva inválida o solapada",
                        Toast.LENGTH_SHORT).show();
                if (exito) {
                    // Navega de vuelta a la lista de reservas
                    androidx.navigation.Navigation.findNavController(requireView())
                        .navigate(R.id.action_crearReserva_to_listareservas);
                }
            }
        });
    }

    private void mostrarDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),
                (view, year, month, day) -> {
                    fechaSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, day);
                    fechaInput.setText(fechaSeleccionada);
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        dialog.show();
    }

    private void mostrarTimePicker(boolean esInicio) {
        Calendar c = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    if (esInicio) {
                        horaInicio = hourOfDay;
                        minutoInicio = minute;
                        horaInicioInput.setText(String.format("%02d:%02d", horaInicio, minutoInicio));
                    } else {
                        horaFin = hourOfDay;
                        minutoFin = minute;
                        horaFinInput.setText(String.format("%02d:%02d", horaFin, minutoFin));
                    }
                },
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);

        dialog.show();
    }
}
