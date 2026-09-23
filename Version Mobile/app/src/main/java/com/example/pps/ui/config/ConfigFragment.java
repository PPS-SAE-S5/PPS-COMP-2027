package com.example.pps.ui.config;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pps.databinding.FragmentConfigBinding;

import java.util.List;

public class ConfigFragment extends Fragment {

    private FragmentConfigBinding binding;
    private ConfigViewModel viewModel;

    private List<Competition> listeCompetitions;
    private List<TypeEpreuve> listeTypesEpreuve;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentConfigBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ConfigViewModel.class);

        viewModel.getCompetitions().observe(getViewLifecycleOwner(), competitions -> {
            listeCompetitions = competitions;
            ArrayAdapter<Competition> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, competitions);
            binding.spinnerCompetition.setAdapter(adapter);
        });

        viewModel.getTypesEpreuve().observe(getViewLifecycleOwner(), types -> {
            listeTypesEpreuve = types;
            ArrayAdapter<TypeEpreuve> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, types);
            binding.spinnerTypeEpreuve.setAdapter(adapter);
        });

        viewModel.getResultatCreation().observe(getViewLifecycleOwner(), resultat -> {
            if ("OK".equals(resultat)) {
                Toast.makeText(requireContext(), "Manche créée avec succès", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), resultat, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.chargerListes();

        binding.btnEnregistrer.setOnClickListener(v -> creerManche());
    }

    private void creerManche() {
        if (listeCompetitions == null || listeTypesEpreuve == null
                || listeCompetitions.isEmpty() || listeTypesEpreuve.isEmpty()) {
            Toast.makeText(requireContext(), "Listes non chargées, réessayez", Toast.LENGTH_SHORT).show();
            return;
        }

        String numeroTexte = binding.editNumero.getText().toString().trim();
        String date = binding.editDate.getText().toString().trim();
        String distanceTexte = binding.editDistance.getText().toString().trim();

        if (numeroTexte.isEmpty() || date.isEmpty()) {
            Toast.makeText(requireContext(), "Remplissez au moins le numéro et la date", Toast.LENGTH_SHORT).show();
            return;
        }

        Competition competition = (Competition) binding.spinnerCompetition.getSelectedItem();
        TypeEpreuve type = (TypeEpreuve) binding.spinnerTypeEpreuve.getSelectedItem();
        int numero = Integer.parseInt(numeroTexte);
        Double distance = distanceTexte.isEmpty() ? null : Double.parseDouble(distanceTexte);

        viewModel.creerManche(competition.getId(), type.getId(), numero, date, distance);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}