package com.gsoc.vedantsingh.locatedvoicecms;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.gsoc.vedantsingh.locatedvoicecms.beans.PlaceInfo;
import com.gsoc.vedantsingh.locatedvoicecms.utils.NearbyPlacesAdapter;

import java.util.ArrayList;
import java.util.List;

public class NearbyPlacesActivity extends AppCompatActivity {
    ListView placesList;
    ImageButton closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_places);

        placesList = findViewById(R.id.placesListView);
        closeButton = findViewById(R.id.closeButton);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        List<PlaceInfo> nearbyPlaces = getIntent().getParcelableArrayListExtra("nearbyPlacesList");
        Log.d("Nearby", nearbyPlaces.get(0).getTitle());

        if (nearbyPlaces != null && !nearbyPlaces.isEmpty()) {
            ListView listView = findViewById(R.id.placesListView);
            NearbyPlacesAdapter adapter = new NearbyPlacesAdapter(this, nearbyPlaces);
            listView.setAdapter(adapter);
        } else {
            Toast.makeText(this, "Click on a POI to view Nearby Places", Toast.LENGTH_SHORT).show();
        }
    }
}