package com.gsoc.vedantsingh.locatedvoicecms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SuggPOIBottomSheetFragment extends BottomSheetDialogFragment {

    @Override
    public void onStart() {
        super.onStart();

        // Set the custom behavior for the BottomSheetDialogFragment
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setDraggable(false);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }

        // Disable dimming
        dialog.getWindow().setDimAmount(0f);
    }


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
        ImageView dismissButton = view.findViewById(R.id.dismissButton);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchFragment.audioPlayerStop();
                if(SearchFragment.mDriveServiceHelper != null){
                    SearchFragment.mDriveServiceHelper.stopVoicePlayer();
                }
                dismiss(); // Dismiss the BottomSheet
            }
        });


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

