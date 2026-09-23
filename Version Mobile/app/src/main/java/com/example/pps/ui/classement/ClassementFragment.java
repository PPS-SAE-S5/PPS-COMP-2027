package com.example.pps.ui.classement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.pps.databinding.FragmentClassementBinding;

public class ClassementFragment extends Fragment {

    private FragmentClassementBinding binding;
    private ClassementViewModel viewModel;
    private ClassementAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentClassementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ClassementViewModel.class);

        adapter = new ClassementAdapter();
        binding.recyclerViewClassement.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewClassement.setAdapter(adapter);

        viewModel.getClassementData().observe(getViewLifecycleOwner(), adapter::setItems);
        viewModel.chargerClassement();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}