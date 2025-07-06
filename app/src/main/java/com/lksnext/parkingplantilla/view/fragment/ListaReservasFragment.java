package com.lksnext.parkingplantilla.view.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.domain.Reserva;
import com.lksnext.parkingplantilla.domain.Hora;
import com.lksnext.parkingplantilla.domain.Plaza;
import com.lksnext.parkingplantilla.view.activity.ReservaAdapter;
import com.lksnext.parkingplantilla.viewmodel.ReservaViewModel;

import java.util.List;

public class ListaReservasFragment extends Fragment implements ReservaAdapter.OnCancelarClickListener, ReservaAdapter.OnEditarClickListener {

    private ReservaViewModel viewModel;
    private ReservaAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista_reservas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_reservas);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ReservaAdapter(this, this);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(requireActivity()).get(ReservaViewModel.class);
        // Si no está autenticado, navega al fragmento de perfil
        if (com.lksnext.parkingplantilla.data.DataRepository.getInstance().getCurrentUser() == null) {
            androidx.navigation.Navigation.findNavController(view)
                .navigate(R.id.perfil); // Cambia 'fragment_perfil' si tu id es diferente
            return;
        }
        // Para mostrar solo reservas actuales e histórico del último mes
        viewModel.cargarReservasUsuarioUltimoMes();
        viewModel.getReservasUsuario().observe(getViewLifecycleOwner(), this::mostrarReservas);

        // Configura el click del FAB para navegar a crearReservaFragment
        View fab = view.findViewById(R.id.fab_add_reserva);
        fab.setOnClickListener(v -> {
            if (com.lksnext.parkingplantilla.data.DataRepository.getInstance().getCurrentUser() == null) {
                androidx.navigation.Navigation.findNavController(view)
                    .navigate(R.id.perfil);
            } else {
                androidx.navigation.Navigation.findNavController(view)
                    .navigate(R.id.action_listaReservas_to_crearReserva);
            }
        });
    }

    private void mostrarReservas(List<Reserva> reservas) {
        adapter.setLista(reservas);
    }

    @Override
    public void onCancelarClick(Reserva reserva) {
        // Aquí debes tener el id del documento de la reserva en Firestore
        String reservaId = reserva.getId(); // Asegúrate de que este id es el de Firestore
        viewModel.cancelarReservaFirestore(reservaId, new com.lksnext.parkingplantilla.repository.ReservaRepository.FirestoreCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Reserva cancelada", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error al cancelar la reserva", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditarClick(Reserva reserva) {
        mostrarDialogoEditarReserva(reserva);
    }

    private void mostrarDialogoEditarReserva(Reserva reserva) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_editar_reserva, null);
        Spinner spinnerTipo = dialogView.findViewById(R.id.spinner_tipo);
        EditText editFecha = dialogView.findViewById(R.id.edit_fecha);
        EditText editHoraInicio = dialogView.findViewById(R.id.edit_hora_inicio);
        EditText editHoraFin = dialogView.findViewById(R.id.edit_hora_fin);
        // Si los campos no existen, crea los EditText temporalmente (evita crash, pero lo ideal es añadirlos al layout XML)
        if (spinnerTipo == null) {
            spinnerTipo = new Spinner(requireContext());
        }
        if (editHoraInicio == null) {
            editHoraInicio = new EditText(requireContext());
            editHoraInicio.setHint("Hora inicio");
        }
        if (editHoraFin == null) {
            editHoraFin = new EditText(requireContext());
            editHoraFin.setHint("Hora fin");
        }
        // Configura spinner
        ArrayAdapter<CharSequence> adapterTipo = ArrayAdapter.createFromResource(requireContext(), R.array.tipos_plaza, android.R.layout.simple_spinner_item);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);
        spinnerTipo.setSelection(adapterTipo.getPosition(reserva.getPlazaId().getTipo()));
        editFecha.setText(reserva.getFecha());
        editHoraInicio.setText(reserva.getHoraInicio().toHoraMinutos());
        editHoraFin.setText(reserva.getHoraInicio().toHoraMinutosFin());
        // DatePicker y TimePicker
        editFecha.setOnClickListener(v -> {
            // Muestra un DatePickerDialog y actualiza editFecha
            // ...implementación...
        });
        editHoraInicio.setOnClickListener(v -> {
            // Muestra un TimePickerDialog y actualiza editHoraInicio
            // ...implementación...
        });
        editHoraFin.setOnClickListener(v -> {
            // Muestra un TimePickerDialog y actualiza editHoraFin
            // ...implementación...
        });
        Spinner finalSpinnerTipo = spinnerTipo;
        EditText finalEditHoraInicio = editHoraInicio;
        EditText finalEditHoraFin = editHoraFin;
        new AlertDialog.Builder(requireContext())
            .setTitle("Editar reserva")
            .setView(dialogView)
            .setPositiveButton("Guardar", (dialog, which) -> {
                final String tipo = finalSpinnerTipo.getSelectedItem().toString();
                final String fecha = editFecha.getText().toString();
                final String horaInicioStr = finalEditHoraInicio.getText().toString();
                final String horaFinStr = finalEditHoraFin.getText().toString();
                // Validación básica
                if (fecha.isEmpty() || horaInicioStr.isEmpty() || horaFinStr.isEmpty()) {
                    Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    return;
                }
                int[] hi = parseHora(horaInicioStr);
                int[] hf = parseHora(horaFinStr);
                if (hi == null || hf == null) {
                    Toast.makeText(requireContext(), "Formato de hora inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
                Hora horaInicio = new Hora(hi[0], hi[1], 0, 0);
                Hora horaFin = new Hora(hf[0], hf[1], 0, 0);
                Reserva nuevaReserva = new Reserva(fecha, reserva.getUsuario(), reserva.getId(), new Plaza(tipo), horaInicio);
                nuevaReserva.setHoraFin(horaFin);
                String reservaId = reserva.getId(); // Asegúrate de que este id es el de Firestore
                viewModel.editarReservaFirestore(reservaId, nuevaReserva, new com.lksnext.parkingplantilla.repository.ReservaRepository.FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(requireContext(), "Reserva editada", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "No hay plazas libres para ese horario", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private int[] parseHora(String horaStr) {
        try {
            String[] partes = horaStr.split(":");
            return new int[]{Integer.parseInt(partes[0]), Integer.parseInt(partes[1])};
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Vuelve a cargar las reservas del usuario actual al volver al fragmento
        if (com.lksnext.parkingplantilla.data.DataRepository.getInstance().getCurrentUser() != null) {
            viewModel.cargarReservasUsuario();
        }
    }
}
