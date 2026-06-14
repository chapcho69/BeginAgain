package com.olivearchi.goodroutine;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import androidx.lifecycle.ViewModelProvider;

import com.olivearchi.goodroutine.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private TodoViewModel viewModel;
    private TodoAdapter adapter;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);
        
        updateFilteredList();

        viewModel.getTodoList().observe(getViewLifecycleOwner(), items -> updateFilteredList());
        viewModel.getFilterType().observe(getViewLifecycleOwner(), filterType -> {
            updateFilteredList();
            if (filterType == TodoViewModel.FILTER_IN_PROGRESS) {
                binding.toggleGroupFilter.check(R.id.button_filter_inprogress);
            } else if (filterType == TodoViewModel.FILTER_DONE) {
                binding.toggleGroupFilter.check(R.id.button_filter_done);
            } else {
                binding.toggleGroupFilter.check(R.id.button_filter_all);
            }
        });

        binding.toggleGroupFilter.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.button_filter_inprogress) {
                    viewModel.setFilterType(TodoViewModel.FILTER_IN_PROGRESS);
                } else if (checkedId == R.id.button_filter_done) {
                    viewModel.setFilterType(TodoViewModel.FILTER_DONE);
                } else if (checkedId == R.id.button_filter_all) {
                    viewModel.setFilterType(TodoViewModel.FILTER_ALL);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.loadTodos();
        }
    }

    private void updateFilteredList() {
        List<TodoItem> allItems = viewModel.getTodoList().getValue();
        Integer filterType = viewModel.getFilterType().getValue();
        if (filterType == null) filterType = TodoViewModel.FILTER_ALL;
        
        if (allItems == null) return;

        List<TodoItem> filteredItems = new ArrayList<>();
        for (TodoItem item : allItems) {
            if (filterType == TodoViewModel.FILTER_ALL) {
                filteredItems.add(item);
            } else if (filterType == TodoViewModel.FILTER_IN_PROGRESS) {
                if (!item.isDone()) {
                    filteredItems.add(item);
                }
            } else if (filterType == TodoViewModel.FILTER_DONE) {
                if (item.isDone()) {
                    filteredItems.add(item);
                }
            }
        }
        
        // Sort by startDateTime
        java.util.Collections.sort(filteredItems, (o1, o2) -> {
            String d1 = o1.getStartDateTime();
            String d2 = o2.getStartDateTime();
            if (d1 == null) return (d2 == null) ? 0 : 1;
            if (d2 == null) return -1;
            return d1.compareTo(d2);
        });
        
        adapter = new TodoAdapter(filteredItems, 
            position -> {
                TodoItem clickedItem = filteredItems.get(position);
                int originalIndex = allItems.indexOf(clickedItem);
                Bundle bundle = new Bundle();
                bundle.putInt("todoPosition", originalIndex);
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
            },
            (item, isChecked) -> {
                viewModel.toggleDone(item, isChecked);
            }
        );
        binding.recyclerviewTodo.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
