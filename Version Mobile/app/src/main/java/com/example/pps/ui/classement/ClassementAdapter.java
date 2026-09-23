package com.example.pps.ui.classement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pps.R;

import java.util.ArrayList;
import java.util.List;

public class ClassementAdapter extends RecyclerView.Adapter<ClassementAdapter.ViewHolder> {

    private List<Participant> items = new ArrayList<>();

    public void setItems(List<Participant> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_classement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Participant participant = items.get(position);
        holder.textRang.setText(String.valueOf(participant.getRang()));
        holder.textNom.setText(participant.getNom());
        holder.textNumero.setText(String.valueOf(participant.getNumero()));
        holder.textPoints.setText(String.valueOf(participant.getPoints()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textRang;
        final TextView textNom;
        final TextView textNumero;
        final TextView textPoints;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textRang = itemView.findViewById(R.id.textRang);
            textNom = itemView.findViewById(R.id.textNom);
            textNumero = itemView.findViewById(R.id.textNumero);
            textPoints = itemView.findViewById(R.id.textPoints);
        }
    }
}