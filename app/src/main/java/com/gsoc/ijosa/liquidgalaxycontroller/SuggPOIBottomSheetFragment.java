package com.gsoc.ijosa.liquidgalaxycontroller;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SuggPOIBottomSheetFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
        return inflater.inflate(R.layout.bottomsheetlayout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            // Retrieve the fragment instance with arguments
            Bundle bundle = getArguments();
            SearchFragment searchFragment = new SearchFragment();
            searchFragment.setArguments(bundle);

            // Inflate your fragment inside the FragmentContainerView
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, searchFragment)
                    .commit();
        }
    }
    // Other methods and code
}

