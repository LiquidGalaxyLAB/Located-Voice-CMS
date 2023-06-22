package com.gsoc.ijosa.liquidgalaxycontroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TourBottomSheetFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setStyle(STYLE_NORMAL, R.style.TourAppBottomSheetDialogTheme);
        return inflater.inflate(R.layout.bottom_sheet_tour, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            // Retrieve the fragment instance with arguments
            TourUserFragment tourFragment = new TourUserFragment();

            // Inflate your fragment inside the FragmentContainerView
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerViewTour, tourFragment)
                    .commit();
        }
    }

    // Other methods and code
}
