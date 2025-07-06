package com.lksnext.parkingplantilla.view.activity;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lksnext.parkingplantilla.R;
import com.lksnext.parkingplantilla.domain.Reserva;

import java.util.ArrayList;
import java.util.List;

public class ReservaAdapter extends RecyclerView.Adapter<ReservaAdapter.ReservaViewHolder> {

    public interface OnCancelarClickListener {
        void onCancelarClick(Reserva reserva);
    }

    public interface OnEditarClickListener {
        void onEditarClick(Reserva reserva);
    }

    private List<Reserva> lista = new ArrayList<>();
    private final OnCancelarClickListener listener;
    private final OnEditarClickListener editarListener;

    public ReservaAdapter(OnCancelarClickListener listener, OnEditarClickListener editarListener) {
        this.listener = listener;
        this.editarListener = editarListener;
    }

    public void setLista(List<Reserva> nuevasReservas) {
        this.lista = nuevasReservas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reserva, parent, false);
        return new ReservaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        Reserva reserva = lista.get(position);

        holder.textTipo.setText("Tipo: " + reserva.getPlazaId().getTipo());
        holder.textFecha.setText("Fecha: " + reserva.getFecha());
        holder.textHora.setText("Hora: " + reserva.getHoraInicio().toString());
        holder.textNumeroPlaza.setText("Plaza: " + reserva.getPlazaId().getId());

        holder.btnCancelar.setOnClickListener(v -> listener.onCancelarClick(reserva));
        holder.btnEditar.setOnClickListener(v -> editarListener.onEditarClick(reserva));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        TextView textTipo, textFecha, textHora, textNumeroPlaza;
        Button btnCancelar, btnEditar;

        public ReservaViewHolder(@NonNull View itemView) {
            super(itemView);
            textTipo = itemView.findViewById(R.id.text_tipo);
            textFecha = itemView.findViewById(R.id.text_fecha);
            textHora = itemView.findViewById(R.id.text_hora);
            textNumeroPlaza = itemView.findViewById(R.id.text_numero_plaza);
            btnCancelar = itemView.findViewById(R.id.btn_cancelar);
            btnEditar = itemView.findViewById(R.id.btn_editar);
        }
    }
}
