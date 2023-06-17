package com.gsoc.ijosa.liquidgalaxycontroller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentContainerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.bottomsheetlayout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentContainerView bottomSheetContainer = view.findViewById(R.id.fragmentContainerView);

        if (savedInstanceState == null) {
            // Inflate your fragment inside the FragmentContainerView
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, new SearchFragment())
                    .commit();
        }
    }
    // Other methods and code
}

